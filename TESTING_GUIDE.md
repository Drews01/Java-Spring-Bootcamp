# Testing Spring Boot Application - Step by Step

## ⚠️ Status: Application Belum Running

Error: `No connection could be made` → Application tidak running di port 8081

---

## 🚀 LANGKAH 1: Jalankan Aplikasi

### Via IntelliJ IDEA (Recommended):
1. Buka file: `src/main/java/com/example/demo/BootcampJavaSpringApplication.java`
2. Klik kanan → **Run 'BootcampJavaSpringApplication'**
3. Tunggu sampai muncul di console:
   ```
   Started BootcampJavaSpringApplication in X seconds
   Tomcat started on port 8081 (http)
   ```

### Via VS Code:
1. Buka file: `BootcampJavaSpringApplication.java`
2. Klik kanan → **Run Java**
3. Tunggu sampai aplikasi start

---

## ✅ LANGKAH 2: Verifikasi Application Running

Setelah aplikasi start, cek di console log harus muncul:

```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

HikariPool-1 - Starting...
HikariPool-1 - Start completed.

Hibernate: create table roles (...)
Hibernate: create table users (...)
Hibernate: create table user_roles (...)

✓ Data initialization completed!
✓ Roles created: ADMIN, USER
✓ Sample user created: admin (with ADMIN and USER roles)

Started BootcampJavaSpringApplication in 3.5 seconds (process running for 4.0)
Tomcat started on port 8081 (http) with context path '/'
```

---

## 🧪 LANGKAH 3: Testing Endpoints

### Test 1: GET All Roles (Browser)
Buka browser → `http://localhost:8081/roles`

**Expected Response:**
```json
[
  {"id": 1, "name": "ADMIN"},
  {"id": 2, "name": "USER"}
]
```

---

### Test 2: GET All Users (Browser)
Buka browser → `http://localhost:8081/users`

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

### Test 3: POST Create Role (PowerShell)
```powershell
Invoke-RestMethod -Method POST -Uri "http://localhost:8081/roles" `
  -ContentType "application/json" `
  -Body '{"name":"MODERATOR"}'
```

**Expected Response:**
```json
{"id": 3, "name": "MODERATOR"}
```

---

### Test 4: POST Create User (PowerShell)
```powershell
Invoke-RestMethod -Method POST -Uri "http://localhost:8081/users" `
  -ContentType "application/json" `
  -Body '{"username":"john","email":"john@example.com","password":"pass123","isActive":true}'
```

**Expected Response:**
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

### Test 5: POST Create User with Roles (PowerShell)
```powershell
$body = @{
    username = "jane"
    email = "jane@example.com"
    password = "pass456"
    isActive = $true
    roles = @(
        @{id = 1},
        @{id = 2}
    )
} | ConvertTo-Json

Invoke-RestMethod -Method POST -Uri "http://localhost:8081/users" `
  -ContentType "application/json" `
  -Body $body
```

**Expected Response:**
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

---

### Test 6: Verify in Database (SSMS)
```sql
USE LoanDatabase;

-- Check all tables created
SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_TYPE = 'BASE TABLE';
-- Expected: roles, users, user_roles

-- Check roles
SELECT * FROM roles;
-- Expected: ADMIN, USER, MODERATOR

-- Check users
SELECT * FROM users;
-- Expected: admin, john, jane

-- Check user-role relationships
SELECT 
    u.username,
    r.name as role_name
FROM users u
JOIN user_roles ur ON u.id = ur.user_id
JOIN roles r ON ur.role_id = r.id
ORDER BY u.username, r.name;
-- Expected:
-- admin - ADMIN
-- admin - USER
-- jane - ADMIN
-- jane - USER
```

---

## ✅ Testing Checklist

- [ ] Application running di port 8081
- [ ] GET /roles → Returns ADMIN, USER
- [ ] GET /users → Returns admin with 2 roles
- [ ] No infinite loop in JSON response
- [ ] POST /roles → Creates MODERATOR successfully
- [ ] POST /users → Creates john successfully
- [ ] POST /users with roles → Creates jane with ADMIN & USER roles
- [ ] All data saved in database LoanDatabase
- [ ] Junction table user_roles has correct relationships

---

## 📊 Expected Final Database State

**roles table:**
| id | name      |
|----|-----------|
| 1  | ADMIN     |
| 2  | USER      |
| 3  | MODERATOR |

**users table:**
| id | username | email              | password | is_active |
|----|----------|--------------------|----------|-----------|
| 1  | admin    | admin@example.com  | admin123 | 1         |
| 2  | john     | john@example.com   | pass123  | 1         |
| 3  | jane     | jane@example.com   | pass456  | 1         |

**user_roles table:**
| user_id | role_id |
|---------|---------|
| 1       | 1       |
| 1       | 2       |
| 3       | 1       |
| 3       | 2       |

---

## 🎯 Summary

1. ✅ **Start application** dari IDE
2. ✅ **Test GET** endpoints via browser
3. ✅ **Test POST** endpoints via PowerShell
4. ✅ **Verify data** di SQL Server
5. ✅ **Confirm** semua working!

Selamat! REST API Anda sudah lengkap dan berfungsi! 🚀
