package com.ferreteria_edu.ferreteria_api.mapper;

import com.ferreteria_edu.ferreteria_api.dto.categoryDto.CategoryDTO;
import com.ferreteria_edu.ferreteria_api.model.Category;

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
