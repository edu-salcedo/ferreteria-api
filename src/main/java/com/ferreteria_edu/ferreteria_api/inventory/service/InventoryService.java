package com.ferreteria_edu.ferreteria_api.inventory.service;

import com.ferreteria_edu.ferreteria_api.exception.ResourceNotFoundException;
import com.ferreteria_edu.ferreteria_api.product.entity.Product;
import com.ferreteria_edu.ferreteria_api.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventoryService {

        private final ProductRepository productRepository;

        @Transactional
        public Product decreaseStock(Long productId, int quantity) {

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Producto no encontrado con ID: " + productId
                    ));

            if (product.getStock() < quantity) {
                throw new RuntimeException(
                        "Stock insuficiente para el producto: " + product.getName()
                );
            }

            product.setStock(product.getStock() - quantity);

            return productRepository.save(product);
        }

}
