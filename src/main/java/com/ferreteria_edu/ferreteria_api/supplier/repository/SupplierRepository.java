package com.ferreteria_edu.ferreteria_api.supplier.repository;

import com.ferreteria_edu.ferreteria_api.supplier.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupplierRepository  extends JpaRepository<Supplier, Long> {
}
