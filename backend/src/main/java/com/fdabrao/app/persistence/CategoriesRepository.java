package com.fdabrao.app.persistence;

import com.fdabrao.app.model.Categories;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriesRepository extends JpaRepository<Categories, Long> {
    Optional<Categories> findByName(String name);
    List<Categories> findByParentCategory(String parentCategory);
    List<Categories> findByActiveTrue();
} 