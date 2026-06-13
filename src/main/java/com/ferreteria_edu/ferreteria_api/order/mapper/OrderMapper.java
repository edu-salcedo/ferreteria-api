package com.ferreteria_edu.ferreteria_api.order.mapper;

import com.ferreteria_edu.ferreteria_api.order.dto.OrderResponseDTO;
import com.ferreteria_edu.ferreteria_api.order.entity.Order;
import com.ferreteria_edu.ferreteria_api.product.repository.ProductRepository;

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
        dto.setTotalAmount(order.getTotalAmount());
        dto.setPaymentMethod(order.getPaymentMethod());

        dto.setItems(
                order.getItems()
                        .stream()
                        .map(item -> {
                            Long productId = item.getProductId();
                            String name = "Producto desconocido";

                            if (productId != null && productRepository != null) {
                                name = productRepository.findById(productId)
                                        .map(p -> p.getName())
                                        .orElse("Producto " + productId);
                            }

                            return OrderItemMapper.toResponse(item);
                        })
                        .collect(Collectors.toList())
        );

        return dto;
    }
}

