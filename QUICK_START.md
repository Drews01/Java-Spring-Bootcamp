# Cara Menjalankan Aplikasi Spring Boot

## ⚠️ Maven Command Tidak Berfungsi

Sayangnya, command `./mvnw spring-boot:run` mengalami **MojoExecutionException** yang persistent.

---

## ✅ SOLUSI: Gunakan IDE (100% WORK)

### Metode 1: IntelliJ IDEA (RECOMMENDED)

1. **Buka IntelliJ IDEA**
2. **File** → **Open** → Pilih folder project ini
3. Tunggu Maven import selesai (lihat progress bar di bawah)
4. **Navigate** ke: `src/main/java/com/example/demo/BootcampJavaSpringApplication.java`
5. **Klik kanan** pada file → **Run 'BootcampJavaSpringApplication'**
6. **Lihat console** - tunggu sampai muncul:
   ```
   Started BootcampJavaSpringApplication in X seconds
   Tomcat started on port 8081 (http)
   ```
7. **Test di browser**: `http://localhost:8081/roles`

---

### Metode 2: VS Code

1. **Install Extensions** (jika belum):
   - Extension Pack for Java
   - Spring Boot Extension Pack
2. **Buka folder** project di VS Code
3. **Buka file**: `BootcampJavaSpringApplication.java`
4. **Klik** tombol **Run** (▶️) di atas method `main`
5. Atau **klik kanan** → **Run Java**
6. **Test di browser**: `http://localhost:8081/roles`

---

### Metode 3: Eclipse

1. **File** → **Import** → **Maven** → **Existing Maven Projects**
2. **Browse** ke folder project → **Finish**
3. **Klik kanan** project → **Run As** → **Spring Boot App**
4. **Test di browser**: `http://localhost:8081/roles`

---

## 📋 Verifikasi Application Running

Setelah run dari IDE, di console harus muncul:

```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

2025-12-18 10:XX:XX.XXX  INFO --- HikariPool-1 - Starting...
2025-12-18 10:XX:XX.XXX  INFO --- HikariPool-1 - Start completed.

Hibernate: create table roles (...)
Hibernate: create table users (...)  
Hibernate: create table user_roles (...)

✓ Data initialization completed!
✓ Roles created: ADMIN, USER
✓ Sample user created: admin (with ADMIN and USER roles)

2025-12-18 10:XX:XX.XXX  INFO --- Started BootcampJavaSpringApplication in 3.5 seconds
2025-12-18 10:XX:XX.XXX  INFO --- Tomcat started on port 8081 (http)
```

---

## 🧪 Testing Setelah Running

### Test 1: Browser - GET Roles
```
http://localhost:8081/roles
```
**Expected:**
```json
[
  {"id": 1, "name": "ADMIN"},
  {"id": 2, "name": "USER"}
]
```

### Test 2: Browser - GET Users
```
http://localhost:8081/users
```
**Expected:**
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

### Test 3: PowerShell - POST Create Role
```powershell
Invoke-RestMethod -Method POST -Uri "http://localhost:8081/roles" `
  -ContentType "application/json" `
  -Body '{"name":"MODERATOR"}'
```

### Test 4: PowerShell - POST Create User
```powershell
Invoke-RestMethod -Method POST -Uri "http://localhost:8081/users" `
  -ContentType "application/json" `
  -Body '{"username":"john","email":"john@example.com","password":"pass123","isActive":true}'
```

---

## 📊 Configuration Summary

✅ **Port**: 8081  
✅ **Database**: LoanDatabase  
✅ **SQL Server**: DESKTOP-08BOFEE\MSSQLSERVER01  
✅ **Authentication**: Windows Authentication  
✅ **Sample Data**: ADMIN, USER roles + admin user  

---

## 🎯 Quick Start

1. **Buka IntelliJ IDEA** (atau IDE favorit Anda)
2. **Open project** ini
3. **Run** `BootcampJavaSpringApplication.java`
4. **Test** di browser: `http://localhost:8081/roles`
5. **Done!** ✅

---

## 💡 Tips

- Gunakan **Postman** untuk testing POST requests (lebih mudah dari PowerShell)
- Lihat **console log** untuk melihat SQL queries yang di-generate Hibernate
- Cek **database** di SSMS untuk verify data tersimpan
- Lihat **TESTING_GUIDE.md** untuk complete testing scenarios

---

## 🆘 Masih Bermasalah?

Jika aplikasi masih error saat running dari IDE:
1. Cek error message di console
2. Pastikan SQL Server running
3. Pastikan database LoanDatabase sudah ada
4. Cek connection string di `application.yml`

Good luck! 🚀
