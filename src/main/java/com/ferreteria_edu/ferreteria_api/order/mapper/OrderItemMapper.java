package com.ferreteria_edu.ferreteria_api.order.mapper;

import com.ferreteria_edu.ferreteria_api.order.dto.OrderItemDTO;
import com.ferreteria_edu.ferreteria_api.order.dto.OrderItemResponseDTO;
import com.ferreteria_edu.ferreteria_api.order.entity.OrderItem;

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
        dto.setBasePrice(item.getBasePrice());
        dto.setUnitePrice(item.getUnit_price());
        dto.setSubtotal(item.getSubtotal());
        dto.setFinalPrice(item.getFinalPrice());
        dto.setDiscountApplied(item.getDiscountApplied());
        return dto;
    }
}
