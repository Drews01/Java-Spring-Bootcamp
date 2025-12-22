# Project Documentation
Entity -- Repository -- service -- Controller
## Project Overview
This is a Spring Boot REST API project with JWT authentication. The code is organized under `src/main/java/com/example/demo` and follows common Spring layered architecture: controllers -> services -> repositories -> entities. Security components provide JWT issuance and filtering. Configuration and initial data setup are included.

**Main entry point**: [src/main/java/com/example/demo/BootcampJavaSpringApplication.java](src/main/java/com/example/demo/BootcampJavaSpringApplication.java)

## Top-level files
- **pom.xml**: Maven build configuration and dependencies.
- **mvnw, mvnw.cmd**: Maven wrapper scripts for Unix/Windows.
- **run-app.bat**: Convenience Windows script to run the app.
- **application.yml** (in `src/main/resources`): Application configuration (DB, JWT settings, etc.).

## Folder structure and explanations

**src/main/java/com/example/demo**
- **base**: Shared response and error models used across controllers and exception handling.
  - [src/main/java/com/example/demo/base/ApiResponse.java](src/main/java/com/example/demo/base/ApiResponse.java)
  - [src/main/java/com/example/demo/base/ErrorDetails.java](src/main/java/com/example/demo/base/ErrorDetails.java)

- **config**: Application configuration and bootstrap components.
  - `DataInitializer.java`: seeds initial roles, users, or products on startup.
  - `SecurityConfig.java`: configures Spring Security (authentication manager, filter chain, password encoder, protected endpoints).
  - Files: [src/main/java/com/example/demo/config/DataInitializer.java](src/main/java/com/example/demo/config/DataInitializer.java), [src/main/java/com/example/demo/config/SecurityConfig.java](src/main/java/com/example/demo/config/SecurityConfig.java)

- **controller**: REST controllers exposing endpoints to clients. Controllers handle HTTP requests, validate input, and delegate to services.
  - `AuthController.java`: login/register endpoints, returns JWT tokens. [src/main/java/com/example/demo/controller/AuthController.java](src/main/java/com/example/demo/controller/AuthController.java)
  - `UserController.java`, `RoleController.java`, `ProductController.java`: resource-specific endpoints.

- **dto**: Data Transfer Objects for requests/responses. Controllers accept DTOs and services return DTOs where appropriate.
  - Examples: `AuthRequest`, `AuthResponse`, `RegisterRequest`. [src/main/java/com/example/demo/dto](src/main/java/com/example/demo/dto)

- **entity**: JPA entity classes that map to database tables.
  - `User`, `Role`, `Product` with relationships and JPA annotations. Entities are persisted via repositories. [src/main/java/com/example/demo/entity](src/main/java/com/example/demo/entity)

- **exception**: Centralized exception handling for controllers.
  - `GlobalExceptionHandler.java`: maps exceptions to HTTP responses and uses `ErrorDetails` from `/base`. [src/main/java/com/example/demo/exception/GlobalExceptionHandler.java](src/main/java/com/example/demo/exception/GlobalExceptionHandler.java)

- **repository**: Spring Data JPA repositories for CRUD and query operations against entities.
  - `UserRepository.java`, `RoleRepository.java`, `ProductRepository.java` used by services. [src/main/java/com/example/demo/repository](src/main/java/com/example/demo/repository)

- **security**: Security-related classes for authentication and JWT handling.
  - `CustomUserDetails.java`, `CustomUserDetailsService.java`: adapt `User` to Spring Security's `UserDetails` and load users.
  - `JwtService.java`: create/validate JWT tokens.
  - `JwtAuthFilter.java`: filter that extracts JWT from requests and sets authentication.
  - Files: [src/main/java/com/example/demo/security](src/main/java/com/example/demo/security)

- **service**: Business logic layer. Services orchestrate operations, perform validation, and call repositories.
  - `AuthService.java`: handles authentication, registration, token creation.
  - `UserService.java`, `RoleService.java`, `ProductService.java`: domain-specific logic. [src/main/java/com/example/demo/service](src/main/java/com/example/demo/service)

**src/main/resources**
- **application.yml**: environment and Spring Boot configuration. [src/main/resources/application.yml](src/main/resources/application.yml)
- **static** and **templates**: static assets and server-side templates (if used).

**src/test**
- Test classes and test resources mirroring the main package structure. Use Maven/IDE to run tests.

**target**
- Build output directory created by Maven. Contains compiled classes and packaged JAR.

## How the pieces connect (request flow)
- Client -> Controller: HTTP requests hit a controller (`AuthController`, `ProductController`, etc.).
- Controller -> Service: Controllers delegate business logic to services (`AuthService`, `ProductService`).
- Service -> Repository: Services use repositories (`UserRepository`, `ProductRepository`) to persist/fetch `entity` objects.
- Entities -> Database: JPA maps entity classes to DB tables configured in `application.yml`.
- Security flow: on protected endpoints,
  - `JwtAuthFilter` extracts token -> `JwtService` validates -> `CustomUserDetailsService` loads user -> Spring Security context is populated -> controllers see authenticated principal.
- Exceptions thrown anywhere bubble to `GlobalExceptionHandler` which converts them to structured HTTP error responses using `ErrorDetails`.

## Key files and purpose (quick reference)
- [BootcampJavaSpringApplication.java](src/main/java/com/example/demo/BootcampJavaSpringApplication.java): application startup.
- [SecurityConfig.java](src/main/java/com/example/demo/config/SecurityConfig.java): security rules and filter chain.
- [JwtService.java](src/main/java/com/example/demo/security/JwtService.java): token generation and validation.
- [AuthService.java](src/main/java/com/example/demo/service/AuthService.java): registration/login logic.
- [GlobalExceptionHandler.java](src/main/java/com/example/demo/exception/GlobalExceptionHandler.java): centralized error handling.
- [application.yml](src/main/resources/application.yml): DB and JWT configuration.

## Running the project
On Windows (from repository root):

```bash
mvnw.cmd spring-boot:run
```
Or run the wrapper on Unix-like shells:

```bash
./mvnw spring-boot:run
```
Or use the provided `run-app.bat`:

```bash
run-app.bat
```

Build the jar:

```bash
mvnw.cmd clean package
```

## Tests
Run unit/integration tests with:

```bash
mvnw.cmd test
```

## Suggested next steps for documentation
- Add API endpoint list with request/response examples (OpenAPI/Swagger would help).
- Document `application.yml` properties and required environment variables (DB URL, JWT secret, etc.).
- Add sequence diagrams for authentication and typical CRUD flows.

---

This file was auto-generated. If you want, I can: add an endpoint-by-endpoint API reference, generate an OpenAPI spec, or integrate Swagger UI. Let me know which next step you prefer.
