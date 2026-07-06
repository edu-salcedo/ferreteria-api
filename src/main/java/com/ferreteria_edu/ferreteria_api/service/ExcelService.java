package com.ferreteria_edu.ferreteria_api.service;

import com.ferreteria_edu.ferreteria_api.product.dto.ProductDTO;
import com.ferreteria_edu.ferreteria_api.product.entity.ProductVariant;
import com.ferreteria_edu.ferreteria_api.product.mapper.ProductMapper;
import com.ferreteria_edu.ferreteria_api.category.entity.Category;
import com.ferreteria_edu.ferreteria_api.product.entity.Product;
import com.ferreteria_edu.ferreteria_api.product.repository.ProductVariantRepository;
import com.ferreteria_edu.ferreteria_api.product.service.ProductService;
import com.ferreteria_edu.ferreteria_api.category.repository.CategoryRepository;
import com.ferreteria_edu.ferreteria_api.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class ExcelService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductService productService;

    public void ExcelImport(MultipartFile file) throws Exception {

        String filename = file.getOriginalFilename();

        if (filename == null ||
                !(filename.endsWith(".xlsx") || filename.endsWith(".xls"))) {
            throw new Exception("Solo Excel permitido");
        }

        Map<String, Category> categoryCache = new HashMap<>();

        try (InputStream is = file.getInputStream()) {

            Workbook workbook = filename.endsWith(".xlsx")
                    ? new XSSFWorkbook(is)
                    : new HSSFWorkbook(is);

            Sheet sheet = workbook.getSheetAt(0);
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

            for (Row row : sheet) {

                if (row.getRowNum() == 0) {
                    continue;
                }

                String name = getString(row.getCell(2));

                if (name == null || name.isBlank()) {
                    continue;
                }

                String categoryName = getString(row.getCell(3));

                if (categoryName == null || categoryName.isBlank()) {
                    categoryName = "SIN CATEGORIA";
                }

                categoryName = categoryName.trim().toUpperCase();

                int stock = getInt(row.getCell(4), evaluator);

                BigDecimal price = getBigDecimal(row.getCell(5), evaluator);

                String image = getString(row.getCell(0));

                // ==========================
                // BUSCAR / CREAR CATEGORIA
                // ==========================

                Category category = categoryCache.get(categoryName);

                if (category == null) {

                    Optional<Category> optionalCategory =
                            categoryRepository.findByNameIgnoreCase(categoryName);

                    if (optionalCategory.isPresent()) {

                        category = optionalCategory.get();

                    } else {

                        Category newCategory = new Category();
                        newCategory.setName(categoryName);

                        category = categoryRepository.save(newCategory);
                    }

                    categoryCache.put(categoryName, category);
                }

                // ==========================
                // BUSCAR / CREAR PRODUCTO
                // ==========================

                Product product;

                Optional<Product> optionalProduct =
                        productRepository.findByName(name.trim().toUpperCase());

                if (optionalProduct.isPresent()) {

                    product = optionalProduct.get();

                } else {

                    product = new Product();
                    product.setName(name.trim().toUpperCase());
                    product.setImg(image);
                    product.setCategory(category);
                    product.setState(true);

                    product = productRepository.save(product);
                }

                // ==========================
                // CREAR VARIANTE
                // ==========================

                ProductVariant variant = new ProductVariant();

                variant.setProduct(product);
                variant.setMeasure("UNICO");
                variant.setPurchasePrice(price);
                variant.setStock(stock);

                BigDecimal margin =
                        productService.calculateProfitMargin(category.getId(), price);

                variant.setProfitMargin(margin);

                BigDecimal salePrice = price.add(
                        price.multiply(margin)
                                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                );

                variant.setSalePrice(salePrice);

                variantRepository.save(variant);
            }
        }
    }

    // ================= HELPERS =================

    private String getString(Cell cell) {
        if (cell == null) return null;
        return cell.toString().trim();
    }

    private int getInt(Cell cell, FormulaEvaluator eval) {
        if (cell == null) return 0;
        Cell c = eval.evaluateInCell(cell);
        return c.getCellType() == CellType.NUMERIC
                ? (int) c.getNumericCellValue()
                : 0;
    }

    private BigDecimal getBigDecimal(Cell cell, FormulaEvaluator eval) {
        if (cell == null) return BigDecimal.ZERO;

        Cell c = eval.evaluateInCell(cell);

        if (c.getCellType() == CellType.NUMERIC) {
            return BigDecimal.valueOf(c.getNumericCellValue());
        }

        try {
            return new BigDecimal(c.toString().replace(",", "."));
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
}