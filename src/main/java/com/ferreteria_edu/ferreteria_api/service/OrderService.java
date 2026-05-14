package com.ferreteria_edu.ferreteria_api.service;

import com.ferreteria_edu.ferreteria_api.dto.orderDto.*;
import com.ferreteria_edu.ferreteria_api.enun.PaymentMethod;
import com.ferreteria_edu.ferreteria_api.exception.InsufficientStockException;
import com.ferreteria_edu.ferreteria_api.exception.ResourceNotFoundException;
import com.ferreteria_edu.ferreteria_api.mapper.OrderItemMapper;
import com.ferreteria_edu.ferreteria_api.mapper.OrderMapper;
import com.ferreteria_edu.ferreteria_api.model.Order;
import com.ferreteria_edu.ferreteria_api.model.OrderItem;
import com.ferreteria_edu.ferreteria_api.model.Product;
import com.ferreteria_edu.ferreteria_api.repository.OrderRepository;
import com.ferreteria_edu.ferreteria_api.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository repository;
    private final ProductRepository productRepository;
    private final PriceCalculatorService priceCalculator;

    // ----------------- CREAR PEDIDO -----------------
    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO request) {
        return buildOrder(request, true);
    }

    // ----------------- CALCULAR PRESUPUESTO -----------------
    public OrderResponseDTO calculateOrder(OrderRequestDTO request) {
        return buildOrder(request, false);
    }

    // ----------------- MÉTODOS CRUD -----------------
    public OrderResponseDTO getOrderById(Long id) {
        Order order = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró orden con ID: " + id));
        return OrderMapper.toResponse(order);
    }

    public List<OrderResponseDTO> getAllOrders() {
        return repository.findAll()
                .stream()
                .map(OrderMapper::toResponse)
                .toList();
    }

    @Transactional
    public void deleteOrder(Long id) {
        Order order = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada con ID: " + id));
        repository.delete(order);
    }

    @Transactional
    public OrderResponseDTO addItem(Long orderId, OrderItemDTO dto) {
        Order order = repository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        if (product.getStock() < dto.getQuantity()) {
            throw new InsufficientStockException("Stock insuficiente");
        }

        product.setStock(product.getStock() - dto.getQuantity());
        productRepository.save(product);

        OrderItem item = OrderItemMapper.toEntity(dto);

        order.addItem(item);
        order.calculateSubtotal();

        return OrderMapper.toResponse(repository.save(order));
    }

    @Transactional
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

        order.calculateSubtotal();

        return OrderMapper.toResponse(repository.save(order));
    }

    @Transactional
    public OrderResponseDTO deleteItem(Long orderId, Long itemId) {
        Order order = repository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        boolean removed = order.getItems().removeIf(i -> i.getId().equals(itemId));

        if (!removed) throw new ResourceNotFoundException("Item not found");

        order.calculateSubtotal();

        return OrderMapper.toResponse(repository.save(order));
    }

    // ----------------- MÉTODO PRIVADO PARA ARMAR ORDEN -----------------
    private OrderResponseDTO buildOrder(OrderRequestDTO request, boolean persist) {


        System.out.println("ORDER OBJECT: " + request);
        PaymentMethod paymentMethod = request.getPaymentMethod() != null
                ? request.getPaymentMethod()
                : PaymentMethod.EFECTIVO;

        BigDecimal discountPercent = request.getDiscount() != null
                ? request.getDiscount()
                : BigDecimal.ZERO;

        // Recargo solo si es tarjeta
        BigDecimal surchargePercent = paymentMethod == PaymentMethod.TARJETA
                ? BigDecimal.valueOf(10)
                : BigDecimal.ZERO;

        List<OrderItemResponseDTO> items = new ArrayList<>();
        BigDecimal orderSubTotal = BigDecimal.ZERO;
        BigDecimal orderTotal = BigDecimal.ZERO;

        Order order = persist ? new Order() : null;
        if (persist) order.setPaymentMethod(paymentMethod);

        for (OrderItemRequestDTO itemReq : request.getItems()) {

            System.out.println("DEBUG ITEM: " + itemReq);

            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

            if (persist && product.getStock() < itemReq.getQuantity()) {
                throw new InsufficientStockException("Stock insuficiente para: " + product.getName());
            }

            // ✅ Calculamos el precio base y final usando PriceCalculatorService
            BigDecimal unitPrice = priceCalculator.calculateSalePrice(product, surchargePercent);

            BigDecimal finalPrice = priceCalculator.calculateFinalPrice(
                         unitPrice, List.of(discountPercent));

            BigDecimal subTotal = priceCalculator.calculateSubtotal(finalPrice, itemReq.getQuantity());
            BigDecimal discountApplied = priceCalculator.calculateDiscountApplied(unitPrice, finalPrice);
            BigDecimal totalFinal= priceCalculator.calculateSubtotal(finalPrice, itemReq.getQuantity());

            // DTO de respuesta
            OrderItemResponseDTO itemDTO = new OrderItemResponseDTO();
            itemDTO.setProductId(product.getId());
            itemDTO.setProductName(product.getName());
            itemDTO.setQuantity(itemReq.getQuantity());
            itemDTO.setUnitePrice(unitPrice);
            itemDTO.setFinalPrice(finalPrice);

            itemDTO.setDiscountApplied(discountApplied);
            itemDTO.setSubtotal(subTotal);

            items.add(itemDTO);
            orderSubTotal=orderSubTotal.add(priceCalculator.calculateSubtotal(unitPrice, itemReq.getQuantity()));
            orderTotal = orderTotal.add(totalFinal);

            // Guardar item si es venta real
            if (persist) {
                product.setStock(product.getStock() - itemReq.getQuantity());
                productRepository.save(product);

                OrderItem item = new OrderItem();
                item.setProductId(product.getId());
                item.setProductName(product.getName());
                item.setQuantity(itemReq.getQuantity());
                item.setFinalPrice(finalPrice);
                item.setUnit_price(unitPrice);
                item.setDiscountApplied(discountApplied);
                item.setSubtotal(subTotal);
                item.setOrder(order);
                order.addItem(item);
            }
        }

        // Respuesta final
        OrderResponseDTO response = new OrderResponseDTO();
        response.setItems(items);
        response.setTotalAmount(orderTotal);
        response.setPaymentMethod(paymentMethod);
        response.setCreatedAt(LocalDateTime.now());
        response.setSubTotal(orderSubTotal);
        response.setTotalDiscount(orderSubTotal.subtract(orderTotal));

        // Persistir orden si corresponde
        if (persist) {
            order.setSubtotal(orderSubTotal);
            order.setTotalAmount(orderTotal);
            repository.save(order);
        }

        return response;
    }
}