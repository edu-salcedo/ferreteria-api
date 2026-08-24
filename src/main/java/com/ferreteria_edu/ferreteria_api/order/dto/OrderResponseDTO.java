package com.ferreteria_edu.ferreteria_api.order.dto;

import com.ferreteria_edu.ferreteria_api.enun.DocumentType;
import com.ferreteria_edu.ferreteria_api.enun.OrderType;
import com.ferreteria_edu.ferreteria_api.enun.PaymentMethod;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponseDTO {

    private Long id;
    private LocalDateTime createdAt;
    private List<OrderItemResponseDTO> items;
    private PaymentMethod paymentMethod;
    private BigDecimal subTotal;
    private BigDecimal totalAmount;
    private BigDecimal totalSurcharge;
    private BigDecimal totalDiscount;
    private boolean invoice;
    private BigDecimal invoiceAmount;
    
    private OrderType orderType;
    private DocumentType documentType;
    private Integer posNumber;
    private Long invoiceNumber;
    private String invoiceType;
}
