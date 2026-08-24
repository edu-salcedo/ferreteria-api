package com.ferreteria_edu.ferreteria_api.order.controller;

import com.ferreteria_edu.ferreteria_api.enun.DocumentType;
import com.ferreteria_edu.ferreteria_api.order.dto.OrderRequestDTO;
import com.ferreteria_edu.ferreteria_api.order.dto.OrderResponseDTO;
import com.ferreteria_edu.ferreteria_api.order.service.OrderService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@AllArgsConstructor
@RequestMapping("order")
public class OrderController {
    private final OrderService service;

    @PostMapping()
    public ResponseEntity<OrderResponseDTO> create(@RequestBody OrderRequestDTO request) {
        return ResponseEntity.ok(service.createOrder(request));
    }

    @PostMapping("/preview")
    public ResponseEntity<OrderResponseDTO> previewOrder(@RequestBody OrderRequestDTO request) {
        OrderResponseDTO dto = service.calculateOrder(request);
        return ResponseEntity.ok(dto);
    }
    
     @PutMapping("/{id}/convert-to-sale")
    public ResponseEntity<OrderResponseDTO> convertBudgetToSale(
            @PathVariable Long id,
            @RequestParam DocumentType targetDocumentType,
            @RequestParam(required = false) Integer posNumber,
            @RequestParam(required = false) Long invoiceNumber,
            @RequestParam(required = false) String invoiceType) {
        
        OrderResponseDTO response = service.convertBudgetToSale(
                id, targetDocumentType, posNumber, invoiceNumber, invoiceType
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getOrderById(id));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponseDTO>> getAllOrders() {
        return ResponseEntity.ok(service.getAllOrders());
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> updateOrder(@PathVariable Long id, @RequestBody OrderRequestDTO request) {
        return ResponseEntity.ok(service.updateOrder(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {

        service.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/last-id")
    public ResponseEntity<Map<String, Long>> getLastOrderId() {
        Long lastId = service.getLastOrderId();
        return ResponseEntity.ok(java.util.Collections.singletonMap("lastId", lastId));
    }

}
