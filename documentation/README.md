# Spring Boot REST API - User & Role Management

Project Spring Boot lengkap dengan relasi Many-to-Many antara User dan Role menggunakan SQL Server, Hibernate JPA, dan Lombok.

## 🚀 Quick Start

### Prerequisites
- Java 21
- Maven
- SQL Server (running on localhost:1433)

### Configuration
Update database credentials di `src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:sqlserver://localhost:1433;databaseName=bootcamp_db;encrypt=true;trustServerCertificate=true
    username: sa
    password: YourPassword123
```

### Run Application
```bash
./mvnw spring-boot:run
```

Application akan berjalan di `http://localhost:8080`

## 📋 Features

- ✅ REST API dengan JSON request/response
- ✅ User & Role entities dengan Many-to-Many relationship
- ✅ Auto-create database tables (Hibernate DDL)
- ✅ Sample data initialization (ADMIN, USER roles + admin user)
- ✅ Custom repository query methods
- ✅ No infinite JSON loop (JsonManagedReference/JsonBackReference)
- ✅ SQL query logging

## 🗂️ Project Structure

```
src/main/java/com/example/demo/
├── config/
│   └── DataInitializer.java          # Sample data initialization
├── controller/
│   ├── UserController.java           # User REST endpoints
│   └── RoleController.java           # Role REST endpoints
├── entity/
│   ├── User.java                     # User entity
│   └── Role.java                     # Role entity
├── repository/
│   ├── UserRepository.java           # User repository
│   └── RoleRepository.java           # Role repository
├── service/
│   ├── UserService.java              # User service
│   └── RoleService.java              # Role service
└── BootcampJavaSpringApplication.java
```

## 🔌 API Endpoints

### Roles

**Get all roles**
```bash
GET http://localhost:8080/roles
```

**Create role**
```bash
POST http://localhost:8080/roles
Content-Type: application/json

{
  "name": "MODERATOR"
}
```

### Users

**Get all users**
```bash
GET http://localhost:8080/users
```

**Create user**
```bash
POST http://localhost:8080/users
Content-Type: application/json

{
  "username": "john",
  "email": "john@example.com",
  "password": "pass123",
  "isActive": true
}
```

**Create user with roles**
```bash
POST http://localhost:8080/users
Content-Type: application/json

{
  "username": "jane",
  "email": "jane@example.com",
  "password": "pass456",
  "isActive": true,
  "roles": [
    {"id": 1},
    {"id": 2}
  ]
}
```

## 🗄️ Database Schema

Hibernate akan membuat 3 tables:

### `users`
| Column    | Type         | Constraints        |
|-----------|--------------|-------------------|
| id        | BIGINT       | PRIMARY KEY, IDENTITY |
| username  | VARCHAR(255) | UNIQUE, NOT NULL  |
| email     | VARCHAR(255) |                   |
| password  | VARCHAR(255) |                   |
| is_active | BIT          |                   |

### `roles`
| Column | Type         | Constraints        |
|--------|--------------|-------------------|
| id     | BIGINT       | PRIMARY KEY, IDENTITY |
| name   | VARCHAR(255) | UNIQUE, NOT NULL  |

### `user_roles` (Junction Table)
| Column  | Type   | Constraints |
|---------|--------|-------------|
| user_id | BIGINT | FK → users.id |
| role_id | BIGINT | FK → roles.id |

## 📚 Documentation

Lihat [DOCUMENTATION.md](DOCUMENTATION.md) untuk:
- Penjelasan lengkap JPA annotations
- Mengapa terbentuk 3 tables
- Alur ORM dari HTTP request sampai database
- Contoh testing dengan Postman/curl
- Troubleshooting common issues

## 🛠️ Tech Stack

- **Java**: 21
- **Spring Boot**: 4.0.0
- **Build Tool**: Maven
- **Database**: SQL Server
- **ORM**: Hibernate (Spring Data JPA)
- **Utilities**: Lombok

## 📦 Dependencies

- spring-boot-starter-web
- spring-boot-starter-data-jpa
- lombok
- mssql-jdbc (runtime)
- spring-boot-starter-test

## 🎯 Sample Data

On startup, aplikasi akan insert:
- **Roles**: ADMIN, USER
- **User**: admin (username: admin, password: admin123) dengan role ADMIN dan USER

## 📝 Notes

- `ddl-auto: update` cocok untuk development. Untuk production, gunakan `validate` atau migration tools (Flyway/Liquibase)
- SQL queries akan tercetak di console karena `show-sql: true`
- Password disimpan plain text (untuk production, gunakan BCrypt)

## 🧪 Testing

Build project:
```bash
./mvnw clean install
```

Run tests:
```bash
./mvnw test
```

## 📄 License

Demo project for Spring Boot Bootcamp
