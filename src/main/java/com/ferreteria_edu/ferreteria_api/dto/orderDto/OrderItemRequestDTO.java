package com.ferreteria_edu.ferreteria_api.dto.orderDto;

import lombok.Data;

@Data
public class OrderItemRequestDTO {
    private Long productId;
    private Integer quantity;
}
