package com.ferreteria_edu.ferreteria_api.controller;

import com.ferreteria_edu.ferreteria_api.dto.orderDto.OrderDTO;
import com.ferreteria_edu.ferreteria_api.dto.orderDto.OrderItemDTO;
import com.ferreteria_edu.ferreteria_api.dto.orderDto.OrderResponseDTO;
import com.ferreteria_edu.ferreteria_api.service.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
@CrossOrigin(origins = "http://localhost:5173")
@RestController
@AllArgsConstructor
@RequestMapping("order")
public class OrderController {
    private final OrderService service;

    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(@RequestBody OrderDTO dto) {
        return ResponseEntity.ok(service.createOrder(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getOrderById(id));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponseDTO>> getAllOrders() {
        return ResponseEntity.ok(service.getAllOrders());
    }

    @PostMapping("/{orderId}/items")
    public ResponseEntity<OrderResponseDTO> addItem(
            @PathVariable Long orderId,
            @RequestBody OrderItemDTO dto) {
        return ResponseEntity.ok(service.addItem(orderId, dto));
    }

    @PutMapping("/{orderId}/items/{itemId}")
    public ResponseEntity<OrderResponseDTO> updateItem(
            @PathVariable Long orderId,
            @PathVariable Long itemId,
            @RequestBody OrderItemDTO dto) {
        return ResponseEntity.ok(service.updateItem(orderId, itemId, dto));
    }

    @DeleteMapping("/{orderId}/items/{itemId}")
    public ResponseEntity<OrderResponseDTO> deleteItem(
            @PathVariable Long orderId,
            @PathVariable Long itemId) {
        return ResponseEntity.ok(service.deleteItem(orderId, itemId));
    }
}
