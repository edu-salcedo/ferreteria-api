package com.ferreteria_edu.ferreteria_api.repository;

import com.ferreteria_edu.ferreteria_api.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
}
