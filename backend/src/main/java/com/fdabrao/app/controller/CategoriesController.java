package com.fdabrao.app.controller;

import com.fdabrao.app.model.Categories;
import com.fdabrao.app.persistence.CategoriesRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class CategoriesController {

  private final CategoriesRepository repo;

  CategoriesController(CategoriesRepository repo) {
    this.repo = repo;
  }

  @GetMapping("/api/categories")
  @PreAuthorize("isAuthenticated()")
  List<Categories> getAll(@RequestParam(required = false) Boolean activeOnly) {
    if (activeOnly != null && activeOnly) {
      return repo.findByActiveTrue();
    }
    return repo.findAll();
  }

  @GetMapping("/api/categories/{id}")
  @PreAuthorize("isAuthenticated()")
  Categories getById(@PathVariable Long id) {
    return repo.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
  }

  @GetMapping("/api/categories/byParent/{parentCategory}")
  @PreAuthorize("isAuthenticated()")
  List<Categories> getByParentCategory(@PathVariable String parentCategory) {
    return repo.findByParentCategory(parentCategory);
  }

  @PostMapping("/api/categories")
  @PreAuthorize("isAuthenticated()")
  ResponseEntity<Categories> create(@RequestBody Categories category) {
    // Check if category name already exists
    if (repo.findByName(category.getName()).isPresent()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category name already exists");
    }
    Categories savedCategory = repo.save(category);
    return new ResponseEntity<>(savedCategory, HttpStatus.CREATED);
  }

  @PutMapping("/api/categories/{id}")
  @PreAuthorize("isAuthenticated()")
  Categories update(@PathVariable Long id, @RequestBody Categories category) {
    if (!repo.existsById(id)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found");
    }
    
    // If name is changed, check that it doesn't conflict with existing categories
    repo.findByName(category.getName()).ifPresent(existingCategory -> {
      if (!existingCategory.getId().equals(id)) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "Another category with this name already exists");
      }
    });
    
    category.setId(id); // Ensure ID is set correctly
    return repo.save(category);
  }

  @DeleteMapping("/api/categories/{id}")
  @PreAuthorize("isAuthenticated()")
  void delete(@PathVariable Long id) {
    if (!repo.existsById(id)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found");
    }
    repo.deleteById(id);
  }
} 