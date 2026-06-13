package com.ferreteria_edu.ferreteria_api.inventory.entity;

import com.ferreteria_edu.ferreteria_api.product.entity.Product;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private long id;

    @OneToOne
    private Product product;

    private Integer stock;
}
