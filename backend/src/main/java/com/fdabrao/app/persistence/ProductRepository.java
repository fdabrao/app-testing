package com.fdabrao.app.persistence;

import com.fdabrao.app.model.Categories;
import com.fdabrao.app.model.Product;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategory(Categories category);
    List<Product> findByCategoryId(Long categoryId);
}
