# Spring Boot REST API - Documentation
Ketika ingin membuat pokoknya, Entity itu harus dihubungkan dengan Repository, repository itu harus dihubungkan dengan Service, service itu harus dihubungkan dengan Controller.


## Table of Contents
1. [JPA Annotations Explained](#jpa-annotations-explained)
2. [Database Table Structure](#database-table-structure)
3. [ORM Flow: Request to Database](#orm-flow-request-to-database)
4. [API Testing Guide](#api-testing-guide)

---

## JPA Annotations Explained

### Entity-Level Annotations

#### `@Entity`
Menandai class sebagai JPA entity yang akan di-mapping ke table database.
```java
@Entity
public class User { ... }
```

#### `@Table(name = "users")`
Menentukan nama table di database. Jika tidak disebutkan, JPA akan menggunakan nama class.
```java
@Table(name = "users")  // Table akan bernama "users" bukan "User"
```

---

### Field-Level Annotations

#### `@Id`
Menandai field sebagai primary key.
```java
@Id
private Long id;
```

#### `@GeneratedValue(strategy = GenerationType.IDENTITY)`
Menentukan strategi auto-increment untuk primary key. `IDENTITY` menggunakan auto-increment database (SQL Server: IDENTITY).
```java
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
```

#### `@Column`
Konfigurasi kolom database:
- `unique = true`: Nilai harus unik (akan membuat UNIQUE constraint)
- `nullable = false`: Kolom tidak boleh NULL (NOT NULL constraint)
- `name = "is_active"`: Nama kolom di database (jika berbeda dari field name)

```java
@Column(unique = true, nullable = false)
private String username;

@Column(name = "is_active")
private Boolean isActive;  // Di database: is_active
```

---

### Relationship Annotations

#### `@ManyToMany`
Mendefinisikan relasi Many-to-Many antara dua entity.

**Owning Side (User.java):**
```java
@ManyToMany(fetch = FetchType.EAGER)
@JoinTable(
    name = "user_roles",              // Nama junction table
    joinColumns = @JoinColumn(name = "user_id"),        // FK ke User
    inverseJoinColumns = @JoinColumn(name = "role_id")  // FK ke Role
)
private Set<Role> roles = new HashSet<>();
```

- `fetch = FetchType.EAGER`: Data roles akan langsung di-load saat User di-query
- `@JoinTable`: Mendefinisikan junction table untuk Many-to-Many
- `joinColumns`: Foreign key ke entity ini (User)
- `inverseJoinColumns`: Foreign key ke entity lain (Role)

**Inverse Side (Role.java):**
```java
@ManyToMany(mappedBy = "roles")
private Set<User> users = new HashSet<>();
```

- `mappedBy = "roles"`: Menunjukkan bahwa relasi ini adalah inverse side, dan field `roles` di User adalah owning side

---

### JSON Serialization Annotations

#### `@JsonManagedReference` dan `@JsonBackReference`
Mencegah infinite loop saat serialisasi JSON pada bidirectional relationship.

**Owning Side (User.java):**
```java
@JsonManagedReference
private Set<Role> roles;
```

**Inverse Side (Role.java):**
```java
@JsonBackReference
private Set<User> users;
```

**Hasil:**
- Saat GET `/users`: User akan include roles, tapi Role TIDAK akan include users
- Saat GET `/roles`: Role akan ditampilkan, tapi field users akan di-ignore

---

### Lombok Annotations

#### `@Data`
Generate getter, setter, toString, equals, dan hashCode.

#### `@Builder`
Generate builder pattern untuk object creation.
```java
User user = User.builder()
    .username("john")
    .email("john@example.com")
    .build();
```

#### `@NoArgsConstructor` dan `@AllArgsConstructor`
Generate constructor tanpa parameter dan constructor dengan semua parameter.

#### `@RequiredArgsConstructor`
Generate constructor untuk final fields (digunakan untuk dependency injection).

---

## Database Table Structure

### Kenapa Terbentuk 3 Table?

Hibernate akan membuat **3 table** dari 2 entity karena relasi Many-to-Many:

#### 1. Table `users`
```sql
CREATE TABLE users (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255),
    password VARCHAR(255),
    is_active BIT
);
```

#### 2. Table `roles`
```sql
CREATE TABLE roles (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL
);
```

#### 3. Table `user_roles` (Junction Table)
```sql
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (role_id) REFERENCES roles(id)
);
```

**Mengapa Junction Table Diperlukan?**

Many-to-Many relationship tidak bisa direpresentasikan langsung dalam relational database. Contoh:
- 1 User bisa punya banyak Role
- 1 Role bisa dimiliki banyak User

Junction table `user_roles` menyimpan pasangan (user_id, role_id) untuk merepresentasikan relasi ini.

**Contoh Data:**

**users:**
| id | username | email | password | is_active |
|----|----------|-------|----------|-----------|
| 1  | admin    | admin@example.com | admin123 | 1 |
| 2  | john     | john@example.com | pass123 | 1 |

**roles:**
| id | name  |
|----|-------|
| 1  | ADMIN |
| 2  | USER  |

**user_roles:**
| user_id | role_id |
|---------|---------|
| 1       | 1       |
| 1       | 2       |
| 2       | 2       |

Artinya:
- User `admin` (id=1) punya role ADMIN (id=1) dan USER (id=2)
- User `john` (id=2) punya role USER (id=2)

---

## ORM Flow: Request to Database

### Alur Lengkap dari HTTP Request sampai Database

#### **1. GET /users - Retrieve All Users**

```
HTTP Request (Client)
    ↓
UserController.getAllUsers()
    ↓
UserService.getAllUsers()
    ↓
UserRepository.findAll()
    ↓
Spring Data JPA generates SQL
    ↓
Hibernate executes SQL:
    SELECT u.*, r.* 
    FROM users u 
    LEFT JOIN user_roles ur ON u.id = ur.user_id
    LEFT JOIN roles r ON ur.role_id = r.id
    ↓
SQL Server returns ResultSet
    ↓
Hibernate maps ResultSet to User objects (with Set<Role>)
    ↓
UserService returns List<User>
    ↓
UserController returns ResponseEntity<List<User>>
    ↓
Jackson converts List<User> to JSON (using @JsonManagedReference/@JsonBackReference)
    ↓
HTTP Response (JSON) sent to Client
```

**SQL yang di-generate Hibernate:**
```sql
SELECT 
    u.id, u.username, u.email, u.password, u.is_active,
    r.id, r.name
FROM users u
LEFT JOIN user_roles ur ON u.id = ur.user_id
LEFT JOIN roles r ON ur.role_id = r.id
```

**JSON Response:**
```json
[
  {
    "id": 1,
    "username": "admin",
    "email": "admin@example.com",
    "password": "admin123",
    "isActive": true,
    "roles": [
      {"id": 1, "name": "ADMIN"},
      {"id": 2, "name": "USER"}
    ]
  }
]
```

---

#### **2. POST /users - Create New User**

```
HTTP Request (Client) with JSON body
    ↓
Jackson deserializes JSON to User object
    ↓
UserController.createUser(User user)
    ↓
UserService.createUser(User user)
    ↓
UserRepository.save(user)
    ↓
Hibernate checks if entity is new (id == null)
    ↓
Hibernate executes INSERT:
    INSERT INTO users (username, email, password, is_active) 
    VALUES (?, ?, ?, ?)
    ↓
SQL Server generates ID (IDENTITY) and returns it
    ↓
Hibernate sets the generated ID to User object
    ↓
If user has roles, Hibernate executes:
    INSERT INTO user_roles (user_id, role_id) VALUES (?, ?)
    (for each role)
    ↓
Hibernate returns managed User entity
    ↓
UserService returns User
    ↓
UserController returns ResponseEntity with status 201 CREATED
    ↓
Jackson converts User to JSON
    ↓
HTTP Response (JSON) sent to Client
```

**SQL yang di-generate Hibernate:**
```sql
-- Insert user
INSERT INTO users (username, email, password, is_active) 
VALUES ('john', 'john@example.com', 'pass123', 1);

-- Get generated ID
SELECT SCOPE_IDENTITY();

-- Insert user-role relationships
INSERT INTO user_roles (user_id, role_id) VALUES (2, 2);
```

---

### Komponen ORM

1. **Entity (User, Role)**: POJO yang merepresentasikan table
2. **Repository (UserRepository, RoleRepository)**: Interface untuk database operations
3. **Spring Data JPA**: Auto-generate implementation dari repository
4. **Hibernate**: ORM engine yang mengkonversi Java objects ↔ SQL
5. **JDBC Driver (mssql-jdbc)**: Koneksi ke SQL Server
6. **SQL Server**: Database yang menyimpan data

---

## API Testing Guide

### Prerequisites
1. SQL Server harus running
2. Update credentials di `application.yml` jika perlu
3. Run aplikasi: `./mvnw spring-boot:run`

---

### Test Endpoints dengan Postman

#### 1. Get All Roles
```
GET http://localhost:8080/roles
```

**Expected Response (200 OK):**
```json
[
  {"id": 1, "name": "ADMIN"},
  {"id": 2, "name": "USER"}
]
```

---

#### 2. Get All Users
```
GET http://localhost:8080/users
```

**Expected Response (200 OK):**
```json
[
  {
    "id": 1,
    "username": "admin",
    "email": "admin@example.com",
    "password": "admin123",
    "isActive": true,
    "roles": [
      {"id": 1, "name": "ADMIN"},
      {"id": 2, "name": "USER"}
    ]
  }
]
```

**Note:** Roles akan muncul dalam response, tapi tidak ada infinite loop karena `@JsonBackReference` di Role.users.

---

#### 3. Create New Role
```
POST http://localhost:8080/roles
Content-Type: application/json

{
  "name": "MODERATOR"
}
```

**Expected Response (201 CREATED):**
```json
{
  "id": 3,
  "name": "MODERATOR"
}
```

---

#### 4. Create New User (Without Roles)
```
POST http://localhost:8080/users
Content-Type: application/json

{
  "username": "john",
  "email": "john@example.com",
  "password": "pass123",
  "isActive": true
}
```

**Expected Response (201 CREATED):**
```json
{
  "id": 2,
  "username": "john",
  "email": "john@example.com",
  "password": "pass123",
  "isActive": true,
  "roles": []
}
```

---

#### 5. Create New User (With Existing Roles)
```
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

**Expected Response (201 CREATED):**
```json
{
  "id": 3,
  "username": "jane",
  "email": "jane@example.com",
  "password": "pass456",
  "isActive": true,
  "roles": [
    {"id": 1, "name": "ADMIN"},
    {"id": 2, "name": "USER"}
  ]
}
```

**Note:** Anda harus menyertakan role yang sudah ada (by id). Hibernate akan membuat relasi di table `user_roles`.

---

### Test dengan cURL

#### Get All Users
```bash
curl http://localhost:8080/users
```

#### Create Role
```bash
curl -X POST http://localhost:8080/roles \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"MODERATOR\"}"
```

#### Create User
```bash
curl -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"john\",\"email\":\"john@example.com\",\"password\":\"pass123\",\"isActive\":true}"
```

---

## Verifikasi di Console

Karena `show-sql: true` di `application.yml`, semua SQL query akan tercetak di console:

```
Hibernate: 
    insert 
    into
        users
        (email, is_active, password, username) 
    values
        (?, ?, ?, ?)
        
Hibernate: 
    insert 
    into
        user_roles
        (user_id, role_id) 
    values
        (?, ?)
```

Ini berguna untuk debugging dan memahami query yang di-generate Hibernate.

---

## Common Issues

### 1. SQL Server Connection Failed
**Error:** `Cannot create PoolableConnectionFactory`

**Solution:**
- Pastikan SQL Server running
- Cek credentials di `application.yml`
- Cek port (default: 1433)
- Untuk SQL Server Express, gunakan: `localhost\\SQLEXPRESS`

### 2. Table Already Exists
**Error:** `Table 'users' already exists`

**Solution:**
- Ubah `ddl-auto: update` menjadi `ddl-auto: create-drop` untuk recreate tables
- Atau drop tables manual di SQL Server

### 3. Infinite JSON Loop
**Error:** StackOverflowError saat serialize JSON

**Solution:**
- Pastikan `@JsonManagedReference` di owning side (User.roles)
- Pastikan `@JsonBackReference` di inverse side (Role.users)

### 4. Lombok Not Working
**Error:** Cannot find symbol getter/setter

**Solution:**
- Pastikan Lombok plugin installed di IDE
- Enable annotation processing di IDE settings
- Rebuild project

---

## Summary

✅ **Entity Layer**: User dan Role dengan Many-to-Many relationship  
✅ **Repository Layer**: JpaRepository dengan custom query methods  
✅ **Service Layer**: Business logic untuk CRUD operations  
✅ **Controller Layer**: REST endpoints untuk User dan Role  
✅ **Configuration**: SQL Server + Hibernate auto DDL  
✅ **Data Initialization**: Sample data via CommandLineRunner  
✅ **JSON Serialization**: No infinite loop dengan @JsonManagedReference/@JsonBackReference  

Project siap dijalankan dan di-test! 🚀
