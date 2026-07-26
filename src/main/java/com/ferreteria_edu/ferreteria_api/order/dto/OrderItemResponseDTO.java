package com.ferreteria_edu.ferreteria_api.order.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemResponseDTO {

    private Long id;
    private Long variantId;

    private BigDecimal purchasePrice;

    private String productName;

    private String measure;

    private Integer quantity;

    private BigDecimal unitPrice;

    private BigDecimal subtotal;

    private BigDecimal finalPrice;

    private BigDecimal discountApplied;
}
