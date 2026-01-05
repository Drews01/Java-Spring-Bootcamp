# Production Migration Guide: Windows to Linux/Cloud

This guide details the steps to migrate the "Loan Spring Bootcamp" application from your local Windows environment to a Linux-based cloud server, including moving the SQL Server into a Docker container.

## 1. Prerequisites (Linux Host)

Before you begin, ensure your Linux server (Ubuntu 22.04+ recommended) has the following installed:
*   **Docker Desktop for Linux** or **Docker Engine**
*   **Docker Compose V2** (Verify with `docker compose version`)
    > [!TIP]
    > In modern Docker, the command is `docker compose` (with a space). The old command `docker-compose` (with a hyphen) may not be installed.
*   **Git**

## 2. Infrastructure Setup (All-in-Docker)

We will use a dedicated folder for production deployment to isolate it from development files.

### Step A: Folder Structure
Create a new directory on your Linux server:
```bash
mkdir -p ~/deployment/loan-app
cd ~/deployment/loan-app
```

### Step B: SQL Server & Nginx in Docker
In production, your SQL Server and Nginx will run in containers.
*   **SQL Server**: Using `mcr.microsoft.com/mssql/server:2022-latest`.
*   **Nginx**: Using `nginx:alpine` as a reverse proxy.
*   **Persistence**: Use Docker volumes for SQL data and mount `nginx.conf` for the proxy settings.

## 3. Configuration Management

**IMPORTANT**: Since we are not modifying `application.yml` directly, you must use **Environment Variables** to override the default settings at runtime. Docker Compose handles this automatically.

### Required Environment Variables
| Variable Name | Purpose | Example Value |
| :--- | :--- | :--- |
| `SPRING_DATASOURCE_URL` | Cloud DB connection | `jdbc:sqlserver://sql-server:1433;databaseName=LoanDatabase;encrypt=true;trustServerCertificate=true` |
| `SPRING_DATASOURCE_USERNAME` | DB Admin user | `sa` |
| `SPRING_DATASOURCE_PASSWORD` | Strong DB password | `Prod_P@ssw0rd_2024!` |
| `SPRING_DATA_REDIS_HOST` | Redis container name | `redis-server` |
| `APP_SECURITY_JWT_SECRET` | Production-only secret | *(Generate a 64-character hex string)* |
| `MAIL_USERNAME` | Email server username | *(Your Mailtrap/SMTP User)* |
| `MAIL_PASSWORD` | Email server password | *(Your Mailtrap/SMTP Password)* |

## 4. Migration Execution Steps

1.  **Code Transfer**: Clone or copy your code to the Linux server.
2.  **Build Artifact**: On the Linux server (or via CI/CD), build the JAR:
    ```bash
    ./mvnw clean package -DskipTests
    ```
3.  **Deployment Directory**: Move the generated JAR (e.g., `demo-0.0.1-SNAPSHOT.jar`) into the `deployment/` folder.
4.  **Launch**: Run `docker compose up -d` from the deployment folder.

## 5. Database Initialization
When SQL Server starts for the first time, it will be empty.
1.  Connect to the container: `docker exec -it sql-server /opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P YourPassword`
2.  Create the database: `CREATE DATABASE LoanDatabase; GO;`
3.  Spring Boot's `ddl-auto: update` will handle table creation on first start.

## 6. How to Access SQL Server

### Option A: From the Terminal (Inside Linux)
Run this command to enter the SQL command line directly:
```bash
docker exec -it sql-server /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P Prod_P@ssw0rd_2024! -C
```

### Option B: From your PC (GUI Tool)
If you use DBeaver, Azure Data Studio, or SSMS:
*   **Host**: Your Linux Cloud IP address
*   **Port**: `1433`
*   **Username**: `sa`
*   **Password**: `Prod_P@ssw0rd_2024!` (or whatever you set in your .env/compose file)
*   **Database**: `LoanDatabase`

> [!NOTE]
> Ensure your cloud firewall (Security Group) allows inbound traffic on port **1433** if you want to connect from your local PC.

## 7. Verifying the Deployment

### Step A: Check Container Status
Run this on your Linux terminal:
```bash
docker compose ps
```
All containers (`loan-app`, `sql-server`, `redis-server`, `nginx-proxy`) should show a status of **Up**.

### Step B: Check Application Logs
Verify that the Spring Boot app started successfully:
```bash
docker compose logs -f loan-app
```
Look for the message: `Started DemoApplication in ... seconds`.

### Step C: Test the API Endpoint
Use `curl` to test the connection through Nginx:
```bash
# Test from the Linux terminal
curl -I http://localhost
```
You should see a `200 OK` or a redirect. You can also test a specific endpoint:
```bash
curl http://localhost/api/auth/test (replace with a real public endpoint)
```

### Step D: External Access
Open your browser or Postman on your local PC and visit:
`http://<YOUR_SERVER_IP>/`
If you see a response from Nginx or your app, the migration is complete!
