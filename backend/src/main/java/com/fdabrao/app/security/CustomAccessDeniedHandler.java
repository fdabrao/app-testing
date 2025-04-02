package com.fdabrao.app.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/**
 * Custom handler for access denied exceptions (403 Forbidden).
 * Provides structured error responses for authorization failures.
 */
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private static final Logger logger = LoggerFactory.getLogger(CustomAccessDeniedHandler.class);
    private static final Pattern PRODUCT_DELETE_PATTERN = Pattern.compile("/api/products/\\d+");
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        
        HttpStatus status = HttpStatus.FORBIDDEN;
        String message;
        
        // Customize message based on the request URI and method
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        
        logger.debug("Access denied for URI: {} with method: {}", requestURI, method);
        
        if (PRODUCT_DELETE_PATTERN.matcher(requestURI).matches() && "DELETE".equals(method)) {
            message = "You don't have admin privileges to delete products";
        } else {
            message = "Insufficient permissions to access this resource";
        }
        
        // Create error response
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", status.value());
        errorResponse.put("error", status.getReasonPhrase());
        errorResponse.put("message", message);
        errorResponse.put("path", requestURI);
        
        // Configure response
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        
        // Write error response as JSON
        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
} 