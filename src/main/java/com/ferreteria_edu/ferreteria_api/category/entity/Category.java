package com.ferreteria_edu.ferreteria_api.category.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.ferreteria_edu.ferreteria_api.product.entity.Product;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;
    private String description;

    @OneToMany(mappedBy = "category")
    @JsonManagedReference  // Maneja la referencia hacia atrás
    private List<Product>listProduct;
}
