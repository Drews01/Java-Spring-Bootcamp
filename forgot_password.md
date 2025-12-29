# Forgot Password Feature Documentation

This document describes the implementation, architecture, and testing of the Forgot Password feature.

## 1. How It Was Built

The feature is built using a secure token-based approach with session invalidation.

### Architectural Flow (Redis Implementation)
1.  **User Request**: User submits email to `/auth/forgot-password`.
2.  **Token Generation**: System validates email, generates a unique UUID token.
3.  **Redis Storage**: Token is stored in **Redis** with a TTL (1 hour).
    *   Key: `password_reset:token:<UUID>`
    *   Value: `userId`
4.  **Email Dispatch**: System sends an email containing a link with the token.
5.  **Password Reset**: User submits the token and new password to `/auth/reset-password`.
6.  **Validation & Update**:
    *   System checks Redis for the token. If found, retrieves `userId`.
    *   System updates the user's password (BCrypt hashed).
    *   **CRITICAL**: System updates `User.lastPasswordResetDate` to the current timestamp.
    *   System deletes the token from Redis.
7.  **Session Invalidation**:
    *   Any JWT token presented to the API has an `issuedAt` timestamp.
    *   `JwtService` compares this `issuedAt` with `User.lastPasswordResetDate`.
    *   If `issuedAt` < `lastPasswordResetDate`, the token is rejected.

### Key Components

#### Domain Entities
*   **User**: Added `lastPasswordResetDate` field to track when the password was last changed.
*   **NOTE**: The `PasswordReset` entity and repository are **deprecated** and should be deleted manually.

#### Services
*   **AuthService**: Orchestrates the flow. Handles `forgotPassword` and `resetPassword` logic.
*   **PasswordResetService**: Now uses `StringRedisTemplate` to manage tokens in Redis.
*   **EmailService**: Wrapper around `JavaMailSender` to send simple text emails.
*   **JwtService**: Enhanced `isTokenValid` method to enforce session invalidation policy.

## 2. Manual Cleanup Required

Since we moved to Redis, the following SQL-based files are no longer needed. Please **delete** them manually:

1.  `src/main/java/com/example/demo/entity/PasswordReset.java`
2.  `src/main/java/com/example/demo/repository/PasswordResetRepository.java`

## 3. Configuration

### SMTP Settings (Gmail Example)
To use Gmail, you need an **App Password** (not your regular login password). 
1. Go to your Google Account > Security.
2. Enable 2-Step Verification.
3. Search for "App Passwords".
4. Create a new one for "Mail" and "Other (Spring Boot)".
5. Use that 16-character code as your password in the config below.

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your-email@gmail.com
    password: your-app-password-here
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
```

### Alternative: Local Development Testing (Mailtrap)
If you don't want to use your real email, use [Mailtrap](https://mailtrap.io/). It captures all outgoing emails in a virtual inbox.
1. Create a free account.
2. In "Inboxes" > "SMTP Settings", select "Spring Boot".
3. Copy the host, port, username, and password into your `application.yml`.

## 4. Testing Strategy

### Option A: End-to-End Testing (With Real Email)
1.  **Configure SMTP**: Set your real Gmail and App Password in `application.yml`.
2.  **Start App**: Run your Spring Boot application.
3.  **Request Reset**:
    ```bash
    curl -X POST http://localhost:8081/auth/forgot-password \
    -H "Content-Type: application/json" \
    -d "{\"email\":\"your-real-email@gmail.com\"}"
    ```
4.  **Verify Inbox**: You should receive an email with a link like:
    `http://localhost:8080/reset-password?token=xxxx-xxxx-xxxx`
5.  **Extract Token**: Copy the token value from the email link.
6.  **Reset**: Call the `/auth/reset-password` endpoint using that token.

### Option B: Local Testing (Without Email / Redis Verification)
If you cannot send real emails or just want to test the logic:

1.  **Request Reset**:
    ```bash
    curl -X POST http://localhost:8081/auth/forgot-password \
    -H "Content-Type: application/json" \
    -d "{\"email\":\"admin@example.com\"}"
    ```
    *Note: Replace `admin@example.com` with a valid user email in your database.*

3.  **Retrieve Token from Redis**:
    Since we are using Redis, you can query via `redis-cli` if you have it installed:
    ```bash
    redis-cli GET password_reset:token:<token_uuid>
    # Note: Since the token is the key, you might need to scan or keys * (dev only) to find it if you don't know the UUID.
    # keys "password_reset:token:*"
    ```
    
    *Alternatively*, for local testing without email, you can temporarily modify `AuthService.java` to print the token to the console logs.

4.  **Reset Password**:
    ```bash
    curl -X POST http://localhost:8081/auth/reset-password \
    -H "Content-Type: application/json" \
    -d "{\"token\":\"<PASTE_TOKEN_HERE>\", \"newPassword\":\"NewStrongPassword123!\"}"
    ```

4.  **Verify Login**:
    Try to login with the **new** password. It should succeed.
    Try to login with the **old** password. It should fail.

5.  **Verify Session Invalidation**:
    *   **Before Reset**: Login and get a JWT token.
    *   **Perform Reset**: Follow steps 1-3.
    *   **After Reset**: Try to use the *old* JWT token to access a secured endpoint (e.g., `/users/me`).
    *   **Result**: You should receive a `403 Forbidden` or `401 Unauthorized` response, proving the old session is dead.
