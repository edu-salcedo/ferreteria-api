package com.ferreteria_edu.ferreteria_api.category.mapper;

import com.ferreteria_edu.ferreteria_api.category.dto.CategoryDTO;
import com.ferreteria_edu.ferreteria_api.category.entity.Category;

public class CategoryMapper {

    public static CategoryDTO toDTO(Category c) {
        return CategoryDTO.builder()
                .id(c.getId())
                .name(c.getName())
                .build();
    }

    public static Category toEntity(CategoryDTO dto) {
        Category c = new Category();
        c.setName(dto.getName());
        return c;
    }
}
