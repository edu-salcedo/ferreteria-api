package com.ferreteria_edu.ferreteria_api.dto.orderDto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
@Data
public class OrderResponseDTO {

    private Long id;
    private LocalDateTime createdAt;
    private BigDecimal totalAmount;
    private List<OrderItemResponseDTO> items;
}
