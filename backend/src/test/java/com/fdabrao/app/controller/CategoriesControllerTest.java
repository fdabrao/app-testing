package com.fdabrao.app.controller;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.fdabrao.app.model.Categories;
import com.fdabrao.app.model.User;
import com.fdabrao.app.persistence.CategoriesRepository;
import com.fdabrao.app.persistence.UserRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.time.LocalDateTime;
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
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CategoriesControllerTest {

    @LocalServerPort
    private Integer port;

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            "postgres:16-alpine");

    @Autowired
    CategoriesRepository categoriesRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    private String userToken;

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
        categoriesRepository.deleteAll();
        userRepository.deleteAll();

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
        user.setCreatedAt(LocalDateTime.now());
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
    void authenticatedUserShouldGetAllCategories() {
        List<Categories> categories = List.of(
                new Categories(null, "Category 1", "Description 1", null, true),
                new Categories(null, "Category 2", "Description 2", "Category 1", true));
        categoriesRepository.saveAll(categories);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + userToken)
                .when()
                .get("/api/categories")
                .then()
                .statusCode(200)
                .body(".", Matchers.hasSize(2));
    }

    @Test
    void shouldGetOnlyActiveCategories() {
        List<Categories> categories = List.of(
                new Categories(null, "Category 1", "Description 1", null, true),
                new Categories(null, "Category 2", "Description 2", null, false));
        categoriesRepository.saveAll(categories);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + userToken)
                .queryParam("activeOnly", true)
                .when()
                .get("/api/categories")
                .then()
                .statusCode(200)
                .body(".", Matchers.hasSize(1))
                .body("[0].name", Matchers.equalTo("Category 1"));
    }

    @Test
    void unauthenticatedUserShouldNotGetAllCategories() {
        List<Categories> categories = List.of(
                new Categories(null, "Category 1", "Description 1", null, true),
                new Categories(null, "Category 2", "Description 2", null, true));
        categoriesRepository.saveAll(categories);

        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/categories")
                .then()
                .statusCode(403);
    }

    @Test
    void authenticatedUserShouldGetCategoryById() {
        Categories category = new Categories(null, "Category 1", "Description 1", null, true);
        categoriesRepository.save(category);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + userToken)
                .when()
                .get("/api/categories/{id}", category.getId())
                .then()
                .statusCode(200)
                .body("id", Matchers.equalTo(category.getId().intValue()))
                .body("name", Matchers.equalTo(category.getName()))
                .body("description", Matchers.equalTo(category.getDescription()))
                .body("active", Matchers.equalTo(category.getActive()));
    }

    @Test
    void shouldGetCategoriesByParent() {
        String parentCategory = "Parent Category";
        List<Categories> categories = List.of(
                new Categories(null, "Parent Category", "Parent Description", null, true),
                new Categories(null, "Child Category 1", "Child Description 1", parentCategory, true),
                new Categories(null, "Child Category 2", "Child Description 2", parentCategory, true));
        categoriesRepository.saveAll(categories);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + userToken)
                .when()
                .get("/api/categories/byParent/{parentCategory}", parentCategory)
                .then()
                .statusCode(200)
                .body(".", Matchers.hasSize(2))
                .body("[0].parentCategory", Matchers.equalTo(parentCategory))
                .body("[1].parentCategory", Matchers.equalTo(parentCategory));
    }

    @Test
    void authenticatedUserShouldCreateNewCategory() {
        Categories newCategory = new Categories(
                null,
                "New Category",
                "New Description",
                null,
                true);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + userToken)
                .body(newCategory)
                .when()
                .post("/api/categories")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("name", Matchers.equalTo(newCategory.getName()))
                .body("description", Matchers.equalTo(newCategory.getDescription()))
                .body("active", Matchers.equalTo(newCategory.getActive()));

        assertEquals(1, categoriesRepository.count());
    }

    @Test
    void shouldNotCreateCategoryWithDuplicateName() {
        // First create a category
        Categories existingCategory = new Categories(
                null, "Existing Category", "Description", null, true);
        categoriesRepository.save(existingCategory);

        // Try to create another with the same name
        Categories duplicateCategory = new Categories(
                null, "Existing Category", "Different Description", null, true);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + userToken)
                .body(duplicateCategory)
                .when()
                .post("/api/categories")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("message", Matchers.equalTo("Category name already exists"));

        assertEquals(1, categoriesRepository.count());
    }

    @Test
    void authenticatedUserShouldUpdateExistingCategory() {
        // First create a category
        Categories originalCategory = new Categories(
                null, "Original Category", "Original Description", null, true);
        categoriesRepository.save(originalCategory);

        // Update fields
        Categories updatedCategory = new Categories(
                originalCategory.getId(),
                "Updated Category",
                "Updated Description",
                "Parent Category",
                true);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + userToken)
                .body(updatedCategory)
                .when()
                .put("/api/categories/{id}", originalCategory.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("id", Matchers.equalTo(originalCategory.getId().intValue()))
                .body("name", Matchers.equalTo(updatedCategory.getName()))
                .body("description", Matchers.equalTo(updatedCategory.getDescription()))
                .body("parentCategory", Matchers.equalTo(updatedCategory.getParentCategory()))
                .body("active", Matchers.equalTo(updatedCategory.getActive()));

        // Verify updated in database
        Categories savedCategory = categoriesRepository.findById(originalCategory.getId()).orElse(null);
        assertEquals("Updated Category", savedCategory.getName());
        assertEquals("Updated Description", savedCategory.getDescription());
    }

    @Test
    void shouldNotUpdateToExistingCategoryName() {
        // Create two categories
        Categories category1 = new Categories(
                null, "Category 1", "Description 1", null, true);
        Categories category2 = new Categories(
                null, "Category 2", "Description 2", null, true);
        categoriesRepository.saveAll(List.of(category1, category2));

        // Try to update category2 to have the same name as category1
        category2.setName("Category 1");

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + userToken)
                .body(category2)
                .when()
                .put("/api/categories/{id}", category2.getId())
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("message", Matchers.equalTo("Another category with this name already exists"));

        // Verify not updated in database
        Categories savedCategory = categoriesRepository.findById(category2.getId()).orElse(null);
        assertEquals("Category 2", savedCategory.getName());
    }

    @Test
    void authenticatedUserShouldDeleteCategory() {
        Categories category = new Categories(
                null, "Category to Delete", "Description", null, true);
        categoriesRepository.save(category);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + userToken)
                .when()
                .delete("/api/categories/{id}", category.getId())
                .then()
                .statusCode(HttpStatus.OK.value());

        // Verify deleted from database
        assertFalse(categoriesRepository.existsById(category.getId()));
    }

    @Test
    void shouldReturnNotFoundForNonExistentCategory() {
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + userToken)
                .when()
                .get("/api/categories/{id}", 999L)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }
} 