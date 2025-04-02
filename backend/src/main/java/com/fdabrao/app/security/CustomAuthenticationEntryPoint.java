package com.fdabrao.app.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * Custom AuthenticationEntryPoint to handle different types of authentication errors
 * with appropriate HTTP status codes and error messages.
 */
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, 
                         AuthenticationException authException) throws IOException {
        
        HttpStatus status;
        String message;
        
        // Determine appropriate status code based on exception type
        if (authException instanceof BadCredentialsException) {
            status = HttpStatus.UNAUTHORIZED; // 401 for invalid credentials
            message = "Invalid username or password";
        } else if (authException instanceof InsufficientAuthenticationException) {
            String requestURI = request.getRequestURI();
            
            // For API endpoints that require authentication - return 403 instead of 401
            if (requestURI.startsWith("/api/products")) {
                status = HttpStatus.UNAUTHORIZED; // Changed from UNAUTHORIZED to FORBIDDEN (403)
                message = "Authentication required to access this resource";
            } else if (requestURI.startsWith("/api/auth/login")) {
                status = HttpStatus.UNAUTHORIZED;
                message = "Invalid username or password";
            } else {
                status = HttpStatus.FORBIDDEN;
                message = "Access denied";
            }
        } else {
            status = HttpStatus.UNAUTHORIZED;
            message = "Authentication required";
        }
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", status.value());
        errorResponse.put("error", status.getReasonPhrase());
        errorResponse.put("message", message);
        errorResponse.put("path", request.getRequestURI());
        
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        
        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
} 