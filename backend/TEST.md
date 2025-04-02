# Running Tests

This document explains how to run the tests for this Spring Boot application using the provided test scripts.

## Prerequisites

- Java 17 or higher
- Maven (or use the provided Maven wrapper)

## Test Script Options

We've provided two script files to help you run tests easily:

- `run-tests.sh` - For Linux/macOS users
- `run-tests.bat` - For Windows users

Both scripts offer the same functionality with similar syntax.

## Common Usage Scenarios

### 1. Run All Tests

To run all tests in the project:

```bash
# Linux/macOS
./run-tests.sh --all

# Windows
run-tests.bat --all
```

### 2. Run a Specific Test Class

To run a specific test class:

```bash
# Linux/macOS
./run-tests.sh -c ProductControllerTest

# Windows
run-tests.bat -c ProductControllerTest
```

### 3. Run a Specific Test Method

To run a specific test method within a test class:

```bash
# Linux/macOS
./run-tests.sh -c ProductControllerTest -t shouldGetAllProducts

# Windows
run-tests.bat -c ProductControllerTest -t shouldGetAllProducts
```

### 4. Run Tests in a Specific Package

To run all tests in a specific package:

```bash
# Linux/macOS
./run-tests.sh -p com.fdabrao.app.controller

# Windows
run-tests.bat -p com.fdabrao.app.controller
```

### 5. Skip Tests During Build

To build the project without running tests:

```bash
# Linux/macOS
./run-tests.sh --skip-tests

# Windows
run-tests.bat --skip-tests
```

### 6. Run Tests in Debug Mode

To run tests in debug mode (useful for attaching a debugger):

```bash
# Linux/macOS
./run-tests.sh -c ProductControllerTest -d

# Windows
run-tests.bat -c ProductControllerTest -d
```

### 7. Get Help

To see all available options:

```bash
# Linux/macOS
./run-tests.sh --help

# Windows
run-tests.bat --help
```

## Running Tests with Maven Directly

You can also run tests directly with Maven commands:

```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw -Dtest=ProductControllerTest test

# Run specific test method
./mvnw -Dtest=ProductControllerTest#shouldGetAllProducts test
```

## Troubleshooting

If you encounter any issues running the tests:

1. Ensure that PostgreSQL is running if your tests require database access
2. Check that the application properties in `src/test/resources/application.properties` are correctly configured
3. Verify that all dependencies are properly resolved by running `./mvnw dependency:resolve`

## Test Report

After running tests, you can find the test reports in:

```
target/surefire-reports/
```

This directory contains detailed HTML and XML reports of test results. 