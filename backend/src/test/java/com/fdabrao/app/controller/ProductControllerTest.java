package com.fdabrao.app.controller;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductControllerTest {

    @LocalServerPort
    private Integer port;

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            "postgres:16-alpine");

    @Autowired
    ProductRepository productRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CategoriesRepository categoriesRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    private String userToken;
    private Categories category1;
    private Categories category2;

    @BeforeAll
    static void beforeAll() {
        postgres.start();
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost:" + port;
        productRepository.deleteAll();
        categoriesRepository.deleteAll();
        userRepository.deleteAll();

        // Create test categories
        category1 = new Categories(null, "Category 1", "Description 1", null, true);
        category2 = new Categories(null, "Category 2", "Description 2", null, true);
        categoriesRepository.saveAll(List.of(category1, category2));

        createTestUsers();

        userToken = getAuthToken("user_test", "user_password");
    }

    /**
     * Creates test users for authentication testing
     */
    private void createTestUsers() {
        User user = new User(
                "user_test",
                "user_test@example.com",
                passwordEncoder.encode("user_password"),
                "User",
                "Test",
                "USER",
                true);
        user.setCreatedAt(java.time.LocalDateTime.now());
        userRepository.save(user);
    }

    /**
     * Gets an authentication token for the specified user
     */
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

    @Test
    void authenticatedUserShouldGetAllProducts() {
        List<Product> products = List.of(
                new Product(null, "Food 1", "Description 1", 10.0, true, category1),
                new Product(null, "Food 2", "Description 2", 20.0, true, category2));
        productRepository.saveAll(products);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + userToken)
                .when()
                .get("/api/products")
                .then()
                .statusCode(200)
                .body(".", Matchers.hasSize(2));
    }

    @Test
    void unauthenticatedUserShouldNotGetAllProducts() {
        List<Product> products = List.of(
                new Product(null, "Food 1", "Description 1", 10.0, true, category1),
                new Product(null, "Food 2", "Description 2", 20.0, true, category2));
        productRepository.saveAll(products);

        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/products")
                .then()
                .statusCode(401);
    }

    @Test
    void authenticatedUserShouldGetProductById() {
        Product product = new Product(null, "Product 1", "Description 1", 10.0, true, category1);
        productRepository.save(product);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + userToken)
                .when()
                .get("/api/products/{id}", product.getId())
                .then()
                .statusCode(200)
                .body("id", Matchers.equalTo(product.getId().intValue()))
                .body("name", Matchers.equalTo(product.getName()))
                .body("description", Matchers.equalTo(product.getDescription()))
                .body("price", Matchers.equalTo(product.getPrice().floatValue()))
                .body("available", Matchers.equalTo(product.getAvailable()))
                .body("category.id", Matchers.equalTo(category1.getId().intValue()))
                .body("category.name", Matchers.equalTo(category1.getName()));
    }

    @Test
    void unauthenticatedUserShouldNotGetProductById() {
        Product product = new Product(null, "Product 1", "Description 1", 10.0, true, category1);
        productRepository.save(product);

        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/products/{id}", product.getId())
                .then()
                .statusCode(401);
    }

    @Test
    void authenticatedUserShouldGetProductsByCategory() {
        List<Product> products = List.of(
                new Product(null, "Product 1-1", "Description 1-1", 10.0, true, category1),
                new Product(null, "Product 1-2", "Description 1-2", 15.0, true, category1),
                new Product(null, "Product 2-1", "Description 2-1", 20.0, true, category2));
        productRepository.saveAll(products);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + userToken)
                .when()
                .get("/api/products/by-category/{categoryId}", category1.getId())
                .then()
                .statusCode(200)
                .body(".", Matchers.hasSize(2))
                .body("[0].category.id", Matchers.equalTo(category1.getId().intValue()))
                .body("[1].category.id", Matchers.equalTo(category1.getId().intValue()));
    }

    @Test
    void authenticatedUserShouldCreateNewProduct() {
        Map<String, Object> newProductMap = new HashMap<>();
        newProductMap.put("name", "New Product");
        newProductMap.put("description", "New Description");
        newProductMap.put("price", 15.99);
        newProductMap.put("available", true);
        
        Map<String, Object> categoryMap = new HashMap<>();
        categoryMap.put("id", category1.getId());
        newProductMap.put("category", categoryMap);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + userToken)
                .body(newProductMap)
                .when()
                .post("/api/products")
                .then()
                .statusCode(200)
                .body("name", Matchers.equalTo("New Product"))
                .body("description", Matchers.equalTo("New Description"))
                .body("price", Matchers.equalTo(15.99f))
                .body("available", Matchers.equalTo(true))
                .body("category.id", Matchers.equalTo(category1.getId().intValue()));

        assertEquals(1, productRepository.count());
    }

    @Test
    void shouldNotCreateProductWithNonExistentCategory() {
        Map<String, Object> newProductMap = new HashMap<>();
        newProductMap.put("name", "New Product");
        newProductMap.put("description", "New Description");
        newProductMap.put("price", 15.99);
        newProductMap.put("available", true);
        
        Map<String, Object> categoryMap = new HashMap<>();
        categoryMap.put("id", 999L); // Non-existent category ID
        newProductMap.put("category", categoryMap);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + userToken)
                .body(newProductMap)
                .when()
                .post("/api/products")
                .then()
                .statusCode(400)
                .body("message", Matchers.equalTo("Referenced category not found"));

        assertEquals(0, productRepository.count());
    }

    @Test
    void authenticatedUserShouldUpdateProduct() {
        Product originalProduct = new Product(null, "Original Product", "Original Description", 10.0, true, category1);
        productRepository.save(originalProduct);

        Map<String, Object> updatedProductMap = new HashMap<>();
        updatedProductMap.put("name", "Updated Product");
        updatedProductMap.put("description", "Updated Description");
        updatedProductMap.put("price", 25.99);
        updatedProductMap.put("available", false);
        
        Map<String, Object> categoryMap = new HashMap<>();
        categoryMap.put("id", category2.getId());
        updatedProductMap.put("category", categoryMap);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + userToken)
                .body(updatedProductMap)
                .when()
                .put("/api/products/{id}", originalProduct.getId())
                .then()
                .statusCode(200)
                .body("id", Matchers.equalTo(originalProduct.getId().intValue()))
                .body("name", Matchers.equalTo("Updated Product"))
                .body("description", Matchers.equalTo("Updated Description"))
                .body("price", Matchers.equalTo(25.99f))
                .body("available", Matchers.equalTo(false))
                .body("category.id", Matchers.equalTo(category2.getId().intValue()));

        Product updatedProduct = productRepository.findById(originalProduct.getId()).orElse(null);
        assertEquals("Updated Product", updatedProduct.getName());
        assertEquals(category2.getId(), updatedProduct.getCategory().getId());
    }

    @Test
    void authenticatedUserShouldDeleteProduct() {
        Product product = new Product(null, "Product to Delete", "Will be deleted", 15.0, true, category1);
        product = productRepository.save(product);
        Long productId = product.getId();

        assertTrue(
                productRepository.existsById(productId),
                "Product should exist in database before deletion");

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + userToken) // Must use admin token
                .when()
                .delete("/api/products/{id}", productId)
                .then()
                .statusCode(Matchers.anyOf(
                        Matchers.equalTo(200),
                        Matchers.equalTo(204)
                ));

        assertFalse(
                productRepository.existsById(productId),
                "Product should not exist in database after deletion");

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + userToken)
                .when()
                .get("/api/products/{id}", productId)
                .then()
                .statusCode(404);
    }

    @Test
    void unAuthenticatedUserShouldNotDeleteProduct() {
        Product product = new Product(null, "Product to Delete", "Will be deleted", 15.0, true, category1);
        product = productRepository.save(product);
        Long productId = product.getId();

        assertTrue(
                productRepository.existsById(productId),
                "Product should exist in database before deletion");

        given()
                .contentType(ContentType.JSON)
                .when()
                .delete("/api/products/{id}", productId)
                .then()
                .statusCode(401);

        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/products/{id}", productId)
                .then()
                .statusCode(401);
    }    
}
