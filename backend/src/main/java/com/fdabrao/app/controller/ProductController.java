package com.fdabrao.app.controller;

import com.fdabrao.app.model.Categories;
import com.fdabrao.app.model.Product;
import com.fdabrao.app.persistence.CategoriesRepository;
import com.fdabrao.app.persistence.ProductRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class ProductController {

  private final ProductRepository productRepo;
  private final CategoriesRepository categoryRepo;

  ProductController(ProductRepository productRepo, CategoriesRepository categoryRepo) {
    this.productRepo = productRepo;
    this.categoryRepo = categoryRepo;
  }

  @GetMapping("/api/products")
  @PreAuthorize("isAuthenticated()")
  List<Product> getAll() {
    return productRepo.findAll();
  }

  @GetMapping("/api/products/{id}")
  @PreAuthorize("isAuthenticated()")
  Product getById(@PathVariable Long id) {
    return productRepo.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
  }

  @GetMapping("/api/products/by-category/{categoryId}")
  @PreAuthorize("isAuthenticated()")
  List<Product> getByCategory(@PathVariable Long categoryId) {
    Categories category = categoryRepo.findById(categoryId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
    
    return productRepo.findByCategory(category);
  }

  @PostMapping("/api/products")
  @PreAuthorize("isAuthenticated()")
  Product create(@RequestBody Product product) {
    // If a category ID is provided in the request
    if (product.getCategory() != null && product.getCategory().getId() != null) {
      Long categoryId = product.getCategory().getId();
      Categories category = categoryRepo.findById(categoryId)
          .orElseThrow(() -> new ResponseStatusException(
              HttpStatus.BAD_REQUEST, "Referenced category not found"));
      
      product.setCategory(category);
    }
    
    return productRepo.save(product);
  }

  @PutMapping("/api/products/{id}")
  @PreAuthorize("isAuthenticated()")
  Product update(@PathVariable Long id, @RequestBody Product product) {
    if (!productRepo.existsById(id)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
    }
    
    // If a category ID is provided in the request
    if (product.getCategory() != null && product.getCategory().getId() != null) {
      Long categoryId = product.getCategory().getId();
      Categories category = categoryRepo.findById(categoryId)
          .orElseThrow(() -> new ResponseStatusException(
              HttpStatus.BAD_REQUEST, "Referenced category not found"));
      
      product.setCategory(category);
    }
    
    product.setId(id);
    return productRepo.save(product);
  }

  @DeleteMapping("/api/products/{id}")
  @PreAuthorize("isAuthenticated()")
  void delete(@PathVariable Long id) {
    productRepo.deleteById(id);
  }
}
