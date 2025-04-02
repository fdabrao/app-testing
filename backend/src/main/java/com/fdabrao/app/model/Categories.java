package com.fdabrao.app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "category")
public class Categories {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String name;

  @Column
  private String description;

  @Column
  private String parentCategory;

  @Column(nullable = false)
  private Boolean active;

  public Categories() {}

  public Categories(
    Long id,
    String name,
    String description,
    String parentCategory,
    Boolean active
  ) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.parentCategory = parentCategory;
    this.active = active;
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public String getParentCategory() {
    return parentCategory;
  }

  public Boolean getActive() {
    return active;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setParentCategory(String parentCategory) {
    this.parentCategory = parentCategory;
  }

  public void setActive(Boolean active) {
    this.active = active;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    
    Categories category = (Categories) o;
    
    if (id != null ? !id.equals(category.id) : category.id != null) return false;
    if (!name.equals(category.name)) return false;
    if (description != null ? !description.equals(category.description) : category.description != null) return false;
    if (parentCategory != null ? !parentCategory.equals(category.parentCategory) : category.parentCategory != null) return false;
    return active.equals(category.active);
  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + name.hashCode();
    result = 31 * result + (description != null ? description.hashCode() : 0);
    result = 31 * result + (parentCategory != null ? parentCategory.hashCode() : 0);
    result = 31 * result + active.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "Categories{" +
           "id=" + id +
           ", name='" + name + '\'' +
           ", description='" + description + '\'' +
           ", parentCategory='" + parentCategory + '\'' +
           ", active=" + active +
           '}';
  }
} 