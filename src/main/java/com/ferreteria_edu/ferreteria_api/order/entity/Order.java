package com.ferreteria_edu.ferreteria_api.order.entity;

import com.ferreteria_edu.ferreteria_api.enun.PaymentMethod;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
//Crear objetos de manera flexible usando el patrón builder
@Builder
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    //Mantieen valores por defecto al usar @Builder
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @Column(nullable = false)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal surcharge = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal discount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod = PaymentMethod.EFECTIVO;

    public void calculateSubtotal() {
        this.subtotal = items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void addItem(OrderItem item) {

        items.add(item);
        item.setOrder(this);
    }
}
