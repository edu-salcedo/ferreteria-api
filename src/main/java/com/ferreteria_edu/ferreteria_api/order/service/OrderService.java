package com.ferreteria_edu.ferreteria_api.order.service;

import com.ferreteria_edu.ferreteria_api.enun.PaymentMethod;
import com.ferreteria_edu.ferreteria_api.exception.InsufficientStockException;
import com.ferreteria_edu.ferreteria_api.exception.ResourceNotFoundException;

import com.ferreteria_edu.ferreteria_api.order.mapper.OrderMapper;

import com.ferreteria_edu.ferreteria_api.order.dto.OrderItemRequestDTO;
import com.ferreteria_edu.ferreteria_api.order.dto.OrderItemResponseDTO;
import com.ferreteria_edu.ferreteria_api.order.dto.OrderRequestDTO;
import com.ferreteria_edu.ferreteria_api.order.dto.OrderResponseDTO;
import com.ferreteria_edu.ferreteria_api.order.entity.Order;
import com.ferreteria_edu.ferreteria_api.order.entity.OrderItem;

import com.ferreteria_edu.ferreteria_api.order.repository.OrderRepository;
import com.ferreteria_edu.ferreteria_api.product.entity.ProductVariant;
import com.ferreteria_edu.ferreteria_api.product.repository.ProductRepository;
import com.ferreteria_edu.ferreteria_api.product.repository.ProductVariantRepository;
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

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final PriceCalculatorService priceCalculator;
    private final ProductVariantRepository variantRepository;

    // ----------------- CREAR PEDIDO -----------------
    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO dto) {

        Order order = new Order();
        order.setCreatedAt(LocalDateTime.now());
        order.setPaymentMethod(dto.getPaymentMethod());

        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequestDTO itemDTO : dto.getItems()) {

            ProductVariant variant = variantRepository
                    .findById(itemDTO.getVariantId())
                    .orElseThrow(() ->
                            new RuntimeException("Variante no encontrada"));

            if (variant.getStock() < itemDTO.getQuantity()) {
                throw new RuntimeException(
                        "Stock insuficiente: " + variant.getMeasure()
                );
            }

            // descontar stock
            variant.setStock(
                    variant.getStock() - itemDTO.getQuantity()
            );

            BigDecimal unitPrice = variant.getSalePrice();

            BigDecimal subtotal =
                    unitPrice.multiply(
                            BigDecimal.valueOf(itemDTO.getQuantity())
                    );

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setVariant(variant);
            item.setProductName(variant.getProduct().getName());
            item.setMeasure(variant.getMeasure());
            item.setQuantity(itemDTO.getQuantity());
            item.setUnitPrice(unitPrice);
            item.setSubtotal(subtotal);

            order.addItem(item);

            total = total.add(subtotal);
        }

        // aplicar descuento / recargo
        if (dto.getDiscount() != null) {
            total = total.subtract(
                    total.multiply(dto.getDiscount())
                            .divide(BigDecimal.valueOf(100))
            );
        }

        if (dto.getSurcharge() != null) {
            total = total.add(
                    total.multiply(dto.getSurcharge())
                            .divide(BigDecimal.valueOf(100))
            );
        }

        order.setTotalAmount(total);
        order.calculateSubtotal();

        Order saved = orderRepository.save(order);

        return OrderMapper.toResponse(saved);
    }



    // ----------------- CALCULAR PRESUPUESTO -----------------
    public OrderResponseDTO calculateOrder(OrderRequestDTO request) {
        return buildOrder(request, false);
    }


    // ----------------- MÉTODOS CRUD -----------------
    public OrderResponseDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró orden con ID: " + id));
        return OrderMapper.toResponse(order);
    }

    public List<OrderResponseDTO> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(OrderMapper::toResponse)
                .toList();
    }

   /* @Transactional
    public OrderResponseDTO addItem(Long orderId, OrderItemDTO dto) {
        Order order = orderRepository.findById(orderId)
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
*/
    @Transactional
    public OrderResponseDTO updateOrder(Long orderId, OrderRequestDTO dto) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada"));

        // devolver stock anterior
        for (OrderItem old : order.getItems()) {
            ProductVariant variant = old.getVariant();
            variant.setStock(variant.getStock() + old.getQuantity());
        }

        order.getItems().clear();

        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemRequestDTO itemDTO : dto.getItems()) {

            ProductVariant variant = variantRepository.findById(itemDTO.getVariantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Variante no encontrada"));

            if (variant.getStock() < itemDTO.getQuantity()) {
                throw new InsufficientStockException(
                        "Stock insuficiente: " + variant.getMeasure()
                );
            }

            variant.setStock(variant.getStock() - itemDTO.getQuantity());

            BigDecimal unitPrice = variant.getSalePrice();
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(itemDTO.getQuantity()));
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setVariant(variant);
            item.setProductName(variant.getProduct().getName());
            item.setMeasure(variant.getMeasure());
            item.setQuantity(itemDTO.getQuantity());
            item.setUnitPrice(unitPrice);
            item.setSubtotal(subtotal);

            order.addItem(item);

            total = total.add(subtotal);
        }
        order.setSubtotal(total);
        order.setTotalAmount(total);
        order.setPaymentMethod(dto.getPaymentMethod());

        return OrderMapper.toResponse(orderRepository.save(order));
    }



            @Transactional
    public OrderResponseDTO deleteItem(Long orderId, Long itemId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        boolean removed = order.getItems().removeIf(i -> i.getId().equals(itemId));

        if (!removed) throw new ResourceNotFoundException("Item not found");

        order.calculateSubtotal();

        return OrderMapper.toResponse(orderRepository.save(order));
    }

    @Transactional
    public void deleteOrder(Long id) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada"));

        // devolver stock
        for (OrderItem item : order.getItems()) {
            ProductVariant variant = item.getVariant();
            variant.setStock(variant.getStock() + item.getQuantity());
        }

        orderRepository.delete(order);
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

            // 🔥 CAMBIO CLAVE: VARIANT NO PRODUCT
            ProductVariant variant = variantRepository.findById(itemReq.getVariantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Variante no encontrada"));

            if (persist && variant.getStock() < itemReq.getQuantity()) {
                throw new InsufficientStockException(
                        "Stock insuficiente para: " + variant.getMeasure()
                );
            }

            // ===============================
            // PRECIOS BASADOS EN VARIANTE
            // ===============================

            BigDecimal unitPrice = variant.getSalePrice();

            BigDecimal finalPrice = unitPrice.subtract(
                    unitPrice.multiply(discountPercent)
                            .divide(BigDecimal.valueOf(100))
            );

            BigDecimal subTotal = finalPrice.multiply(
                    BigDecimal.valueOf(itemReq.getQuantity())
            );

            BigDecimal discountApplied = unitPrice.subtract(finalPrice);

            // ===============================
            // RESPONSE DTO
            // ===============================

            OrderItemResponseDTO itemDTO = new OrderItemResponseDTO();
            itemDTO.setVariantId(variant.getId());
            itemDTO.setProductName(variant.getProduct().getName());
            itemDTO.setMeasure(variant.getMeasure());
            itemDTO.setQuantity(itemReq.getQuantity());
            itemDTO.setUnitPrice(unitPrice);
            itemDTO.setSubtotal(subTotal);

            items.add(itemDTO);

            orderSubTotal = orderSubTotal.add(unitPrice.multiply(
                    BigDecimal.valueOf(itemReq.getQuantity())
            ));

            orderTotal = orderTotal.add(subTotal);

            // ===============================
            // PERSISTENCIA
            // ===============================

            if (persist) {

                variant.setStock(
                        variant.getStock() - itemReq.getQuantity()
                );

                variantRepository.save(variant);

                OrderItem item = new OrderItem();
                item.setVariant(variant);
                item.setProductName(variant.getProduct().getName());
                item.setMeasure(variant.getMeasure());
                item.setQuantity(itemReq.getQuantity());
                item.setUnitPrice(unitPrice);
                item.setSubtotal(subTotal);
                item.setOrder(order);

                order.addItem(item);
            }
        }

        // ===============================
        // RESPONSE FINAL
        // ===============================

        OrderResponseDTO response = new OrderResponseDTO();
        response.setItems(items);
        response.setPaymentMethod(paymentMethod);
        response.setCreatedAt(LocalDateTime.now());
        response.setSubTotal(orderSubTotal);
        response.setTotalAmount(orderTotal);
        response.setTotalDiscount(orderSubTotal.subtract(orderTotal));

        // ===============================
        // SAVE ORDER
        // ===============================

        if (persist) {
            order.setSubtotal(orderSubTotal);
            order.setTotalAmount(orderTotal);
            orderRepository.save(order);
        }

        return response;
    }
}