package com.ferreteria_edu.ferreteria_api.product.mapper;

import com.ferreteria_edu.ferreteria_api.product.dto.ProductVariantDTO;
import com.ferreteria_edu.ferreteria_api.product.entity.ProductVariant;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ProductVariantMapper {

    public static ProductVariantDTO toDTO(ProductVariant variant) {

        if (variant == null) return null;

        BigDecimal purchasePrice = variant.getPurchasePrice() != null
                ? variant.getPurchasePrice()
                : BigDecimal.ZERO;

        BigDecimal margin = variant.getProfitMargin() != null
                ? variant.getProfitMargin()
                : BigDecimal.ZERO;

        BigDecimal profit = purchasePrice
                .multiply(margin)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal salePrice = purchasePrice.add(profit);

        // Redondeo al múltiplo de 50
        salePrice = salePrice
                .divide(BigDecimal.valueOf(50), 0, RoundingMode.CEILING)
                .multiply(BigDecimal.valueOf(50));

        return ProductVariantDTO.builder()
                .id(variant.getId())
                .measure(variant.getMeasure())
                .purchasePrice(purchasePrice)
                .profitMargin(margin)
                .profit(profit)
                .salePrice(salePrice)
                .stock(variant.getStock())
                .build();
    }

    public static ProductVariant toEntity(ProductVariantDTO dto) {

        if (dto == null) return null;

        ProductVariant variant = new ProductVariant();

        variant.setId(dto.getId());
        variant.setMeasure(dto.getMeasure());

        BigDecimal purchasePrice = dto.getPurchasePrice() != null
                ? dto.getPurchasePrice()
                : BigDecimal.ZERO;

        BigDecimal margin = dto.getProfitMargin() != null
                ? dto.getProfitMargin()
                : BigDecimal.ZERO;

        BigDecimal profit = purchasePrice
                .multiply(margin)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal salePrice = purchasePrice.add(profit);

        salePrice = salePrice
                .divide(BigDecimal.valueOf(50), 0, RoundingMode.CEILING)
                .multiply(BigDecimal.valueOf(50));

        variant.setPurchasePrice(purchasePrice);
        variant.setProfitMargin(margin);
        variant.setSalePrice(salePrice);
        variant.setStock(dto.getStock());

        return variant;
    }
}