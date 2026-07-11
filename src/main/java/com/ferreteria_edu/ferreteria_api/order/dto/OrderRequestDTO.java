package com.ferreteria_edu.ferreteria_api.order.dto;

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
}
