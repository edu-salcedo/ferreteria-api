package com.ferreteria_edu.ferreteria_api.product.repository;

import com.ferreteria_edu.ferreteria_api.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByName(String name);

    // 🚀 SOLUCIÓN: Método derivado automático (Cero consultas manuales propensas a
    // error 500)
    Page<Product> findByNameContainingIgnoreCaseAndCategoryId(String name, Long categoryId, Pageable pageable);

    // 🚀 Fallback: Si no se envía categoría, necesitamos poder buscar solo por
    // nombre
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

}
