package com.ferreteria_edu.ferreteria_api.mapper;

import com.ferreteria_edu.ferreteria_api.dto.orderDto.OrderResponseDTO;
import com.ferreteria_edu.ferreteria_api.model.Order;

import java.util.stream.Collectors;

public class OrderMapper {
    public static OrderResponseDTO toResponse(Order order) {
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setId(order.getId());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setItems(
                order.getItems()
                        .stream()
                        .map(OrderItemMapper::toResponse)
                        .collect(Collectors.toList())
        );
        return dto;
    }
}
