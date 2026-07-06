package com.ferreteria_edu.ferreteria_api.product.repository;

import com.ferreteria_edu.ferreteria_api.product.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductVariantRepository  extends JpaRepository<ProductVariant, Long> {
}
