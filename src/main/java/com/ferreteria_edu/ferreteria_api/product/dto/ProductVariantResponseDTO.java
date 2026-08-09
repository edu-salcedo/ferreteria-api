package com.ferreteria_edu.ferreteria_api.product.dto;

import java.math.BigDecimal;
import java.util.List;

public class ProductVariantResponseDTO {
    private Long id;

    private String measure;

    private BigDecimal purchasePrice;

    private BigDecimal salePrice;

    private BigDecimal profitMargin;

    private Integer stock;

    private String sku;
    private List<ProductVariantResponseDTO> variants;
}
