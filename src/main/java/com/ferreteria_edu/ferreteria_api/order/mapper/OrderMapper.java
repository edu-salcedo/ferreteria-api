package com.ferreteria_edu.ferreteria_api.order.mapper;

import com.ferreteria_edu.ferreteria_api.order.dto.OrderItemResponseDTO;
import com.ferreteria_edu.ferreteria_api.order.dto.OrderResponseDTO;
import com.ferreteria_edu.ferreteria_api.order.entity.Order;
import com.ferreteria_edu.ferreteria_api.order.entity.OrderItem;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class OrderMapper {

    public static OrderResponseDTO toResponse(Order order) {

        if (order == null) {
            return null;
        }
        OrderResponseDTO dto = new OrderResponseDTO();

        dto.setId(order.getId());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setSubTotal(order.getSubtotal());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setTotalSurcharge(order.getSurcharge());
        dto.setInvoice(order.isInvoice());
        dto.setInvoiceAmount(order.getInvoiceAmount());

        BigDecimal sub = order.getSubtotal() != null ? order.getSubtotal() : BigDecimal.ZERO;
        BigDecimal tot = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;
        dto.setTotalDiscount(sub.subtract(tot).compareTo(BigDecimal.ZERO) > 0
                ? sub.subtract(tot)
                : BigDecimal.ZERO);

        // [NUEVOS CAMPOS TRADUCIDOS PARA ARCA Y COMPROBANTES]
        dto.setOrderType(order.getOrderType());
        dto.setDocumentType(order.getDocumentType());
        dto.setPosNumber(order.getPosNumber());
        dto.setInvoiceNumber(order.getInvoiceNumber());
        dto.setInvoiceType(order.getInvoiceType());

        if (order.getItems() != null) {
            List<OrderItemResponseDTO> items = order.getItems()
                    .stream()
                    .map(OrderMapper::mapItem)
                    .toList();

            dto.setItems(items);
        } else {
            dto.setItems(new ArrayList<>());
        }

        return dto;
    }

    private static OrderItemResponseDTO mapItem(OrderItem item) {
        if (item == null) {
            return null;
        }
        OrderItemResponseDTO dto = new OrderItemResponseDTO();
        if (item.getVariant() != null) {
            dto.setVariantId(item.getVariant().getId());
        }
        if (item.getPurchasePrice() != null) {
            dto.setPurchasePrice(item.getPurchasePrice());
        } else if (item.getVariant() != null) {
            dto.setPurchasePrice(item.getVariant().getPurchasePrice());
        } else {
            dto.setPurchasePrice(BigDecimal.ZERO);
        }
        dto.setProductName(item.getProductName());
        dto.setMeasure(item.getMeasure());
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setFinalPrice(item.getFinalPrice());
        dto.setSubtotal(item.getSubtotal());
        dto.setDiscountApplied(item.getDiscountApplied());

        return dto;
    }
}
