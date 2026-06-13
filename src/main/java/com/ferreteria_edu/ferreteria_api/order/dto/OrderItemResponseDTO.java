package com.ferreteria_edu.ferreteria_api.order.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemResponseDTO {
    private Long id;
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal basePrice;
    private BigDecimal unitePrice;
    private BigDecimal finalPrice;
    private BigDecimal discountApplied;
    private BigDecimal subtotal;
}
