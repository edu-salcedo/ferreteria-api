package com.ferreteria_edu.ferreteria_api.order.service;

import com.ferreteria_edu.ferreteria_api.enun.PaymentMethod;
import com.ferreteria_edu.ferreteria_api.exception.InsufficientStockException;
import com.ferreteria_edu.ferreteria_api.exception.ResourceNotFoundException;
import com.ferreteria_edu.ferreteria_api.order.mapper.OrderItemMapper;
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

                return buildOrder(dto, true);
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

        /*
         * @Transactional
         * public OrderResponseDTO addItem(Long orderId, OrderItemDTO dto) {
         * Order order = orderRepository.findById(orderId)
         * .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
         * 
         * Product product = productRepository.findById(dto.getProductId())
         * .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
         * 
         * if (product.getStock() < dto.getQuantity()) {
         * throw new InsufficientStockException("Stock insuficiente");
         * }
         * 
         * product.setStock(product.getStock() - dto.getQuantity());
         * productRepository.save(product);
         * 
         * OrderItem item = OrderItemMapper.toEntity(dto);
         * 
         * order.addItem(item);
         * order.calculateSubtotal();
         * 
         * return OrderMapper.toResponse(repository.save(order));
         * }
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
                                                "Stock insuficiente: " + variant.getMeasure());
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
                        item.setBasePrice(variant.getPurchasePrice() != null ? variant.getPurchasePrice()
                                        : BigDecimal.ZERO);
                        item.setFinalPrice(variant.getSalePrice() != null ? variant.getSalePrice() : BigDecimal.ZERO);
                        BigDecimal discountApplied = item.getUnitPrice().subtract(item.getFinalPrice());
                        item.setDiscountApplied(discountApplied.compareTo(BigDecimal.ZERO) > 0 ? discountApplied
                                        : BigDecimal.ZERO);

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

                if (!removed)
                        throw new ResourceNotFoundException("Item not found");

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

                Order order = null;

                if (persist) {
                        order = new Order();
                        order.setPaymentMethod(paymentMethod);
                        order.setSubtotal(BigDecimal.ZERO);
                        order.setTotalAmount(BigDecimal.ZERO);
                        order.setCreatedAt(LocalDateTime.now());
                        order.setInvoice(request.isInvoice());
                        order.setDiscount(discountPercent);
                        order.setSurcharge(surchargePercent);

                        order = orderRepository.save(order); // Genera ID inicial en la DB
                }

                for (OrderItemRequestDTO itemReq : request.getItems()) {

                        ProductVariant variant = variantRepository.findById(itemReq.getVariantId())
                                        .orElseThrow(() -> new ResourceNotFoundException(
                                                        "Variante no encontrada con ID: " + itemReq.getVariantId()));

                        if (persist && variant.getStock() < itemReq.getQuantity()) {
                                throw new InsufficientStockException(
                                                "Stock insuficiente para: " + variant.getMeasure());
                        }

                        // ===============================================================
                        // CÁLCULO DE PRECIOS
                        BigDecimal unitPrice = variant.getSalePrice() != null ? variant.getSalePrice()
                                        : BigDecimal.ZERO;

                        // Descuento
                        BigDecimal discountAmount = unitPrice.multiply(discountPercent)
                                        .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
                        BigDecimal priceWithDiscount = unitPrice.subtract(discountAmount);

                        // Recargo por tarjeta si aplica
                        BigDecimal finalPrice = priceWithDiscount;
                        if (paymentMethod == PaymentMethod.TARJETA) {
                                BigDecimal surchargeAmount = priceWithDiscount.multiply(surchargePercent)
                                                .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
                                finalPrice = priceWithDiscount.add(surchargeAmount);
                        }

                        // Aseguramos matemáticamente que NUNCA sea nulo antes de ir al mapper
                        if (finalPrice == null) {
                                finalPrice = unitPrice;
                        }

                        BigDecimal subTotal = finalPrice.multiply(BigDecimal.valueOf(itemReq.getQuantity()));

                        BigDecimal discountApplied = unitPrice.subtract(finalPrice);
                        if (discountApplied == null || discountApplied.compareTo(BigDecimal.ZERO) < 0) {
                                discountApplied = BigDecimal.ZERO; // Si dio negativo por recargo, lo seteamos en 0
                        }

                        // ===============================================================
                        // CONSTRUCCIÓN DEL RESPUESTA DTO
                        // ===============================================================
                        OrderItemResponseDTO itemDTO = new OrderItemResponseDTO();
                        itemDTO.setVariantId(variant.getId());
                        itemDTO.setProductName(variant.getProduct().getName());
                        itemDTO.setBasePrice(variant.getPurchasePrice());
                        itemDTO.setMeasure(variant.getMeasure());
                        itemDTO.setQuantity(itemReq.getQuantity());
                        itemDTO.setFinalPrice(finalPrice);
                        itemDTO.setDiscountApplied(discountPercent); // porcentaje de descuento aplicado
                        itemDTO.setUnitPrice(unitPrice);
                        itemDTO.setSubtotal(subTotal);
                        items.add(itemDTO);

                        orderSubTotal = orderSubTotal
                                        .add(unitPrice.multiply(BigDecimal.valueOf(itemReq.getQuantity())));
                        orderTotal = orderTotal.add(subTotal);

                        // ===============================================================
                        // PERSISTENCIA USANDO EL MAPPER CORREGIDO
                        // ===============================================================
                        if (persist) {
                                variant.setStock(variant.getStock() - itemReq.getQuantity());
                                variantRepository.save(variant);
                                OrderItem item = OrderItemMapper.toEntity(
                                                itemReq,
                                                variant,
                                                order,
                                                finalPrice,
                                                discountApplied,
                                                subTotal);

                                order.addItem(item);

                        }
                }

                // RESPONSE DTO FINAL
                OrderResponseDTO response = new OrderResponseDTO();
                response.setItems(items);
                response.setPaymentMethod(paymentMethod);
                response.setCreatedAt(LocalDateTime.now());
                response.setSubTotal(orderSubTotal);
                response.setTotalAmount(orderTotal);
                response.setTotalSurcharge(surchargePercent);
                response.setInvoice(request.isInvoice());
                response.setTotalDiscount(orderSubTotal.subtract(orderTotal).compareTo(BigDecimal.ZERO) > 0
                                ? orderSubTotal.subtract(orderTotal)
                                : BigDecimal.ZERO);

                if (persist) {
                        order.setSubtotal(orderSubTotal);
                        order.setTotalAmount(orderTotal);
                        orderRepository.save(order); // Guarda la orden final y propaga los ítems
                        response.setId(order.getId());
                }

                return response;
        }

}