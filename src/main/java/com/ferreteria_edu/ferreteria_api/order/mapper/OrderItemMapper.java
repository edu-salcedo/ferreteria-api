package com.ferreteria_edu.ferreteria_api.order.mapper;

import com.ferreteria_edu.ferreteria_api.order.dto.OrderItemDTO;
import com.ferreteria_edu.ferreteria_api.order.dto.OrderItemRequestDTO;
import com.ferreteria_edu.ferreteria_api.order.dto.OrderItemResponseDTO;
import com.ferreteria_edu.ferreteria_api.order.entity.Order;
import com.ferreteria_edu.ferreteria_api.order.entity.OrderItem;
import com.ferreteria_edu.ferreteria_api.product.entity.ProductVariant;

import java.math.BigDecimal;

public class OrderItemMapper {

    public static OrderItem toEntity(OrderItemRequestDTO dto, ProductVariant variant, Order order) {

        OrderItem item = new OrderItem();

        item.setOrder(order);
        item.setVariant(variant);
        item.setProductName(variant.getProduct().getName());
        item.setMeasure(variant.getMeasure());

        item.setQuantity(dto.getQuantity());

        item.setUnitPrice(variant.getSalePrice());
        item.setSubtotal(
                variant.getSalePrice()
                        .multiply(BigDecimal.valueOf(dto.getQuantity()))
        );

        return item;
    }

    public static OrderItemResponseDTO toResponse(OrderItem item) {

        OrderItemResponseDTO dto = new OrderItemResponseDTO();

        dto.setId(item.getId());
        dto.setVariantId(item.getVariant().getId());
        dto.setProductName(item.getProductName());
        dto.setMeasure(item.getMeasure());
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setSubtotal(item.getSubtotal());

        return dto;
    }
}
