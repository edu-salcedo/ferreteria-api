package com.ferreteria_edu.ferreteria_api.category.service;

import com.ferreteria_edu.ferreteria_api.category.dto.CategoryDTO;
import com.ferreteria_edu.ferreteria_api.exception.ResourceNotFoundException;
import com.ferreteria_edu.ferreteria_api.category.mapper.CategoryMapper;
import com.ferreteria_edu.ferreteria_api.category.entity.Category;
import com.ferreteria_edu.ferreteria_api.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    final CategoryRepository repository;

    public CategoryDTO create(CategoryDTO dto) {
        Category c = CategoryMapper.toEntity(dto);
        return CategoryMapper.toDTO(repository.save(c));
    }

    public List<CategoryDTO> findAll() {
        return repository.findAll().stream()
                .map(CategoryMapper::toDTO)
                .collect(Collectors.toList());
    }

    public CategoryDTO findById(Integer id) {
        Category c = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la categoría con ID " + id));
        return CategoryMapper.toDTO(c);
    }

    public CategoryDTO update(Integer id, CategoryDTO dto) {
        Category existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la categoría con ID " + id));

        existing.setName(dto.getName());

        return CategoryMapper.toDTO(repository.save(existing));
    }

    public void delete(Integer id) {
        Category existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la categoría con ID " + id));
        repository.delete(existing);
    }
}
