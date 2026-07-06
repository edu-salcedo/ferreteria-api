package com.ferreteria_edu.ferreteria_api.order.mapper;

import com.ferreteria_edu.ferreteria_api.order.dto.OrderItemResponseDTO;
import com.ferreteria_edu.ferreteria_api.order.dto.OrderResponseDTO;
import com.ferreteria_edu.ferreteria_api.order.entity.Order;
import com.ferreteria_edu.ferreteria_api.order.entity.OrderItem;
import com.ferreteria_edu.ferreteria_api.product.repository.ProductRepository;

import java.util.List;
import java.util.stream.Collectors;


public class OrderMapper {

    private static ProductRepository productRepository;

    public static void setProductRepository(ProductRepository repo) {
        productRepository = repo;
    }

    public static OrderResponseDTO toResponse(Order order) {

        OrderResponseDTO dto = new OrderResponseDTO();

        dto.setId(order.getId());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setSubTotal(order.getSubtotal());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setPaymentMethod(order.getPaymentMethod());

        List<OrderItemResponseDTO> items = order.getItems()
                .stream()
                .map(OrderMapper::mapItem)
                .toList();

        dto.setItems(items);

        dto.setTotalDiscount(
                order.getSubtotal().subtract(order.getTotalAmount())
        );

        return dto;
    }

    private static OrderItemResponseDTO mapItem(OrderItem item) {

        OrderItemResponseDTO dto = new OrderItemResponseDTO();

        dto.setVariantId(item.getVariant().getId());
        dto.setProductName(item.getProductName());
        dto.setMeasure(item.getMeasure());
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setSubtotal(item.getSubtotal());

        return dto;
    }
}

