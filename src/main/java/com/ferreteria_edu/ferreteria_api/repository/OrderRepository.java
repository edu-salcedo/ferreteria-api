package com.ferreteria_edu.ferreteria_api.repository;

import com.ferreteria_edu.ferreteria_api.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
