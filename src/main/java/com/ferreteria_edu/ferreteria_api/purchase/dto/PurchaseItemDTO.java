package com.ferreteria_edu.ferreteria_api.purchase.dto;

import lombok.Data;

import java.math.BigDecimal;
@Data
public class PurchaseItemDTO {
    private Long variantId;
    private Integer quantity;
    private BigDecimal unitCost;
}
