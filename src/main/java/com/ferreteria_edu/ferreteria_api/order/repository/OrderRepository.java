package com.ferreteria_edu.ferreteria_api.order.repository;

import com.ferreteria_edu.ferreteria_api.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
