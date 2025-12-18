# Cara Menjalankan dan Testing Spring Boot Application

## 🚀 Cara Menjalankan Aplikasi

### 1. Pastikan SQL Server Running
- SQL Server harus sudah running
- Windows Authentication sudah enabled
- Database "LoanDatabase" sudah ada (atau akan dibuat otomatis)

### 2. Jalankan Aplikasi
```bash
cd "c:\Users\Andrew\Desktop\Bootcamp\Spring Java Bootcamp\Java Spring Bootcamp"
./mvnw spring-boot:run
```

### 3. Tunggu Sampai Aplikasi Running
Anda akan melihat output seperti ini:
```
✓ Data initialization completed!
✓ Roles created: ADMIN, USER
✓ Sample user created: admin (with ADMIN and USER roles)

Started BootcampJavaSpringApplication in 3.5 seconds
```

**Application berjalan di:** `http://localhost:8080`

---

## 🧪 Cara Testing Endpoints

### Opsi 1: Menggunakan Browser (untuk GET requests)

#### Test GET All Roles
Buka browser dan akses:
```
http://localhost:8080/roles
```

**Expected Response:**
```json
[
  {"id": 1, "name": "ADMIN"},
  {"id": 2, "name": "USER"}
]
```

#### Test GET All Users
```
http://localhost:8080/users
```

**Expected Response:**
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

### Opsi 2: Menggunakan PowerShell (curl)

#### GET All Roles
```powershell
curl http://localhost:8080/roles
```

#### GET All Users
```powershell
curl http://localhost:8080/users
```

#### POST Create New Role
```powershell
curl -Method POST -Uri "http://localhost:8080/roles" `
  -ContentType "application/json" `
  -Body '{"name":"MODERATOR"}'
```

#### POST Create New User (tanpa roles)
```powershell
curl -Method POST -Uri "http://localhost:8080/users" `
  -ContentType "application/json" `
  -Body '{"username":"john","email":"john@example.com","password":"pass123","isActive":true}'
```

#### POST Create New User (dengan roles)
```powershell
curl -Method POST -Uri "http://localhost:8080/users" `
  -ContentType "application/json" `
  -Body '{"username":"jane","email":"jane@example.com","password":"pass456","isActive":true,"roles":[{"id":1},{"id":2}]}'
```

---

### Opsi 3: Menggunakan Postman (Recommended)

#### Setup Postman
1. Download Postman dari https://www.postman.com/downloads/
2. Install dan buka Postman
3. Create New Collection: "Spring Boot User-Role API"

#### Test 1: GET All Roles
- **Method:** GET
- **URL:** `http://localhost:8080/roles`
- **Headers:** (tidak perlu)
- **Click Send**

#### Test 2: GET All Users
- **Method:** GET
- **URL:** `http://localhost:8080/users`
- **Headers:** (tidak perlu)
- **Click Send**

#### Test 3: POST Create Role
- **Method:** POST
- **URL:** `http://localhost:8080/roles`
- **Headers:** 
  - Key: `Content-Type`
  - Value: `application/json`
- **Body:** (pilih raw, JSON)
```json
{
  "name": "MODERATOR"
}
```
- **Click Send**

#### Test 4: POST Create User
- **Method:** POST
- **URL:** `http://localhost:8080/users`
- **Headers:** 
  - Key: `Content-Type`
  - Value: `application/json`
- **Body:** (pilih raw, JSON)
```json
{
  "username": "john",
  "email": "john@example.com",
  "password": "pass123",
  "isActive": true
}
```
- **Click Send**

#### Test 5: POST Create User with Roles
- **Method:** POST
- **URL:** `http://localhost:8080/users`
- **Headers:** 
  - Key: `Content-Type`
  - Value: `application/json`
- **Body:** (pilih raw, JSON)
```json
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
- **Click Send**

---

## 📊 Verifikasi di Database

Setelah testing, Anda bisa cek database SQL Server:

### Cek Tables
```sql
USE LoanDatabase;

-- Lihat semua tables
SELECT * FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_TYPE = 'BASE TABLE';

-- Expected: users, roles, user_roles
```

### Cek Data Roles
```sql
SELECT * FROM roles;
```
**Expected:**
| id | name |
|----|------|
| 1  | ADMIN |
| 2  | USER |

### Cek Data Users
```sql
SELECT * FROM users;
```

### Cek User-Role Relationships
```sql
SELECT 
    u.username,
    r.name as role_name
FROM users u
JOIN user_roles ur ON u.id = ur.user_id
JOIN roles r ON ur.role_id = r.id;
```

**Expected:**
| username | role_name |
|----------|-----------|
| admin    | ADMIN     |
| admin    | USER      |

---

## 🛑 Cara Stop Aplikasi

Di terminal/PowerShell tempat aplikasi running, tekan:
```
Ctrl + C
```

---

## 🔍 Troubleshooting

### Issue: Port 8080 already in use
**Error:** `Port 8080 is already in use`

**Solution:** Ubah port di `application.yml`:
```yaml
server:
  port: 8081
```

### Issue: Cannot connect to SQL Server
**Error:** `Cannot create PoolableConnectionFactory`

**Solution:**
1. Pastikan SQL Server running
2. Cek Windows Authentication enabled
3. Cek database name: "LoanDatabase"
4. Cek connection string di `application.yml`

### Issue: Tables tidak dibuat
**Solution:** Cek console log, pastikan ada:
```
Hibernate: create table roles (...)
Hibernate: create table users (...)
Hibernate: create table user_roles (...)
```

Jika tidak ada, cek `ddl-auto: update` di `application.yml`

---

## 📝 Quick Reference

### Endpoints Summary
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET    | /roles   | Get all roles |
| POST   | /roles   | Create new role |
| GET    | /users   | Get all users with roles |
| POST   | /users   | Create new user |

### Sample Data (Auto-inserted on startup)
- **Roles:** ADMIN, USER
- **User:** admin (password: admin123) dengan role ADMIN dan USER

### HTTP Status Codes
- **200 OK** - GET request successful
- **201 CREATED** - POST request successful
- **500 Internal Server Error** - Ada error di server (cek console log)

---

## ✅ Testing Checklist

- [ ] Application berhasil running di port 8080
- [ ] GET /roles menampilkan ADMIN dan USER
- [ ] GET /users menampilkan user admin dengan 2 roles
- [ ] POST /roles berhasil create role baru
- [ ] POST /users berhasil create user baru
- [ ] POST /users dengan roles berhasil
- [ ] Data tersimpan di database SQL Server
- [ ] Junction table user_roles berisi relasi yang benar

---

## 🎯 Next Steps

Setelah testing berhasil, Anda bisa:
1. Tambahkan endpoint UPDATE dan DELETE
2. Tambahkan validation (@Valid, @NotNull, dll)
3. Tambahkan Spring Security untuk authentication
4. Tambahkan pagination untuk GET endpoints
5. Tambahkan exception handling yang lebih baik

Selamat mencoba! 🚀
