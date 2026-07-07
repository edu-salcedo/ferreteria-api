package com.ferreteria_edu.ferreteria_api.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class ProductImportDTO {
    private String name;
    private String image;
    private String categoryName;

    private String measure;

    private Integer stock;

    private BigDecimal purchasePrice;
}
