package com.ferreteria_edu.ferreteria_api.order.dto;

import com.ferreteria_edu.ferreteria_api.enun.DocumentType;
import com.ferreteria_edu.ferreteria_api.enun.OrderType;
import com.ferreteria_edu.ferreteria_api.enun.PaymentMethod;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderRequestDTO {
    private List<OrderItemRequestDTO> items;
    private BigDecimal discount; // %
    private BigDecimal surcharge; // %
    private PaymentMethod paymentMethod;
    private boolean invoice;
    private OrderType orderType; // VENTA o PRESUPUESTO
    private DocumentType documentType; // FACTURA, TIQUE, REMITO, NINGUNO
    private Integer posNumber; // Opcional: El punto de venta (ej: 1)
    private Long invoiceNumber; // Opcional: El nro de factura si lo cargan a mano (ej: 120 o 380)
    private String invoiceType;
}
