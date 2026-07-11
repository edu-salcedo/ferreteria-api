package com.ferreteria_edu.ferreteria_api.order.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemRequestDTO {
    private Long variantId;
    private Integer quantity;
    private BigDecimal basePrice;
    private BigDecimal finalPrice;
}
