package com.ferreteria_edu.ferreteria_api.service;

import com.ferreteria_edu.ferreteria_api.model.Product;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class PriceCalculatorService {

    // Calcula la ganancia de un producto
    public BigDecimal calculateProfit(Product p) {
        if (p.getProfitMargin() == null) return BigDecimal.ZERO;
        return p.getPurchasePrice()
                .multiply(p.getProfitMargin())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    // Precio de venta base (purchasePrice + ganancia)
    public BigDecimal calculateSalePrice(Product p, BigDecimal surchargePercent) {
        BigDecimal basePrice = p.getPurchasePrice().add(calculateProfit(p));
        BigDecimal fifty = new BigDecimal("50");
        BigDecimal rounded = basePrice
                .divide(fifty, 0, RoundingMode.UP)
                .multiply(fifty);

        System.out.println(rounded);
        if (surchargePercent != null && surchargePercent.compareTo(BigDecimal.ZERO) != 0) {

            rounded = rounded.add(rounded.multiply(surchargePercent).divide(BigDecimal.valueOf(100)));
        }
        return rounded;
    }

    // Aplica recargos y descuentos
    public BigDecimal calculateFinalPrice(BigDecimal basePrice, List<BigDecimal> discountsPercent) {
        BigDecimal price = basePrice;

        // descuentos
        if (discountsPercent != null) {
            for (BigDecimal discount : discountsPercent) {
                price = price.subtract(price.multiply(discount).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
            }
        }

        return price.setScale(2, RoundingMode.HALF_UP);
    }

    // Subtotal = precio final * cantidad
    public BigDecimal calculateSubtotal(BigDecimal finalPrice, int quantity) {
        return finalPrice.multiply(BigDecimal.valueOf(quantity));
    }

    // % de descuento real aplicado
    public BigDecimal calculateDiscountApplied(BigDecimal basePrice, BigDecimal finalPrice) {
        if (basePrice.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return basePrice.subtract(finalPrice)
                .multiply(BigDecimal.valueOf(100))
                .divide(basePrice, 2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateFinalPrice(BigDecimal basePrice, BigDecimal discountPercent) {
        return calculateFinalPrice(basePrice, List.of(discountPercent));
    }
}