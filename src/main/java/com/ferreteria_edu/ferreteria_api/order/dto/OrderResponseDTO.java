package com.ferreteria_edu.ferreteria_api.order.dto;

import com.ferreteria_edu.ferreteria_api.enun.PaymentMethod;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponseDTO {

    private Long id;
    private LocalDateTime createdAt;
    private BigDecimal subTotal;
    private BigDecimal totalAmount;
    private List<OrderItemResponseDTO> items;
    private PaymentMethod paymentMethod;
    private BigDecimal totalDiscount;
    private BigDecimal totalSurcharge;
    private boolean invoice;
}
