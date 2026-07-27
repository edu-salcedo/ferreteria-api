package com.ferreteria_edu.ferreteria_api.order.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.ferreteria_edu.ferreteria_api.enun.PaymentMethod;
import com.ferreteria_edu.ferreteria_api.order.dto.ImportSalesResultDTO;
import com.ferreteria_edu.ferreteria_api.order.dto.SaleImportDTO;
import lombok.RequiredArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SalesExcelService {

    private final SalesImportService salesImportService;

    public ImportSalesResultDTO importExcel(MultipartFile file) throws Exception {
        
        Workbook workbook = WorkbookFactory.create(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);
        ImportSalesResultDTO result = new ImportSalesResultDTO();
        List<SaleImportDTO> sales = new ArrayList<>();

        // Formateador de Apache POI para evitar excepciones de tipo de celda
        DataFormatter formatter = new DataFormatter();

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null)
                continue;

            SaleImportDTO dto = new SaleImportDTO();

            // 1. Manejo seguro de la FECHA (Columna 0)
            Cell cellFecha = row.getCell(0);
            if (cellFecha != null && cellFecha.getCellType() == CellType.NUMERIC
                    && DateUtil.isCellDateFormatted(cellFecha)) {
                LocalDate localDate = cellFecha.getDateCellValue().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
                dto.setCreatedAt(localDate.atStartOfDay());
            } else {
                // Alternativa si por alguna razón viene como texto plano en vez de número de
                // fecha
                String dateStr = formatter.formatCellValue(cellFecha).trim();
                if (!dateStr.isEmpty()) {
                    java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("M/d/yyyy");
                    dto.setCreatedAt(LocalDate.parse(dateStr, dtf).atStartOfDay());
                }
            }
            String saleNumStr = formatter.formatCellValue(row.getCell(1)).replaceAll("[^0-9]", "");
            dto.setSaleNumber(saleNumStr.isEmpty() ? 0 : Integer.parseInt(saleNumStr));
            dto.setProductName(formatter.formatCellValue(row.getCell(2)).trim());
            String quantityStr = formatter.formatCellValue(row.getCell(3)).replaceAll("[^0-9]", "");
            dto.setQuantity(quantityStr.isEmpty() ? 0 : Integer.parseInt(quantityStr));
            dto.setPurchasePrice(BigDecimal.valueOf(row.getCell(4) != null ? row.getCell(4).getNumericCellValue() : 0.0));
            dto.setPurchaseTotal( BigDecimal.valueOf(row.getCell(5) != null ? row.getCell(5).getNumericCellValue() : 0.0));
            dto.setSalePrice(BigDecimal.valueOf(row.getCell(6) != null ? row.getCell(6).getNumericCellValue() : 0.0));
            dto.setSaleTotal(BigDecimal.valueOf(row.getCell(7) != null ? row.getCell(7).getNumericCellValue() : 0.0));
            String payment = formatter.formatCellValue(row.getCell(8)).trim().toUpperCase();

            if (payment.isEmpty()) {
                dto.setPaymentMethod(PaymentMethod.EFECTIVO); // Valor por defecto si está vacío
            } else {
                switch (payment) {
                    case "EFECTIVO":
                        dto.setPaymentMethod(PaymentMethod.EFECTIVO);
                        break;
                    case "DEBITO":
                        dto.setPaymentMethod(PaymentMethod.DEBITO);
                        break;
                    case "TRANSFER":
                    case "TRANSFERENCIA":
                        dto.setPaymentMethod(PaymentMethod.TRANSFERENCIA);
                        break;
                    case "TARJETA":
                    case "CREDITO":
                        dto.setPaymentMethod(PaymentMethod.TARJETA);
                        break;
                    default:
                        dto.setPaymentMethod(PaymentMethod.EFECTIVO);
                }
            }

            Cell invoiceCell = row.getCell(9);

            if (invoiceCell != null && invoiceCell.getCellType() == CellType.NUMERIC) {
                dto.setInvoice(true);
                dto.setInvoiceAmount(BigDecimal.valueOf(invoiceCell.getNumericCellValue()));
            } else {
                dto.setInvoice(false);
                dto.setInvoiceAmount(BigDecimal.ZERO);
            }
            // Guardar DTO procesado
            sales.add(dto);
        }

        // Asignar el total fuera del bucle para mejor rendimiento
        result.setImportedOrders(sales.size());

        salesImportService.importSales(sales);
        workbook.close();
        return result;
    }
}
