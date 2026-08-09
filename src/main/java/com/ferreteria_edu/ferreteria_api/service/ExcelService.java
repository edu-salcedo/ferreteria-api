package com.ferreteria_edu.ferreteria_api.service;

import com.ferreteria_edu.ferreteria_api.product.dto.ImportResultDTO;
import com.ferreteria_edu.ferreteria_api.product.dto.ProductImportDTO;
import com.ferreteria_edu.ferreteria_api.product.service.ProductImportService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ExcelService {

    private final ProductImportService productImportService;

    public ImportResultDTO importExcel(MultipartFile file) throws Exception {

        String filename = file.getOriginalFilename();

        if (filename == null ||
                !(filename.toLowerCase().endsWith(".xlsx")
                        || filename.toLowerCase().endsWith(".xls"))) {

            throw new RuntimeException("Solo se permiten archivos Excel");
        }

        ImportResultDTO result = new ImportResultDTO();

        try (InputStream is = file.getInputStream()) {

            Workbook workbook = filename.toLowerCase().endsWith(".xlsx")
                    ? new XSSFWorkbook(is)
                    : new HSSFWorkbook(is);

            FormulaEvaluator evaluator =
                    workbook.getCreationHelper().createFormulaEvaluator();

            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {

                // Saltar encabezado
                if (row.getRowNum() == 0) {
                    continue;
                }

                String name = getString(row.getCell(2));

                if (name == null || name.isBlank()) {
                    continue;
                }

                ProductImportDTO dto = ProductImportDTO.builder()
                        .name(name.trim().toUpperCase())
                        .image(getString(row.getCell(0)))
                        .categoryName(getCategory(row))
                        .measure( getString(row.getCell(4))) // luego podés leer otra columna
                        .stock(getInt(row.getCell(5), evaluator))
                        .purchasePrice(getPrice(row.getCell(6), evaluator))
                        .build();

                ImportResultDTO partial =
                        productImportService.createOrUpdate(dto);

                result.setNewProducts(
                        result.getNewProducts()
                                + partial.getNewProducts());

                result.setUpdatedProducts(
                        result.getUpdatedProducts()
                                + partial.getUpdatedProducts());

                result.setNewVariants(
                        result.getNewVariants()
                                + partial.getNewVariants());

                result.setUpdatedVariants(
                        result.getUpdatedVariants()
                                + partial.getUpdatedVariants());

                result.setNewCategories(
                        result.getNewCategories()
                                + partial.getNewCategories());
            }

            workbook.close();
        }

        return result;
    }

    // ===========================
    // HELPERS
    // ===========================

    private String getCategory(Row row) {

        String category = getString(row.getCell(3));

        if (category == null || category.isBlank()) {
            return "SIN CATEGORIA";
        }

        return category.trim().toUpperCase();
    }

    private String getString(Cell cell) {

        if (cell == null) {
            return null;
        }

        return cell.toString().trim();
    }

    private int getInt(Cell cell, FormulaEvaluator evaluator) {

        if (cell == null) {
            return 0;
        }

        Cell evaluated = evaluator.evaluateInCell(cell);

        if (evaluated.getCellType() == CellType.NUMERIC) {
            return (int) evaluated.getNumericCellValue();
        }

        return 0;
    }

    private BigDecimal getPrice(Cell cell,
                                FormulaEvaluator evaluator) {

        if (cell == null) {
            return BigDecimal.ZERO;
        }

        Cell evaluated = evaluator.evaluateInCell(cell);

        if (evaluated.getCellType() == CellType.NUMERIC) {

            return BigDecimal.valueOf(
                    evaluated.getNumericCellValue()
            );
        }

        try {

            String value = evaluated.toString()
                    .replace(".", "")
                    .replace(",", ".");

            return new BigDecimal(value);

        } catch (Exception e) {

            return BigDecimal.ZERO;
        }
    }
}