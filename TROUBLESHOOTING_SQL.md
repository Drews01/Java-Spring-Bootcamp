# Troubleshooting SQL Server Connection

## ❌ Error yang Terjadi
```
Caused by: com.microsoft.sqlserver.jdbc.SQLServerException
```

Aplikasi Spring Boot sudah jalan, tapi **tidak bisa connect ke SQL Server**.

---

## ✅ Solusi - Cek & Fix Koneksi SQL Server

### 1. Pastikan SQL Server Running

**Cek via Services:**
```powershell
# Buka PowerShell sebagai Administrator
Get-Service -Name "MSSQL*"
```

**Expected Output:**
```
Status   Name               DisplayName
------   ----               -----------
Running  MSSQLSERVER        SQL Server (MSSQLSERVER)
```

**Jika Stopped, start service:**
```powershell
Start-Service -Name "MSSQLSERVER"
```

---

### 2. Cek Database "LoanDatabase" Ada

**Buka SQL Server Management Studio (SSMS):**
1. Connect ke SQL Server (Windows Authentication)
2. Klik kanan "Databases" → New Database
3. Database name: **LoanDatabase**
4. Klik OK

**Atau via SQL Query:**
```sql
-- Cek apakah database ada
SELECT name FROM sys.databases WHERE name = 'LoanDatabase';

-- Jika tidak ada, create database
CREATE DATABASE LoanDatabase;
```

---

### 3. Cek SQL Server Instance Name

**Kemungkinan SQL Server Anda menggunakan instance name, bukan default.**

**Cek instance name:**
```powershell
# PowerShell
Get-ItemProperty -Path 'HKLM:\SOFTWARE\Microsoft\Microsoft SQL Server' -Name InstalledInstances
```

**Common instance names:**
- `MSSQLSERVER` (default instance)
- `SQLEXPRESS` (SQL Server Express)
- Custom instance name

---

### 4. Update Connection String

**Jika menggunakan SQL Server Express:**

Edit `application.yml`, ubah connection string:

**Dari:**
```yaml
url: jdbc:sqlserver://localhost:1433;databaseName=LoanDatabase;...
```

**Menjadi (untuk SQL Express):**
```yaml
url: jdbc:sqlserver://localhost\\SQLEXPRESS;databaseName=LoanDatabase;encrypt=true;trustServerCertificate=true;integratedSecurity=true;authenticationScheme=nativeAuthentication
```

**Atau jika menggunakan port lain:**
```yaml
url: jdbc:sqlserver://localhost:1434;databaseName=LoanDatabase;encrypt=true;trustServerCertificate=true;integratedSecurity=true;authenticationScheme=nativeAuthentication
```

---

### 5. Cek TCP/IP Enabled

**SQL Server Configuration Manager:**
1. Buka "SQL Server Configuration Manager"
2. SQL Server Network Configuration → Protocols for MSSQLSERVER (atau instance Anda)
3. Pastikan **TCP/IP** = **Enabled**
4. Klik kanan TCP/IP → Properties
5. Tab "IP Addresses" → Scroll ke **IPAll**
6. Cek **TCP Port** (biasanya 1433)
7. Jika ada perubahan, **restart SQL Server service**

---

### 6. Test Connection Manual

**Test koneksi via PowerShell:**
```powershell
# Install SqlServer module jika belum ada
Install-Module -Name SqlServer -AllowClobber -Force

# Test connection
Test-NetConnection -ComputerName localhost -Port 1433

# Atau untuk SQL Express
Test-NetConnection -ComputerName localhost -Port 1434
```

---

## 🔧 Fix Application.yml

Berdasarkan setup SQL Server Anda, pilih salah satu:

### Opsi 1: SQL Server Default Instance (Port 1433)
```yaml
spring:
  datasource:
    url: jdbc:sqlserver://localhost:1433;databaseName=LoanDatabase;encrypt=true;trustServerCertificate=true;integratedSecurity=true;authenticationScheme=nativeAuthentication
    driver-class-name: com.microsoft.sqlserver.jdbc.SQLServerDriver
```

### Opsi 2: SQL Server Express (Named Instance)
```yaml
spring:
  datasource:
    url: jdbc:sqlserver://localhost\\SQLEXPRESS;databaseName=LoanDatabase;encrypt=true;trustServerCertificate=true;integratedSecurity=true;authenticationScheme=nativeAuthentication
    driver-class-name: com.microsoft.sqlserver.jdbc.SQLServerDriver
```

### Opsi 3: SQL Server dengan Port Spesifik
```yaml
spring:
  datasource:
    url: jdbc:sqlserver://localhost:1434;databaseName=LoanDatabase;encrypt=true;trustServerCertificate=true;integratedSecurity=true;authenticationScheme=nativeAuthentication
    driver-class-name: com.microsoft.sqlserver.jdbc.SQLServerDriver
```

### Opsi 4: Jika Windows Auth Tidak Work, Gunakan SQL Auth
```yaml
spring:
  datasource:
    url: jdbc:sqlserver://localhost:1433;databaseName=LoanDatabase;encrypt=true;trustServerCertificate=true
    username: sa
    password: YourPassword123
    driver-class-name: com.microsoft.sqlserver.jdbc.SQLServerDriver
```

---

## 📋 Checklist Troubleshooting

- [ ] SQL Server service running
- [ ] Database "LoanDatabase" sudah dibuat
- [ ] TCP/IP protocol enabled
- [ ] Port 1433 (atau port yang benar) accessible
- [ ] Instance name benar (MSSQLSERVER atau SQLEXPRESS)
- [ ] Windows Authentication enabled (jika pakai integratedSecurity)
- [ ] Connection string di application.yml sudah benar

---

## 🎯 Quick Fix Steps

1. **Buka SSMS** → Connect ke SQL Server
2. **Create database** jika belum ada:
   ```sql
   CREATE DATABASE LoanDatabase;
   ```
3. **Cek instance name** Anda (MSSQLSERVER atau SQLEXPRESS?)
4. **Update application.yml** sesuai instance name
5. **Restart application** dari IDE

---

## 💡 Cara Cek Instance Name & Port

**Via SSMS:**
- Saat connect, lihat "Server name"
- Format: `localhost` atau `localhost\SQLEXPRESS` atau `localhost\INSTANCENAME`

**Via SQL Query (setelah connect):**
```sql
SELECT @@SERVERNAME AS ServerName;
SELECT @@VERSION AS SQLVersion;
```

---

## ✅ Setelah Fix

Setelah update `application.yml`, run lagi aplikasi dari IDE:
1. Stop application (Ctrl+C atau Stop button)
2. Run lagi `BootcampJavaSpringApplication`
3. Lihat console log, seharusnya muncul:
   ```
   HikariPool-1 - Starting...
   HikariPool-1 - Start completed.
   Hibernate: create table roles (...)
   ✓ Data initialization completed!
   Started BootcampJavaSpringApplication in X seconds
   ```

Lalu test: **http://localhost:8081/roles**

---

## 🆘 Masih Error?

Kirim info berikut:
1. SQL Server version Anda (Express/Standard/Developer?)
2. Instance name (dari SSMS saat connect)
3. Port yang digunakan
4. Full error message dari console

Saya akan bantu sesuaikan connection string! 🚀
