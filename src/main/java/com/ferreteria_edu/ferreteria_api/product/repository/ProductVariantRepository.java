package com.ferreteria_edu.ferreteria_api.product.repository;

import com.ferreteria_edu.ferreteria_api.product.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductVariantRepository  extends JpaRepository<ProductVariant, Long> {
    Optional<ProductVariant> findByProduct_IdAndMeasure(
            Long productId,
            String measure
    );

    List<ProductVariant> findByProduct_Id(Long productId);
}
