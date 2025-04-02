package com.fdabrao.app.persistence;

import com.fdabrao.app.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  
  // Find a user by username
  Optional<User> findByUsername(String username);
  
  // Find a user by email
  Optional<User> findByEmail(String email);
  
  // Check if a username exists
  boolean existsByUsername(String username);
  
  // Check if an email exists
  boolean existsByEmail(String email);
  
  @Modifying
  @Transactional
  @Query("UPDATE User u SET u.lastLogin = CURRENT_TIMESTAMP WHERE u.id = :userId")
  void updateLastLogin(Long userId);
} 