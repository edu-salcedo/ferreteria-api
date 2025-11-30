package com.ferreteria_edu.ferreteria_api.mapper;

import com.ferreteria_edu.ferreteria_api.dto.productDto.ProductDTO;
import com.ferreteria_edu.ferreteria_api.model.Category;
import com.ferreteria_edu.ferreteria_api.model.Product;

public class ProductMapper {

    public static ProductDTO toDTO(Product p) {
        return ProductDTO.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .img(p.getImg())
                .price(p.getPrice())
                .stock(p.getStock())
                .state(p.isState())
                .categoryId(p.getCategory() != null ? p.getCategory().getId() : null)
                .categoriaName(p.getCategory() != null ? p.getCategory().getName() : null)
                .build();
    }

    public static Product toEntity(ProductDTO dto, Category c) {

        Product p = new Product();
        p.setId(dto.getId());
        p.setName(dto.getName());
        p.setDescription(dto.getDescription());
        p.setImg(dto.getImg());
        p.setPrice(dto.getPrice());
        p.setStock(dto.getStock());
        p.setState(dto.isState());
        p.setCategory(c);
        return p;
    }
}
