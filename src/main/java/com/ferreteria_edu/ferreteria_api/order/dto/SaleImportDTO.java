package com.ferreteria_edu.ferreteria_api.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.ferreteria_edu.ferreteria_api.enun.PaymentMethod;

import lombok.Data;

@Data
public class SaleImportDTO {
    private Integer saleNumber;

    private LocalDateTime createdAt;

    private String productName;

    private String measure;

    private Integer quantity;

    private BigDecimal purchasePrice;

    private BigDecimal salePrice;

    private BigDecimal purchaseTotal;

    private BigDecimal saleTotal;

    private PaymentMethod paymentMethod;

    private boolean invoice;

    private BigDecimal invoiceAmount;

}
