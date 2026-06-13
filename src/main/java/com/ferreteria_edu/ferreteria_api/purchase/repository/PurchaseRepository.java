package com.ferreteria_edu.ferreteria_api.purchase.repository;

import com.ferreteria_edu.ferreteria_api.purchase.entity.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseRepository extends JpaRepository<PurchaseOrder, Long> {
}
