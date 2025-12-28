package com.ferreteria_edu.ferreteria_api.service;

import com.ferreteria_edu.ferreteria_api.dto.orderDto.OrderDTO;
import com.ferreteria_edu.ferreteria_api.dto.orderDto.OrderItemDTO;
import com.ferreteria_edu.ferreteria_api.dto.orderDto.OrderResponseDTO;
import com.ferreteria_edu.ferreteria_api.exception.ResourceNotFoundException;
import com.ferreteria_edu.ferreteria_api.mapper.OrderItemMapper;
import com.ferreteria_edu.ferreteria_api.mapper.OrderMapper;
import com.ferreteria_edu.ferreteria_api.model.Order;
import com.ferreteria_edu.ferreteria_api.model.OrderItem;
import com.ferreteria_edu.ferreteria_api.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository repository;

    public OrderResponseDTO createOrder(OrderDTO dto) {
        Order order = new Order();

        if (dto.getItems() != null) {
            for (OrderItemDTO itemDTO : dto.getItems()) {
                OrderItem item = OrderItemMapper.toEntity(itemDTO);
                order.addItem(item);
            }
        }

        order.calculateTotal();
        Order saved = repository.save(order);
        return OrderMapper.toResponse(saved);
    }



    public OrderResponseDTO getOrderById(Long id) {
        Order order= repository.findById(id)
                .orElseThrow(() -> new RuntimeException("no se encontro orden con ID: " + id));
        return OrderMapper.toResponse(order);
    }


    public List<OrderResponseDTO> getAllOrders() {
        return repository.findAll()
                .stream()
                .map(OrderMapper::toResponse)
                .toList();
    }


    public void deleteOrder(Long id) {
        Order order = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada con ID: " + id));
        repository.delete(order);
    }


    public OrderResponseDTO addItem(Long orderId, OrderItemDTO dto) {
        Order order = repository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        OrderItem item = OrderItemMapper.toEntity(dto);
        order.addItem(item);

        return OrderMapper.toResponse(repository.save(order));
    }

    public OrderResponseDTO updateItem(Long orderId, Long itemId, OrderItemDTO dto) {
        Order order = repository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        OrderItem item = order.getItems()
                .stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        item.setProductId(dto.getProductId());
        item.setQuantity(dto.getQuantity());
        item.setUnitPrice(dto.getUnitPrice());

        order.calculateTotal();

        return OrderMapper.toResponse(repository.save(order));
    }

    public OrderResponseDTO deleteItem(Long orderId, Long itemId) {
        Order order = repository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        boolean removed = order.getItems().removeIf(i -> i.getId().equals(itemId));
        if (!removed) {
            throw new ResourceNotFoundException("Item not found");
        }

        order.calculateTotal();

        return OrderMapper.toResponse(repository.save(order));
    }
}
