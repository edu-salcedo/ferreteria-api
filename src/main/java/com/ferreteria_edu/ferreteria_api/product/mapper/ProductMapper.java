package com.ferreteria_edu.ferreteria_api.product.mapper;

import com.ferreteria_edu.ferreteria_api.category.entity.Category;
import com.ferreteria_edu.ferreteria_api.product.dto.ProductDTO;
import com.ferreteria_edu.ferreteria_api.product.entity.Product;

import java.util.stream.Collectors;

public class ProductMapper {

    public static ProductDTO toDTO(Product p) {

        if (p == null) return null;

        return ProductDTO.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .img(p.getImg())
                .state(p.isState())
                .categoryId(
                        p.getCategory() != null
                                ? p.getCategory().getId()
                                : null
                )
                .categoryName(
                        p.getCategory() != null
                                ? p.getCategory().getName()
                                : null
                )
                .variants(
                        p.getVariants()
                                .stream()
                                .map(ProductVariantMapper::toDTO)
                                .collect(Collectors.toList())
                )
                .build();
    }

    public static Product toEntity(ProductDTO dto, Category category) {

        Product p = new Product();

        p.setId(dto.getId());
        p.setName(dto.getName());
        p.setDescription(dto.getDescription());
        p.setImg(dto.getImg());
        p.setState(dto.isState());
        p.setCategory(category);

        return p;
    }

}