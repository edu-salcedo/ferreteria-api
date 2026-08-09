package com.ferreteria_edu.ferreteria_api.product.service;

import com.ferreteria_edu.ferreteria_api.category.entity.Category;
import com.ferreteria_edu.ferreteria_api.category.repository.CategoryRepository;
import com.ferreteria_edu.ferreteria_api.order.service.PriceCalculatorService;
import com.ferreteria_edu.ferreteria_api.product.dto.ImportResultDTO;
import com.ferreteria_edu.ferreteria_api.product.dto.ProductImportDTO;
import com.ferreteria_edu.ferreteria_api.product.entity.Product;
import com.ferreteria_edu.ferreteria_api.product.entity.ProductVariant;
import com.ferreteria_edu.ferreteria_api.product.repository.ProductRepository;
import com.ferreteria_edu.ferreteria_api.product.repository.ProductVariantRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductImportService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final CategoryRepository categoryRepository;
    private final ProductService productService;
    private final PriceCalculatorService priceCalculatorService;

    public ImportResultDTO createOrUpdate(ProductImportDTO dto) {

        ImportResultDTO result = new ImportResultDTO();

        // ===========================================
        // BUSCAR / CREAR CATEGORIA
        // ===========================================

        Category category = categoryRepository
                .findByNameIgnoreCase(dto.getCategoryName())
                .orElseGet(() -> {

                    Category c = new Category();
                    c.setName(dto.getCategoryName());
                    c.setDescription("");

                    result.setNewCategories(1);

                    return categoryRepository.save(c);
                });

        // ===========================================
        // BUSCAR / CREAR PRODUCTO
        // ===========================================

        Product product = productRepository
                .findByName(dto.getName().trim().toUpperCase())
                .orElseGet(() -> {

                    Product p = new Product();

                    p.setName(dto.getName().trim().toUpperCase());
                    p.setImg(dto.getImage());
                    p.setCategory(category);
                    p.setState(true);

                    result.setNewProducts(1);

                    return productRepository.save(p);
                });

        if (result.getNewProducts() == 0) {

            result.setUpdatedProducts(1);

            product.setCategory(category);

            if (dto.getImage() != null && !dto.getImage().isBlank()) {
                product.setImg(dto.getImage());
            }

            productRepository.save(product);
        }

        // ===========================================
        // MARGEN
        // ===========================================

        BigDecimal margin = productService.calculateProfitMargin(
                category.getId(),
                dto.getPurchasePrice()
        );

        // ===========================================
        // BUSCAR VARIANTE
        // ===========================================

        Optional<ProductVariant> optionalVariant =
                productVariantRepository.findByProduct_IdAndMeasure(
                        product.getId(),
                        dto.getMeasure()
                );

        if (optionalVariant.isPresent()) {

            ProductVariant variant = optionalVariant.get();

            variant.setPurchasePrice(dto.getPurchasePrice());

            variant.setStock(
                    variant.getStock() + dto.getStock()
            );

            variant.setProfitMargin(margin);

            variant.setSalePrice(
                    priceCalculatorService.calculateSalePrice(variant)
            );

            productVariantRepository.save(variant);

            result.setUpdatedVariants(1);

        } else {

            ProductVariant variant = new ProductVariant();

            variant.setProduct(product);

            variant.setMeasure(dto.getMeasure());

            variant.setPurchasePrice(dto.getPurchasePrice());

            variant.setStock(dto.getStock());

            variant.setProfitMargin(margin);

            variant.setSalePrice(
                    priceCalculatorService.calculateSalePrice(variant)
            );

            productVariantRepository.save(variant);

            result.setNewVariants(1);
        }

        return result;
    }
}