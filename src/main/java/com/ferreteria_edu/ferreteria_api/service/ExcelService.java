package com.ferreteria_edu.ferreteria_api.service;

import com.ferreteria_edu.ferreteria_api.dto.productDto.ProductDTO;
import com.ferreteria_edu.ferreteria_api.mapper.ProductMapper;
import com.ferreteria_edu.ferreteria_api.model.Category;
import com.ferreteria_edu.ferreteria_api.model.Product;
import com.ferreteria_edu.ferreteria_api.repository.CategoryRepository;
import com.ferreteria_edu.ferreteria_api.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;



@Service
@RequiredArgsConstructor
public class ExcelService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductService productService;

    public void ExcelImport(MultipartFile file) throws Exception {

        String filename = file.getOriginalFilename();
        if (filename == null ||
                !(filename.toLowerCase().endsWith(".xlsx") || filename.toLowerCase().endsWith(".xls"))) {
            throw new Exception("Solo se permiten archivos Excel (.xlsx o .xls)");
        }

        try (InputStream is = file.getInputStream()) {

            Workbook workbook = filename.toLowerCase().endsWith(".xlsx")
                    ? new XSSFWorkbook(is)
                    : new HSSFWorkbook(is);

            Sheet sheet = workbook.getSheetAt(0);
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // encabezado

                Cell imgCell = row.getCell(0, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                Cell nameCell = row.getCell(2, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                Cell categoryCell = row.getCell(3, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                Cell stockCell = row.getCell(4, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                Cell priceCell = row.getCell(5, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);


                if (nameCell == null || priceCell == null) continue;

                String name = nameCell.getStringCellValue().trim();
                if (name.isEmpty()) continue;

                /* ========= CATEGORY ========= */
                String categoryName = "NUEVO";
                if (categoryCell != null && categoryCell.getCellType() == CellType.STRING) {
                    categoryName = categoryCell.getStringCellValue().trim();
                }

                final String finalCategoryName = categoryName;

                Category category = categoryRepository
                        .findByNameIgnoreCase(finalCategoryName)
                        .orElseGet(() -> {
                            Category c = new Category();
                            c.setName(finalCategoryName);
                            return categoryRepository.save(c);
                        });

                /* ========= STOCK (cantidad) ========= */
                int stock = 0;
                if (stockCell != null) {
                    Cell evaluatedStock = evaluator.evaluateInCell(stockCell);
                    if (evaluatedStock.getCellType() == CellType.NUMERIC) {
                        stock = (int) evaluatedStock.getNumericCellValue();
                    }
                }

                /* ========= PRICE ========= */
                BigDecimal price = BigDecimal.ZERO;
                //evalua si la celda tiene una fórmula si no queda el mismo valor
                Cell evaluatedPrice = evaluator.evaluateInCell(priceCell);

                if (evaluatedPrice.getCellType() == CellType.NUMERIC) {
                    price = BigDecimal.valueOf(evaluatedPrice.getNumericCellValue());
                } else if (evaluatedPrice.getCellType() == CellType.STRING) {
                    String raw = evaluatedPrice.getStringCellValue().trim();
                    if (!raw.equalsIgnoreCase("s/precio")) {
                        raw = raw.replace(".", "").replace(",", ".");
                        try {
                            price = new BigDecimal(raw);
                        } catch (Exception ignored) {
                            price = BigDecimal.ZERO;
                        }
                    }
                }

                String image = null;
                if (imgCell != null && imgCell.getCellType() == CellType.STRING) {
                    image = imgCell.getStringCellValue().trim();
                }
                BigDecimal margin = productService.calculateProfitMargin(category.getId(), price);
                if (margin == null) margin = BigDecimal.valueOf(40);

                /* ========= PRODUCT ========= */

                ProductDTO dto = new ProductDTO();
                dto.setName(name);
                dto.setPrice(price);
                dto.setStock(stock);
                dto.setCategoryId(category.getId());
                dto.setCategoryName(category.getName());
                dto.setImg(image);
                dto.setProfitMargin(margin);
                Product product = ProductMapper.toEntity(dto, category);
                product.setStock(stock);

                productRepository.save(product);
            }
        }
    }
}