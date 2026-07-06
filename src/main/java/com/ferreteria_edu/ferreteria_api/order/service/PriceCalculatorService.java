package com.ferreteria_edu.ferreteria_api.order.service;

import com.ferreteria_edu.ferreteria_api.product.entity.Product;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service

public class PriceCalculatorService {

    // =========================
    // GANANCIA POR VARIANTE
    // =========================
    public BigDecimal calculateProfit(BigDecimal purchasePrice, BigDecimal margin) {

        if (purchasePrice == null || margin == null) {
            return BigDecimal.ZERO;
        }

        return purchasePrice
                .multiply(margin)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    // =========================
    // PRECIO BASE (SIN REDONDEO)
    // =========================
    public BigDecimal calculateBasePrice(BigDecimal purchasePrice, BigDecimal profit) {
        return purchasePrice.add(profit);
    }

    // =========================
    // DESCUENTOS
    // =========================
    public BigDecimal applyDiscounts(BigDecimal price, List<BigDecimal> discounts) {

        BigDecimal result = price;

        if (discounts != null) {
            for (BigDecimal d : discounts) {
                result = result.subtract(
                        result.multiply(d)
                                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                );
            }
        }

        return result;
    }

    // =========================
    // RECARGO
    // =========================
    public BigDecimal applySurcharge(BigDecimal price, BigDecimal surchargePercent) {

        if (surchargePercent == null || surchargePercent.compareTo(BigDecimal.ZERO) == 0) {
            return price;
        }

        return price.add(
                price.multiply(surchargePercent)
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
        );
    }

    // =========================
    // SUBTOTAL
    // =========================
    public BigDecimal calculateSubtotal(BigDecimal price, int quantity) {
        return price.multiply(BigDecimal.valueOf(quantity));
    }

    // =========================
    // DESCUENTO REAL
    // =========================
    public BigDecimal calculateDiscountApplied(BigDecimal base, BigDecimal finalPrice) {

        if (base.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;

        return base.subtract(finalPrice)
                .multiply(BigDecimal.valueOf(100))
                .divide(base, 2, RoundingMode.HALF_UP);
    }
}