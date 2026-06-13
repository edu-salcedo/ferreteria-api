package com.ferreteria_edu.ferreteria_api.order.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemDTO {

    private Long productId;
    private Integer quantity;
    private BigDecimal unitPrice;
}
