package com.ferreteria_edu.ferreteria_api.product.dto;

import java.math.BigDecimal;
import java.util.List;

public class ProductVariantRequestDTO {
    private String measure;

    private BigDecimal purchasePrice;

    private BigDecimal salePrice;

    private BigDecimal profitMargin;

    private Integer stock;

    private String sku;
    private List<ProductVariantRequestDTO> variants;
}
