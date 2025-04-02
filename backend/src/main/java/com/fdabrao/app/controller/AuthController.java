package com.fdabrao.app.controller;

import com.fdabrao.app.model.User;
import com.fdabrao.app.persistence.UserRepository;
import com.fdabrao.app.security.JwtTokenUtil;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final UserRepository userRepository;
  private final AuthenticationManager authenticationManager;
  private final UserDetailsService userDetailsService;
  private final JwtTokenUtil jwtTokenUtil;
  private final PasswordEncoder passwordEncoder;

  public AuthController(UserRepository userRepository,
                        AuthenticationManager authenticationManager,
                        UserDetailsService userDetailsService,
                        JwtTokenUtil jwtTokenUtil,
                        PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.authenticationManager = authenticationManager;
    this.userDetailsService = userDetailsService;
    this.jwtTokenUtil = jwtTokenUtil;
    this.passwordEncoder = passwordEncoder;
  }
  
  // Login endpoint
  @PostMapping("/login")
  public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credentials) {
    String username = credentials.get("username");
    String password = credentials.get("password");
    
    if (username == null || password == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username and password are required");
    }
    
    try {
      // Authenticate the user
      authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
    } catch (BadCredentialsException e) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
    }
    
    // Load user details
    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
    
    // Generate JWT token
    String token = jwtTokenUtil.generateToken(userDetails);
    
    // Get the user from repository
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    
    // Update last login time
    user.setLastLogin(LocalDateTime.now());
    userRepository.save(user);
    
    // Create response with token and user details
    Map<String, Object> response = new HashMap<>();
    response.put("token", token);
    response.put("id", user.getId());
    response.put("username", user.getUsername());
    response.put("email", user.getEmail());
    response.put("firstName", user.getFirstName());
    response.put("lastName", user.getLastName());
    response.put("role", user.getRole());
    
    return ResponseEntity.ok(response);
  }
  
  // Register new user
  @PostMapping("/register")
  public ResponseEntity<Map<String, Object>> register(@RequestBody User newUser) {
    // Check if username or email already exists
    if (userRepository.existsByUsername(newUser.getUsername())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already exists");
    }
    
    if (userRepository.existsByEmail(newUser.getEmail())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
    }
    
    // Hash the password before saving
    newUser.setPasswordHash(passwordEncoder.encode(newUser.getPasswordHash()));
    
    // Set default values
    newUser.setRole("USER");
    newUser.setActive(true);
    newUser.setCreatedAt(LocalDateTime.now());
    
    // Save user
    User savedUser = userRepository.save(newUser);
    
    // Generate JWT token
    UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getUsername());
    String token = jwtTokenUtil.generateToken(userDetails);
    
    // Create response with token and user details
    Map<String, Object> response = new HashMap<>();
    response.put("token", token);
    response.put("id", savedUser.getId());
    response.put("username", savedUser.getUsername());
    response.put("email", savedUser.getEmail());
    response.put("firstName", savedUser.getFirstName());
    response.put("lastName", savedUser.getLastName());
    response.put("role", savedUser.getRole());
    
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }
} 