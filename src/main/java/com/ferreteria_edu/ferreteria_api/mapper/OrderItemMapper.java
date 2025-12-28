package com.ferreteria_edu.ferreteria_api.mapper;

import com.ferreteria_edu.ferreteria_api.dto.orderDto.OrderItemDTO;
import com.ferreteria_edu.ferreteria_api.dto.orderDto.OrderItemResponseDTO;
import com.ferreteria_edu.ferreteria_api.model.OrderItem;

public class OrderItemMapper {

    public static OrderItem toEntity(OrderItemDTO dto) {
        OrderItem item = new OrderItem();
        item.setProductId(dto.getProductId());
        item.setQuantity(dto.getQuantity());
        item.setUnitPrice(dto.getUnitPrice());
        return item;
    }

    public static OrderItemResponseDTO toResponse(OrderItem item) {
        OrderItemResponseDTO dto = new OrderItemResponseDTO();
        dto.setId(item.getId());
        dto.setProductId(item.getProductId());
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setSubtotal(item.getSubtotal());
        return dto;
    }
}
