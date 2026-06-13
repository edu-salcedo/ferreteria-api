package com.ferreteria_edu.ferreteria_api.order.dto;

import com.ferreteria_edu.ferreteria_api.enun.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderDTO {
    private Long id;
    private List<OrderItemDTO> items;
    private PaymentMethod paymentMethod;
    private int discount;
}
