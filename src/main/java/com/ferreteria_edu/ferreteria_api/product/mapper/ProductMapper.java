package com.ferreteria_edu.ferreteria_api.product.mapper;

import com.ferreteria_edu.ferreteria_api.product.dto.ProductDTO;
import com.ferreteria_edu.ferreteria_api.category.entity.Category;
import com.ferreteria_edu.ferreteria_api.product.entity.Product;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ProductMapper {

    public static ProductDTO toDTO(Product p) {
        if (p == null) return null;

        // Evitar nulls en precios y margen
        BigDecimal price = p.getPurchasePrice() != null ? p.getPurchasePrice() : BigDecimal.ZERO;
        BigDecimal margin = p.getProfitMargin() != null ? p.getProfitMargin() : BigDecimal.ZERO;

        // Calcular ganancia
        BigDecimal profit = price.multiply(margin).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        // Precio final con ganancia
        BigDecimal finalPrice = price.add(profit);

        // Redondeo comercial al múltiplo de 50
        BigDecimal roundedFinalPrice = finalPrice
                .divide(new BigDecimal("50"), 0, RoundingMode.CEILING)
                .multiply(new BigDecimal("50"));

        return ProductDTO.builder()
                .id(p.getId())
                .name(p.getName() != null ? p.getName() : "Producto")
                .description(p.getDescription() != null ? p.getDescription() : "")
                .img(p.getImg() != null ? p.getImg() : "")
                .purchasePrice(price)
                .profitMargin(margin)
                .profit(profit)
                .salePrice(roundedFinalPrice)
                .stock(p.getStock())
                .state(p.isState())
                .categoryId(p.getCategory() != null ? p.getCategory().getId() : null)
                .categoryName(p.getCategory() != null ? p.getCategory().getName() : null)
                .build();
    }

    public static Product toEntity(ProductDTO dto, Category c) {
        if (dto == null) return null;

        Product p = new Product();
        p.setId(dto.getId());
        p.setName(dto.getName() != null ? dto.getName() : "Producto");
        p.setDescription(dto.getDescription() != null ? dto.getDescription() : "");
        p.setImg(dto.getImg() != null ? dto.getImg() : "");
        p.setPurchasePrice(dto.getPurchasePrice() != null ? dto.getPurchasePrice() : BigDecimal.ZERO);
        p.setStock(dto.getStock());
        p.setState(dto.isState());
        p.setCategory(c);
        p.setProfitMargin(dto.getProfitMargin() != null ? dto.getProfitMargin() : BigDecimal.ZERO);

        // 🔹 Calculamos salePrice y lo guardamos
        BigDecimal salePrice = p.getPurchasePrice()
                .add(p.getPurchasePrice()
                        .multiply(p.getProfitMargin())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));

        return p;
    }
}