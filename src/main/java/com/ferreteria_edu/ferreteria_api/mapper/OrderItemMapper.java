package com.ferreteria_edu.ferreteria_api.mapper;

import com.ferreteria_edu.ferreteria_api.dto.orderDto.OrderItemDTO;
import com.ferreteria_edu.ferreteria_api.dto.orderDto.OrderItemResponseDTO;
import com.ferreteria_edu.ferreteria_api.model.OrderItem;

public class OrderItemMapper {

    public static OrderItem toEntity(OrderItemDTO dto) {
        OrderItem item = new OrderItem();
        item.setProductId(dto.getProductId());
        item.setQuantity(dto.getQuantity());
        item.setUnit_price(dto.getUnitPrice());
        return item;
    }

    public static OrderItemResponseDTO toResponse(OrderItem item) {
        OrderItemResponseDTO dto = new OrderItemResponseDTO();

        dto.setId(item.getId());
        dto.setProductId(item.getProductId());
        dto.setProductName(item.getProductName());
        dto.setQuantity(item.getQuantity());
        dto.setUnitePrice(item.getUnit_price());
        dto.setSubtotal(item.getSubtotal());
        dto.setFinalPrice(item.getFinalPrice());
        dto.setDiscountApplied(item.getDiscountApplied());
        return dto;
    }
}
