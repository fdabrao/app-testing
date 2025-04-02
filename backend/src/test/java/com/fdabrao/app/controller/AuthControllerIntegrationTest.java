package com.fdabrao.app.controller;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fdabrao.app.model.Categories;
import com.fdabrao.app.model.Product;
import com.fdabrao.app.model.User;
import com.fdabrao.app.persistence.CategoriesRepository;
import com.fdabrao.app.persistence.ProductRepository;
import com.fdabrao.app.persistence.UserRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthControllerIntegrationTest {

    @LocalServerPort
    private Integer port;

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            "postgres:16-alpine");

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    CategoriesRepository categoriesRepository;    

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeAll
    static void beforeAll() {
        postgres.start();
    }

    private Categories category1;
    private Categories category2;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost:" + port;
        userRepository.deleteAll();
        productRepository.deleteAll();
        categoriesRepository.deleteAll();

        // Create test categories
        category1 = new Categories(null, "Category 1", "Description 1", null, true);
        category2 = new Categories(null, "Category 2", "Description 2", null, true);
        categoriesRepository.saveAll(List.of(category1, category2));
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
        productRepository.deleteAll();
    }

    @Test
    void shouldRegisterNewUser() {
        // Arrange
        String username = "admin_" + UUID.randomUUID().toString().substring(0, 8);
        String email = username + "@local.com";
        String password = "admin";

        Map<String, Object> newUserMap = new HashMap<>();
        newUserMap.put("username", username);
        newUserMap.put("email", email);
        newUserMap.put("passwordHash", password);
        newUserMap.put("firstName", "Admin");
        newUserMap.put("lastName", "User");

        // Act
        Response response = given()
                .contentType(ContentType.JSON)
                .body(newUserMap)
                .when()
                .post("/api/auth/register");

        // Assert
        response.then()
                .statusCode(HttpStatus.CREATED.value())
                .body("token", notNullValue())
                .body("username", equalTo(username))
                .body("email", equalTo(email))
                .body("firstName", equalTo("Admin"))
                .body("lastName", equalTo("User"))
                .body("role", equalTo("USER"));

        // Verify user was created in the database
        User savedUser = userRepository.findByUsername(username).orElse(null);
        assertNotNull(savedUser);
        assertEquals(username, savedUser.getUsername());
        assertEquals(email, savedUser.getEmail());
        assertTrue(passwordEncoder.matches(password, savedUser.getPasswordHash()));
        assertEquals("USER", savedUser.getRole());
    }

    @Test
    void shouldNotRegisterUserWithExistingUsername() {
        // Arrange - create a user first
        String username = "existing_user";
        createTestUser(username, "unique_email@example.com");

        // Try to register with same username but different email
        Map<String, Object> newUserMap = new HashMap<>();
        newUserMap.put("username", username);
        newUserMap.put("email", "different_email@example.com");
        newUserMap.put("passwordHash", "password123");
        newUserMap.put("firstName", "Test");
        newUserMap.put("lastName", "User");

        given()
                .contentType(ContentType.JSON)
                .body(newUserMap)
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("message", equalTo("Username already exists"));
    }

    @Test
    void shouldNotRegisterUserWithExistingEmail() {
        String email = "same_email@example.com";
        createTestUser("unique_username", email);

        Map<String, Object> newUserMap = new HashMap<>();
        newUserMap.put("username", "different_username");
        newUserMap.put("email", email);
        newUserMap.put("passwordHash", "password123");
        newUserMap.put("firstName", "Test");
        newUserMap.put("lastName", "User");

        given()
                .contentType(ContentType.JSON)
                .body(newUserMap)
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("message", equalTo("Email already exists"));
    }

    @Test
    void shouldLoginWithValidCredentials() {
        // Arrange
        String username = "logintest";
        String password = "password123";
        createTestUser(username, "logintest@example.com", password);

        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", username);
        credentials.put("password", password);

        // Act
        Response response = given()
                .contentType(ContentType.JSON)
                .body(credentials)
                .when()
                .post("/api/auth/login");

        // Assert
        response.then()
                .statusCode(HttpStatus.OK.value())
                .body("token", notNullValue())
                .body("username", equalTo(username))
                .body("role", equalTo("USER"));

        String token = response.jsonPath().getString("token");

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/api/products")
                .then()
                .statusCode(HttpStatus.OK.value());
    }

    @Test
    void shouldFailLoginWithInvalidPassword() {
        // Arrange
        String username = "wrongpasstest";
        createTestUser(username, "wrongpass@example.com", "correctpassword");

        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", username);
        credentials.put("password", "wrongpassword");

        given()
                .contentType(ContentType.JSON)
                .body(credentials)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("message", Matchers.anyOf(
                        Matchers.equalTo("Invalid username or password"),
                        Matchers.equalTo("Invalid credentials")));
    }

    @Test
    void shouldFailLoginWithNonExistentUser() {
        // Arrange
        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", "nonexistentuser");
        credentials.put("password", "anypassword");

        given()
                .contentType(ContentType.JSON)
                .body(credentials)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("message", Matchers.anyOf(
                        Matchers.equalTo("Invalid username or password"),
                        Matchers.equalTo("Invalid credentials")));
    }

    @Test
    void tokenShouldBeRequiredForProtectedEndpoints() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/products")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("message", equalTo("Authentication required to access this resource"));
    }

    @Test
    void invalidTokenShouldBeRejected() {
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer invalidtoken123")
                .when()
                .get("/api/products")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void authorizedUserShouldReceiveProductList() {
        String adminUsername = "admin";
        createTestUser(adminUsername, "admin@local.com");
        String adminToken = getAuthToken(adminUsername, "password123");
        // Create a product directly to ensure it exists
        Product product = new Product(null, "Product 1", "Test Description", 19.99, true, category1);
        productRepository.save(product);

        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/api/products")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("content", Matchers.hasSize(1));
    }

    // Helper methods
    private void createTestUser(String username, String email) {
        createTestUser(username, email, "password123");
    }

    private void createTestUser(String username, String email, String password) {
        User user = new User(
                username,
                email,
                passwordEncoder.encode(password),
                "Test",
                "User",
                "USER",
                true);
        user.setCreatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    private String getAuthToken(String username, String password) {
        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", username);
        credentials.put("password", password);

        Response response = given()
                .contentType(ContentType.JSON)
                .body(credentials)
                .when()
                .post("/api/auth/login");

        response.then().statusCode(200);
        return response.jsonPath().getString("token");
    }
}