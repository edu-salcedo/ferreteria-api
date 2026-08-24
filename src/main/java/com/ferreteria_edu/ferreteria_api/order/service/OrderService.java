package com.ferreteria_edu.ferreteria_api.order.service;

import com.ferreteria_edu.ferreteria_api.enun.DocumentType;
import com.ferreteria_edu.ferreteria_api.enun.OrderType;
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
        private final ProductVariantRepository variantRepository;

        // ----------------- 1. CREAR COMPROBANTE DESDE CERO -----------------
        @Transactional
        public OrderResponseDTO createOrder(OrderRequestDTO dto) {
                return buildOrder(dto, true);
        }

        // ----------------- 2. CALCULAR EN PANTALLA (SIN GUARDAR EN BD)
        // -----------------
        public OrderResponseDTO calculateOrder(OrderRequestDTO request) {
                return buildOrder(request, false);
        }

        // ----------------- 3. CONVERTIR UN PRESUPUESTO EXISTENTE A VENTA REAL
        // -----------------
        @Transactional
        public OrderResponseDTO convertBudgetToSale(Long budgetId, DocumentType targetDocumentType, Integer posNumber,
                        Long invoiceNumber, String invoiceType) {

                Order order = orderRepository.findById(budgetId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "No se encontró el presupuesto con ID: " + budgetId));

                if (!order.isBudget()) {
                        throw new IllegalStateException("La orden con ID " + budgetId
                                        + " no es un presupuesto, es una: " + order.getOrderType());
                }

                // Descontamos stock recién ahora porque pasa a ser una VENTA real
                for (OrderItem item : order.getItems()) {
                        ProductVariant variant = item.getVariant();
                        if (variant.getStock() < item.getQuantity()) {
                                throw new InsufficientStockException(
                                                "Stock insuficiente para concretar la venta. Producto: "
                                                                + variant.getProduct().getName() + " ("
                                                                + variant.getMeasure() + ")");
                        }
                        variant.setStock(variant.getStock() - item.getQuantity());
                        variantRepository.save(variant);
                }

                // Cambiamos el tipo a VENTA y aplicamos el comprobante elegido
                order.setOrderType(OrderType.SALE);
                order.setDocumentType(targetDocumentType);
                order.setCreatedAt(LocalDateTime.now()); // Fecha de la venta real

                // Si eligieron facturar por ARCA en la conversión
                if (DocumentType.INVOICE.equals(targetDocumentType)) {
                        order.setInvoice(true);
                        order.setInvoiceAmount(order.getTotalAmount());
                        order.setPosNumber(posNumber != null ? posNumber : 1);
                        order.setInvoiceNumber(invoiceNumber);
                        order.setInvoiceType(invoiceType != null ? invoiceType : "C_LINEA");
                }

                return OrderMapper.toResponse(orderRepository.save(order));
        }

        // ----------------- 4. MÉTODOS DE LECTURA (CRUD) -----------------
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

        // ----------------- 5. ACTUALIZAR COMPROBANTE EXISTENTE -----------------
        @Transactional
        public OrderResponseDTO updateOrder(Long orderId, OrderRequestDTO dto) {
                Order order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada"));

                // DEVOLVER STOCK ANTERIOR (Solo si no era un presupuesto original)
                if (!order.isBudget()) {
                        for (OrderItem old : order.getItems()) {
                                ProductVariant variant = old.getVariant();
                                variant.setStock(variant.getStock() + old.getQuantity());
                                variantRepository.save(variant);
                        }
                }

                order.getItems().clear();
                BigDecimal total = BigDecimal.ZERO;

                for (OrderItemRequestDTO itemDTO : dto.getItems()) {
                        ProductVariant variant = variantRepository.findById(itemDTO.getVariantId())
                                        .orElseThrow(() -> new ResourceNotFoundException("Variante no encontrada"));

                        // VALIDAR Y RESTAR STOCK NUEVO (Solo si el nuevo estado NO es presupuesto)
                        boolean esNuevoPresupuesto = OrderType.BUDGET.equals(dto.getOrderType());
                        if (!esNuevoPresupuesto) {
                                if (variant.getStock() < itemDTO.getQuantity()) {
                                        throw new InsufficientStockException(
                                                        "Stock insuficiente: " + variant.getMeasure());
                                }
                                variant.setStock(variant.getStock() - itemDTO.getQuantity());
                                variantRepository.save(variant);
                        }

                        BigDecimal unitPrice = variant.getSalePrice() != null ? variant.getSalePrice()
                                        : BigDecimal.ZERO;
                        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(itemDTO.getQuantity()));

                        OrderItem item = new OrderItem();
                        item.setOrder(order);
                        item.setVariant(variant);
                        item.setProductName(variant.getProduct().getName());
                        item.setMeasure(variant.getMeasure());
                        item.setQuantity(itemDTO.getQuantity());
                        item.setUnitPrice(unitPrice);
                        item.setSubtotal(subtotal);
                        item.setPurchasePrice(variant.getPurchasePrice() != null ? variant.getPurchasePrice()
                                        : BigDecimal.ZERO);
                        item.setFinalPrice(unitPrice);
                        item.setDiscountApplied(BigDecimal.ZERO);

                        order.addItem(item);
                        total = total.add(subtotal);
                }

                order.setSubtotal(total);
                order.setTotalAmount(total);
                order.setPaymentMethod(dto.getPaymentMethod());
                order.setOrderType(dto.getOrderType());
                order.setDocumentType(dto.getDocumentType());

                return OrderMapper.toResponse(orderRepository.save(order));
        }

        // ----------------- 6. ELIMINAR COMPROBANTE -----------------
        @Transactional
        public void deleteOrder(Long id) {
                Order order = orderRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada"));

                // DEVOLVER STOCK AL ELIMINAR (Solo si no era presupuesto)
                if (!order.isBudget()) {
                        for (OrderItem item : order.getItems()) {
                                ProductVariant variant = item.getVariant();
                                variant.setStock(variant.getStock() + item.getQuantity());
                                variantRepository.save(variant);
                        }
                }

                orderRepository.delete(order);
        }

        // ----------------- 7. MÉTODO PRIVADO PARA ARMAR COMPROBANTES CON LOGICA ARCA
        // -----------------
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

                        // Seteamos tipos base
                        order.setOrderType(request.getOrderType());
                        order.setDocumentType(request.getDocumentType());

                        order = orderRepository.save(order);
                }

                for (OrderItemRequestDTO itemReq : request.getItems()) {

                        ProductVariant variant = variantRepository.findById(itemReq.getVariantId())
                                        .orElseThrow(() -> new ResourceNotFoundException(
                                                        "Variante no encontrada con ID: " + itemReq.getVariantId()));

                        // REGLA STOCK: Evitamos validar o restar stock si es un presupuesto
                        boolean esPresupuesto = OrderType.BUDGET.equals(request.getOrderType());

                        if (persist && !esPresupuesto && variant.getStock() < itemReq.getQuantity()) {
                                throw new InsufficientStockException(
                                                "Stock insuficiente para: " + variant.getMeasure());
                        }

                        BigDecimal unitPrice = variant.getSalePrice() != null ? variant.getSalePrice()
                                        : BigDecimal.ZERO;

                        // Matemáticas de Descuentos y Recargos
                        BigDecimal discountAmount = unitPrice.multiply(discountPercent)
                                        .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
                        BigDecimal priceWithDiscount = unitPrice.subtract(discountAmount);

                        BigDecimal finalPrice = priceWithDiscount;
                        if (paymentMethod == PaymentMethod.TARJETA) {
                                BigDecimal surchargeAmount = priceWithDiscount.multiply(surchargePercent)
                                                .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
                                finalPrice = priceWithDiscount.add(surchargeAmount);
                        }

                        if (finalPrice == null) {
                                finalPrice = unitPrice;
                        }

                        BigDecimal subTotal = finalPrice.multiply(BigDecimal.valueOf(itemReq.getQuantity()));

                        BigDecimal discountApplied = unitPrice.subtract(finalPrice);
                        if (discountApplied == null || discountApplied.compareTo(BigDecimal.ZERO) < 0) {
                                discountApplied = BigDecimal.ZERO;
                        }

                        // Llenamos el DTO de los items individuales
                        OrderItemResponseDTO itemDTO = new OrderItemResponseDTO();
                        itemDTO.setVariantId(variant.getId());
                        itemDTO.setProductName(variant.getProduct().getName());
                        itemDTO.setPurchasePrice(variant.getPurchasePrice());
                        itemDTO.setMeasure(variant.getMeasure());
                        itemDTO.setQuantity(itemReq.getQuantity());
                        itemDTO.setFinalPrice(finalPrice);
                        itemDTO.setDiscountApplied(discountPercent);
                        itemDTO.setUnitPrice(unitPrice);
                        itemDTO.setSubtotal(subTotal);
                        items.add(itemDTO);

                        orderSubTotal = orderSubTotal
                                        .add(unitPrice.multiply(BigDecimal.valueOf(itemReq.getQuantity())));
                        orderTotal = orderTotal.add(subTotal);

                        if (persist) {
                                // Descontamos del inventario real solo si no es presupuesto
                                if (!esPresupuesto) {
                                        variant.setStock(variant.getStock() - itemReq.getQuantity());
                                        variantRepository.save(variant);
                                }

                                OrderItem item = OrderItemMapper.toEntity(
                                                itemReq, variant, order, finalPrice, discountApplied, subTotal);
                                order.addItem(item);
                        }
                }

                // Preparamos respuesta para el Frontend
                OrderResponseDTO response = new OrderResponseDTO();
                response.setItems(items);
                response.setPaymentMethod(paymentMethod);
                response.setCreatedAt(LocalDateTime.now());
                response.setSubTotal(orderSubTotal);
                response.setTotalAmount(orderTotal);
                response.setTotalSurcharge(surchargePercent);
                response.setInvoice(request.isInvoice());
                response.setOrderType(request.getOrderType());
                response.setDocumentType(request.getDocumentType());
                response.setTotalDiscount(orderSubTotal.subtract(orderTotal).compareTo(BigDecimal.ZERO) > 0
                                ? orderSubTotal.subtract(orderTotal)
                                : BigDecimal.ZERO);

                if (persist) {
                        order.setSubtotal(orderSubTotal);
                        order.setTotalAmount(orderTotal);

                        // LÓGICA ESPECIAL PARA EMISIÓN DE FACTURAS FISCALES ARCA
                        if (order.isArcaInvoice()) {
                                order.setInvoice(true);
                                order.setInvoiceAmount(orderTotal);

                                // Asignamos los datos del punto de venta y número dinámico de ARCA
                                order.setPosNumber(request.getPosNumber() != null ? request.getPosNumber() : 1);
                                order.setInvoiceNumber(request.getInvoiceNumber());
                                order.setInvoiceType(request.getInvoiceType() != null ? request.getInvoiceType()
                                                : "C_LINEA");
                        } else {
                                order.setInvoice(request.isInvoice());
                                if (request.isInvoice()) {
                                        order.setInvoiceAmount(orderTotal);
                                }
                        }

                        orderRepository.save(order);
                        response.setId(order.getId());
                        response.setPosNumber(order.getPosNumber());
                        response.setInvoiceNumber(order.getInvoiceNumber());
                        response.setInvoiceType(order.getInvoiceType());
                }

                return response;
        }

        public Long getLastOrderId() {
                // Buscamos la última entidad completa guardada de forma óptima
                return orderRepository.findFirstByOrderByIdDesc()
                                .map(Order::getId) // Si existe, extraemos su ID
                                .orElse(0L); // Si no existe (DB vacía), devolvemos 0
        }
} // <--- Cierre definitivo de la clase OrderService
