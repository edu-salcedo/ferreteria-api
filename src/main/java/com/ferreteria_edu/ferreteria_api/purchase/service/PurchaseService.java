package com.ferreteria_edu.ferreteria_api.purchase.service;

import com.ferreteria_edu.ferreteria_api.exception.ResourceNotFoundException;
import com.ferreteria_edu.ferreteria_api.product.entity.Product;
import com.ferreteria_edu.ferreteria_api.product.repository.ProductRepository;
import com.ferreteria_edu.ferreteria_api.purchase.dto.PurchaseItemDTO;
import com.ferreteria_edu.ferreteria_api.purchase.dto.PurchaseRequestDTO;
import com.ferreteria_edu.ferreteria_api.purchase.entity.PurchaseItem;
import com.ferreteria_edu.ferreteria_api.purchase.entity.PurchaseOrder;
import com.ferreteria_edu.ferreteria_api.purchase.repository.PurchaseRepository;
import com.ferreteria_edu.ferreteria_api.supplier.entity.Supplier;
import com.ferreteria_edu.ferreteria_api.supplier.repository.SupplierRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;


import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class PurchaseService {
     private SupplierRepository supplierRepository;
     private ProductRepository productRepository;
     private PurchaseRepository purchaseRepository;
    @Transactional
    public PurchaseOrder createPurchase(PurchaseRequestDTO request) {

        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found"));

        PurchaseOrder purchase = new PurchaseOrder();
        purchase.setSupplier(supplier);
        purchase.setCreatedAt(LocalDateTime.now());

        BigDecimal total = BigDecimal.ZERO;

        for (PurchaseItemDTO dto : request.getItems()) {

            Product product = productRepository.findById(dto.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

            PurchaseItem item = new PurchaseItem();

            item.setProductName(product.getName());
            item.setQuantity(dto.getQuantity());
            item.setUnitCost(dto.getUnitCost());

            BigDecimal subtotal = dto.getUnitCost()
                    .multiply(BigDecimal.valueOf(dto.getQuantity()));

            item.setSubtotal(subtotal);
            item.setPurchaseOrder(purchase);

            purchase.getItems().add(item);

            total = total.add(subtotal);

            // ⭐ IMPORTANTE: actualizar stock
            product.setStock(product.getStock() + dto.getQuantity());
            productRepository.save(product);
        }

        purchase.setTotalAmount(total);

        return purchaseRepository.save(purchase);
    }
}


