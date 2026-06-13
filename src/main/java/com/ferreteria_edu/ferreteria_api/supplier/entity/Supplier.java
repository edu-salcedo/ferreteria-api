package com.ferreteria_edu.ferreteria_api.supplier.entity;

import com.ferreteria_edu.ferreteria_api.purchase.entity.PurchaseOrder;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Data;

import java.util.List;
import java.util.UUID;
@Data
@Entity
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String cuit;
    private String phone;
    private String email;
    private String address;

    @OneToMany(mappedBy = "supplier")
    private List<PurchaseOrder> purchases;
}
