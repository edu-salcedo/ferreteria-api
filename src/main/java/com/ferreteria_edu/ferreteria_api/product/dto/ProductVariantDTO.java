package com.ferreteria_edu.ferreteria_api.product.dto;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class ProductVariantDTO {
    private Long id;

    private String measure;

    private BigDecimal purchasePrice;

    private BigDecimal salePrice;

    private Integer stock;

    private BigDecimal profitMargin;

    private BigDecimal profit;
}
