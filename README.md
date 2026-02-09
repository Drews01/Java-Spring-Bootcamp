# Loan Management System

A comprehensive Loan Management System built with Spring Boot 3.4.1 and Java 21, featuring role-based access control (RBAC), JWT authentication, and a complete loan workflow pipeline.

## 🚀 Features

- **Authentication & Authorization**
  - JWT-based authentication with HttpOnly cookies
  - Google OAuth 2.0 integration
  - Role-based access control (USER, MARKETING, BRANCH_MANAGER, BACK_OFFICE, ADMIN)
  - CSRF protection for SPA

- **Loan Workflow**
  - Complete loan application lifecycle
  - Multi-stage approval process (Marketing → Branch Manager → Back Office)
  - Real-time status tracking
  - Push notifications via Firebase Cloud Messaging (FCM)

- **User Management**
  - User profiles with KTP verification
  - Credit tier system (Bronze, Silver, Gold, Platinum)
  - Branch assignment for staff

- **Admin Features**
  - Dashboard analytics
  - User management
  - Product configuration
  - Branch management
  - RBAC menu access control

## 📋 Prerequisites

- **Java 21** (LTS)
- **Maven 3.8+**
- **SQL Server** (Production) or **H2** (Testing)
- **Redis** (for session/token management)
- **Docker** (optional, for containerized deployment)

## 🛠️ Tech Stack

| Component | Technology |
|-----------|------------|
| Framework | Spring Boot 3.4.1 |
| Language | Java 21 |
| Database | SQL Server / H2 |
| Cache | Redis |
| Security | Spring Security + JWT |
| Build Tool | Maven |
| Code Style | Google Java Format (Spotless) |
| Push Notifications | Firebase Admin SDK |
| Object Storage | Cloudflare R2 (S3-compatible) |

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/Drews01/Java-Spring-Bootcamp.git
cd Java-Spring-Bootcamp
```

### 2. Configure Environment Variables

Create or update `application.properties` or set environment variables:

```properties
# Database
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=loandb
spring.datasource.username=your_username
spring.datasource.password=your_password

# JWT
JWT_SECRET_KEY=your-secret-key-at-least-256-bits

# Redis
spring.redis.host=localhost
spring.redis.port=6379

# Google OAuth (optional)
app.google.client-id=your-google-client-id

# Email (optional)
spring.mail.host=smtp.gmail.com
spring.mail.username=your-email
spring.mail.password=your-app-password
```

### 3. Build and Run

```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

Or use the provided batch script:

```bash
./run-app.bat
```

### 4. Access the API

- Base URL: `http://localhost:8080`
- API Documentation: See [API_DOCUMENTATION.md](documentation/API_DOCUMENTATION.md)

## 🧪 Running Tests

```bash
# Run all tests
mvn test

# Run with coverage report
mvn test jacoco:report
```

## 📁 Project Structure

```
src/main/java/com/example/demo/
├── base/           # Base response wrappers
├── config/         # Configuration classes
├── controller/     # REST API Controllers
├── dto/            # Data Transfer Objects
├── entity/         # JPA Entities
├── enums/          # Enumerations
├── exception/      # Custom exceptions & handlers
├── repository/     # Spring Data JPA Repositories
├── security/       # Security configuration & JWT
├── service/        # Business logic
│   ├── impl/       # Service implementations
│   └── notification/  # Notification channels
└── util/           # Utility classes
```

## 🔐 Security

- **JWT Authentication**: Tokens stored in HttpOnly cookies
- **CSRF Protection**: Cookie-based tokens for SPA
- **Password Encryption**: BCrypt encoder
- **SQL Injection Prevention**: JPA parameter binding
- **Input Validation**: Bean Validation annotations

## 🔄 Loan Workflow States

```
SUBMITTED → IN_REVIEW → WAITING_APPROVAL → APPROVED_WAITING_DISBURSEMENT → DISBURSED
     ↓          ↓              ↓
  REJECTED   RETURNED      REJECTED
```

## 📖 API Documentation

See [API_DOCUMENTATION.md](documentation/API_DOCUMENTATION.md) for complete API reference.

## 🏗️ Architecture

See [Code Architecture Analysis](documentation/code-architecture-analysis.md) for detailed architecture documentation.

## 🐳 Docker Deployment

```bash
# Build and run with Docker Compose
cd deploy
docker-compose up --build
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License.

---

