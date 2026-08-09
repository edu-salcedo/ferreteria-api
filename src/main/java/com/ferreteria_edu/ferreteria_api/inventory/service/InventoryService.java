package com.ferreteria_edu.ferreteria_api.inventory.service;

import com.ferreteria_edu.ferreteria_api.exception.ResourceNotFoundException;
import com.ferreteria_edu.ferreteria_api.product.entity.Product;
import com.ferreteria_edu.ferreteria_api.product.entity.ProductVariant;
import com.ferreteria_edu.ferreteria_api.product.repository.ProductRepository;
import com.ferreteria_edu.ferreteria_api.product.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final ProductVariantRepository variantRepository;

    @Transactional
    public ProductVariant decreaseStock(Long variantId, int quantity) {

        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Variante no encontrada con ID: " + variantId
                ));

        if (variant.getStock() < quantity) {
            throw new RuntimeException(
                    "Stock insuficiente para la variante: " + variant.getMeasure()
            );
        }

        variant.setStock(variant.getStock() - quantity);

        return variantRepository.save(variant);
    }
}