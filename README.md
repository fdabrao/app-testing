# Product Management System

A full-stack application for managing product categories and inventory, built with Spring Boot backend and Angular frontend.

## Project Structure

The project is organized into two main components:

- **Backend**: Spring Boot application providing RESTful APIs
- **Frontend**: Angular 19 application for the user interface

## Technologies Used

### Backend
- Java 17
- Spring Boot
- Spring Data JPA
- Spring Security with JWT
- H2 Database (for development)
- Maven

### Frontend
- Angular 19
- TypeScript
- RxJS
- SCSS for styling
- Cypress for E2E testing

## Getting Started

### Prerequisites
- Java 17 or higher
- Node.js (v18 or higher)
- npm (v9 or higher)
- Git

### Clone the Repository

```bash
git clone <repository-url>
cd product-management-system
```

### Running the Backend

Navigate to the backend directory and start the Spring Boot application:

```bash
cd backend
./mvnw spring-boot:run
```

The backend server will start on http://localhost:8080.

### Running the Frontend

Open a new terminal, navigate to the frontend directory, and start the Angular application:

```bash
cd frontend
npm install
npm start
```

The frontend application will be available at http://localhost:4200.

### Running with the Start Script

Alternatively, you can use the provided script to start both applications:

```bash
# On Windows
start-system.bat

# On Linux/macOS
./start-system.sh
```

## Testing

### Backend Tests

```bash
cd backend
./mvnw test
```

### Frontend Unit Tests

```bash
cd frontend
npm test
```

### End-to-End Tests

```bash
cd frontend
npm run cypress:open   # For interactive testing
npm run cypress:run    # For headless testing
```

## Development Guidelines

### Git Workflow

1. Create a feature branch from `main`:
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. Make your changes and commit them with meaningful messages:
   ```bash
   git add .
   git commit -m "Add description of changes"
   ```

3. Push your branch and create a pull request:
   ```bash
   git push origin feature/your-feature-name
   ```

### Code Style

- **Backend**: Follow the Google Java Style Guide
- **Frontend**: Follow the Angular style guide

## Deployment

### Backend

Build the JAR file:

```bash
cd backend
./mvnw clean package
```

The JAR file will be available in the `target` directory.

### Frontend

Build the production version:

```bash
cd frontend
npm run build
```

The compiled output will be available in the `dist` directory.

## Environment Configuration

### Backend

Configuration files are located in:
- `src/main/resources/application.properties` (or `application.yml`)

For production, create a separate `application-prod.properties` file.

### Frontend

Environment configurations are in:
- `src/environments/environment.ts` (development)
- `src/environments/environment.prod.ts` (production)

## License

[Your License Information]
