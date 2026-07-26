package com.ferreteria_edu.ferreteria_api.order.service;

import com.ferreteria_edu.ferreteria_api.product.entity.ProductVariant;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class PriceCalculatorService {

    // Ganancia
    public BigDecimal calculateProfit(ProductVariant variant) {

        if (variant.getProfitMargin() == null
                || variant.getPurchasePrice() == null) {
            return BigDecimal.ZERO;
        }

        return variant.getPurchasePrice()
                .multiply(variant.getProfitMargin())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    // Precio de venta
    public BigDecimal calculateSalePrice(ProductVariant variant) {

        BigDecimal purchase = variant.getPurchasePrice();

        BigDecimal margin = purchase
                .multiply(variant.getProfitMargin())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal salePrice = purchase.add(margin);

        BigDecimal fifty = BigDecimal.valueOf(50);

        return salePrice.divide(fifty, 0, RoundingMode.UP).multiply(fifty);
    }

    // Aplicar descuento
    public BigDecimal applyDiscount(BigDecimal price, BigDecimal discount) {

        if (discount == null)
            return price;

        return price.subtract(
                price.multiply(discount)
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
    }

    // Aplicar recargo
    public BigDecimal applySurcharge(BigDecimal price, BigDecimal surcharge) {

        if (surcharge == null)
            return price;

        return price.add(
                price.multiply(surcharge)
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
    }

    public BigDecimal subtotal(BigDecimal price, Integer quantity) {

        return price.multiply(BigDecimal.valueOf(quantity));
    }
}