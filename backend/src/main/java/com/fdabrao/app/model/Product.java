package com.fdabrao.app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "product")
public class Product {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String description;

  @Column(nullable = false)
  private Double price;

  @Column(nullable = false)
  private Boolean available;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "category_id")
  private Categories category;

  public Product() {}

  public Product(
    Long id,
    String name,
    String description,
    Double price,
    Boolean available,
    Categories category
  ) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.price = price;
    this.available = available;
    this.category = category;
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

  public Double getPrice() {
    return price;
  }

  public Boolean getAvailable() {
    return available;
  }

  public Categories getCategory() {
    return category;
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

  public void setPrice(Double price) {
    this.price = price;
  }

  public void setAvailable(Boolean available) {
    this.available = available;
  }

  public void setCategory(Categories category) {
    this.category = category;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    
    Product product = (Product) o;
    
    if (id != null ? !id.equals(product.id) : product.id != null) return false;
    if (!name.equals(product.name)) return false;
    if (!description.equals(product.description)) return false;
    if (!price.equals(product.price)) return false;
    if (!available.equals(product.available)) return false;
    return category != null ? category.equals(product.category) : product.category == null;
  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + name.hashCode();
    result = 31 * result + description.hashCode();
    result = 31 * result + price.hashCode();
    result = 31 * result + available.hashCode();
    result = 31 * result + (category != null ? category.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "Product{" +
           "id=" + id +
           ", name='" + name + '\'' +
           ", description='" + description + '\'' +
           ", price=" + price +
           ", available=" + available +
           ", category=" + (category != null ? category.getName() : "null") +
           '}';
  }
}
