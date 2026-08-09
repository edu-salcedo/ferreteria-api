package com.ferreteria_edu.ferreteria_api.order.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ferreteria_edu.ferreteria_api.order.dto.SaleImportDTO;
import com.ferreteria_edu.ferreteria_api.order.entity.Order;
import com.ferreteria_edu.ferreteria_api.order.entity.OrderItem;
import com.ferreteria_edu.ferreteria_api.order.repository.OrderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class SalesImportService {

    private final OrderRepository orderRepository;

    public void importSales(List<SaleImportDTO> sales) {

        Map<Integer, List<SaleImportDTO>> groupedSales = new LinkedHashMap<>();

        // AGRUPAR POR NUMERO DE VENTA

        for (SaleImportDTO dto : sales) {

            groupedSales
                    .computeIfAbsent(dto.getSaleNumber(), k -> new ArrayList<>())
                    .add(dto);
        }
        // crear ordenes
        for (List<SaleImportDTO> saleItems : groupedSales.values()) {

            SaleImportDTO first = saleItems.get(0);

            Order order = new Order();

            order.setCreatedAt(first.getCreatedAt());
            order.setPaymentMethod(first.getPaymentMethod());
            order.setInvoice(first.isInvoice());
            order.setInvoiceAmount(first.getInvoiceAmount());

            order.setSubtotal(BigDecimal.ZERO);
            order.setTotalAmount(BigDecimal.ZERO);

            BigDecimal total = BigDecimal.ZERO;

            // ITEMS

            for (SaleImportDTO dto : saleItems) {

                OrderItem item = new OrderItem();

                item.setOrder(order);

                // HISTORICO
                item.setVariant(null);

                item.setProductName(dto.getProductName());

                item.setMeasure("");

                item.setQuantity(dto.getQuantity());

                item.setPurchasePrice(dto.getPurchasePrice());

                item.setUnitPrice(dto.getSalePrice());

                item.setFinalPrice(dto.getSalePrice());

                item.setDiscountApplied(BigDecimal.ZERO);

                item.setSubtotal(dto.getSaleTotal());

                order.addItem(item);

                total = total.add(dto.getSaleTotal());
            }

            order.setSubtotal(total);

            order.setTotalAmount(total);

            orderRepository.save(order);
        }
    }

}