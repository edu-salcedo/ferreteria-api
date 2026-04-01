package com.ferreteria_edu.ferreteria_api.dto.orderDto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemResponseDTO {
    private Long id;
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal salePrice;
    private BigDecimal salePriceFinal;
    private BigDecimal discountApplied;
    private BigDecimal subtotal;
}
