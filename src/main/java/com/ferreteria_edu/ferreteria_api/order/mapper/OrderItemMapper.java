package com.ferreteria_edu.ferreteria_api.order.mapper;

import com.ferreteria_edu.ferreteria_api.order.dto.OrderItemRequestDTO;
import com.ferreteria_edu.ferreteria_api.order.dto.OrderItemResponseDTO;
import com.ferreteria_edu.ferreteria_api.order.entity.Order;
import com.ferreteria_edu.ferreteria_api.order.entity.OrderItem;
import com.ferreteria_edu.ferreteria_api.product.entity.ProductVariant;

import java.math.BigDecimal;

public class OrderItemMapper {

    public static OrderItem toEntity(OrderItemRequestDTO dto, ProductVariant variant, Order order,
            BigDecimal finalPrice, BigDecimal discountApplied, BigDecimal subtotal) {

        OrderItem item = new OrderItem();

        item.setOrder(order);
        item.setVariant(variant);
        item.setProductName(variant.getProduct().getName());
        item.setMeasure(variant.getMeasure());
        item.setPurchasePrice(variant.getPurchasePrice());
        item.setQuantity(dto.getQuantity());
        item.setUnitPrice(variant.getSalePrice());
        item.setFinalPrice(finalPrice);
        item.setDiscountApplied(discountApplied);
        item.setSubtotal(subtotal);
        return item;
    }

    public static OrderItemResponseDTO toResponse(OrderItem item) {

        OrderItemResponseDTO dto = new OrderItemResponseDTO();

        dto.setId(item.getId());
        dto.setVariantId(item.getVariant() != null ? item.getVariant().getId() : null);
        dto.setProductName(item.getProductName());
        dto.setMeasure(item.getMeasure());
        dto.setPurchasePrice(item.getPurchasePrice());
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setFinalPrice(item.getFinalPrice());
        dto.setDiscountApplied(item.getDiscountApplied());
        dto.setSubtotal(item.getSubtotal());
        return dto;
    }
}
