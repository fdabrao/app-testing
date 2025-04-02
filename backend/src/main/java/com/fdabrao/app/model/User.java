package com.fdabrao.app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "app_user")
public class User {
  
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @Column(nullable = false, unique = true, length = 50)
  private String username;
  
  @Column(nullable = false, unique = true, length = 100)
  private String email;
  
  @Column(name = "password_hash", nullable = false)
  private String passwordHash;
  
  @Column(name = "first_name", length = 50)
  private String firstName;
  
  @Column(name = "last_name", length = 50)
  private String lastName;
  
  @Column(nullable = false, length = 20)
  private String role;
  
  @Column(nullable = false)
  private Boolean active = true;
  
  @Column(name = "created_at")
  private LocalDateTime createdAt;
  
  @Column(name = "last_login")
  private LocalDateTime lastLogin;
  
  // Default constructor required by JPA
  public User() {
  }
  
  public User(String username, String email, String passwordHash, String firstName, 
              String lastName, String role, Boolean active) {
    this.username = username;
    this.email = email;
    this.passwordHash = passwordHash;
    this.firstName = firstName;
    this.lastName = lastName;
    this.role = role;
    this.active = active;
    this.createdAt = LocalDateTime.now();
  }
  
  // Getters and setters
  public Long getId() {
    return id;
  }
  
  public void setId(Long id) {
    this.id = id;
  }
  
  public String getUsername() {
    return username;
  }
  
  public void setUsername(String username) {
    this.username = username;
  }
  
  public String getEmail() {
    return email;
  }
  
  public void setEmail(String email) {
    this.email = email;
  }
  
  public String getPasswordHash() {
    return passwordHash;
  }
  
  public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
  }
  
  public String getFirstName() {
    return firstName;
  }
  
  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }
  
  public String getLastName() {
    return lastName;
  }
  
  public void setLastName(String lastName) {
    this.lastName = lastName;
  }
  
  public String getRole() {
    return role;
  }
  
  public void setRole(String role) {
    this.role = role;
  }
  
  public Boolean getActive() {
    return active;
  }
  
  public void setActive(Boolean active) {
    this.active = active;
  }
  
  public LocalDateTime getCreatedAt() {
    return createdAt;
  }
  
  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }
  
  public LocalDateTime getLastLogin() {
    return lastLogin;
  }
  
  public void setLastLogin(LocalDateTime lastLogin) {
    this.lastLogin = lastLogin;
  }
  
  @Override
  public String toString() {
    return "User{" +
           "id=" + id +
           ", username='" + username + '\'' +
           ", email='" + email + '\'' +
           ", firstName='" + firstName + '\'' +
           ", lastName='" + lastName + '\'' +
           ", role='" + role + '\'' +
           ", active=" + active +
           ", createdAt=" + createdAt +
           ", lastLogin=" + lastLogin +
           '}';
  }
} 