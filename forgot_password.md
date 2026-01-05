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

### Security Configuration (IMPORTANT!)

The forgot password and reset password endpoints **must be publicly accessible** since users won't have a JWT token when they've forgotten their password.

Ensure your `SecurityConfig.java` includes these endpoints in `permitAll()`:

```java
.authorizeHttpRequests(
    auth ->
        auth.requestMatchers(
                "/auth/login",
                "/auth/register",
                "/auth/forgot-password",    // Must be public
                "/auth/reset-password",     // Must be public
                "/auth/logout")
            .permitAll()
            // ... rest of config
)
```

**If you get a 403 Forbidden error**, it means these endpoints are not in your permitAll() list!

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

If you don't want to use your real email, use [Mailtrap](https://mailtrap.io/). It captures all outgoing emails in a virtual inbox - perfect for testing!

#### Step-by-Step Mailtrap Setup:

1. **Create Free Account**:
   - Go to [https://mailtrap.io/](https://mailtrap.io/)
   - Sign up for a free account (no credit card required)

2. **Access Your Inbox**:
   - After login, go to **Email Testing** > **Inboxes**
   - Click on your default inbox (or create a new one)

3. **Get SMTP Credentials**:
   - In the inbox, click on **SMTP Settings**
   - Select **Integration**: Choose "Spring Boot" or "Other" from dropdown
   - You'll see your credentials displayed:
     - **Host**: `sandbox.smtp.mailtrap.io` (or similar)
     - **Port**: `2525` or `587`
     - **Username**: (unique username shown)
     - **Password**: (unique password shown)

4. **Configure application.yml**:
   ```yaml
   spring:
     mail:
       host: sandbox.smtp.mailtrap.io
       port: 2525
       username: your-mailtrap-username
       password: your-mailtrap-password
       properties:
         mail:
           smtp:
             auth: true
             starttls:
               enable: true
   ```
   
   **Important**: Replace `your-mailtrap-username` and `your-mailtrap-password` with the actual credentials from Mailtrap.

5. **Restart Your Application**:
   - Stop your Spring Boot app
   - Start it again to load the new mail configuration

## 4. Testing Strategy with Postman

### Option A: Testing with Mailtrap (Recommended for Development)

**Prerequisites**: Complete the Mailtrap setup steps above.

1.  **Start Your Application**:
    - Ensure your `application.yml` has Mailtrap credentials configured
    - Run your Spring Boot application

2.  **Request Password Reset** (in Postman):
    - **Method**: `POST`
    - **URL**: `http://localhost:8081/auth/forgot-password`
    - **Headers**: 
      - `Content-Type`: `application/json`
    - **Body** (raw JSON):
      ```json
      {
        "email": "admin@example.com"
      }
      ```
      *Note: Use any email from your database - Mailtrap will catch it regardless of the actual email address*
    - **Expected Response**: 
      ```json
      {
        "status": "success",
        "message": "Password reset token sent to email"
      }
      ```

3.  **Get Token from Mailtrap Inbox**:
    - Go to your Mailtrap inbox at [https://mailtrap.io/](https://mailtrap.io/)
    - Navigate to **Email Testing** > **Inboxes** > Your Inbox
    - You should see a new email that was just sent
    - Click on the email to open it
    - In the email body, you'll see a reset link like:
      ```
      http://localhost:8080/reset-password?token=a1b2c3d4-e5f6-7890-abcd-ef1234567890
      ```
    - Copy the token value (the UUID part after `token=`)
    
    **Tip**: You can also view the email in different formats:
    - Click **HTML** tab to see formatted email
    - Click **Text** tab to see plain text version
    - Click **Raw** tab to see the complete email source

4.  **Reset Password** (in Postman):
    - **Method**: `POST`
    - **URL**: `http://localhost:8081/auth/reset-password`
    - **Headers**: 
      - `Content-Type`: `application/json`
    - **Body** (raw JSON):
      ```json
      {
        "token": "<PASTE_TOKEN_FROM_MAILTRAP>",
        "newPassword": "NewStrongPassword123!"
      }
      ```
    - **Expected Response**: 
      ```json
      {
        "status": "success",
        "message": "Password reset successful"
      }
      ```

5.  **Verify Login with New Password**:
    - **Method**: `POST`
    - **URL**: `http://localhost:8081/auth/login`
    - **Headers**: 
      - `Content-Type`: `application/json`
    - **Body** (raw JSON):
      ```json
      {
        "email": "admin@example.com",
        "password": "NewStrongPassword123!"
      }
      ```
    - **Expected**: Login should **succeed** and return a JWT token
    
    Try logging in with the **old** password - it should **fail**.

6.  **Verify Session Invalidation**:
    
    **Step 1: Get Old Token**
    - Login to get a JWT token (before password reset):
      - **Method**: `POST`
      - **URL**: `http://localhost:8081/auth/login`
      - **Body**: 
        ```json
        {
          "email": "admin@example.com",
          "password": "OldPassword123!"
        }
        ```
      - Save the JWT token from the response
    
    **Step 2: Perform Password Reset**
    - Follow steps 2-4 above to reset the password
    
    **Step 3: Test Old Token**
    - Try to use the *old* JWT token to access a secured endpoint:
      - **Method**: `GET`
      - **URL**: `http://localhost:8081/users/me` (or any protected endpoint)
      - **Headers**: 
        - `Authorization`: `Bearer <OLD_JWT_TOKEN>`
    - **Expected Result**: You should receive a `403 Forbidden` or `401 Unauthorized` response, proving the old session is invalidated.

### Option B: End-to-End Testing (With Real Email)

1.  **Configure SMTP**: Set your real Gmail and App Password in `application.yml`.
2.  **Start App**: Run your Spring Boot application.

3.  **Request Password Reset**:
    - **Method**: `POST`
    - **URL**: `http://localhost:8081/auth/forgot-password`
    - **Headers**: 
      - `Content-Type`: `application/json`
    - **Body** (raw JSON):
      ```json
      {
        "email": "your-real-email@gmail.com"
      }
      ```
    - **Expected Response**: 
      ```json
      {
        "status": "success",
        "message": "Password reset token sent to email"
      }
      ```

4.  **Verify Inbox**: 
    - Check your email inbox
    - You should receive an email with a link like: `http://localhost:8080/reset-password?token=xxxx-xxxx-xxxx`
    - Copy the token value from the email link

5.  **Reset Password**:
    - **Method**: `POST`
    - **URL**: `http://localhost:8081/auth/reset-password`
    - **Headers**: 
      - `Content-Type`: `application/json`
    - **Body** (raw JSON):
      ```json
      {
        "token": "<PASTE_TOKEN_FROM_EMAIL>",
        "newPassword": "NewStrongPassword123!"
      }
      ```
    - **Expected Response**: 
      ```json
      {
        "status": "success",
        "message": "Password reset successful"
      }
      ```

### Option C: Local Testing (Without Email / Redis Verification)

If you cannot send emails or want to verify tokens directly from Redis:

1.  **Request Password Reset**:
    - **Method**: `POST`
    - **URL**: `http://localhost:8081/auth/forgot-password`
    - **Headers**: 
      - `Content-Type`: `application/json`
    - **Body** (raw JSON):
      ```json
      {
        "email": "admin@example.com"
      }
      ```
    - **Note**: Replace `admin@example.com` with a valid user email in your database.

2.  **Retrieve Token from Redis**:
    Since we are using Redis, you can query via `redis-cli` if you have it installed:
    ```bash
    # List all password reset tokens
    redis-cli KEYS "password_reset:token:*"
    
    # Get userId for a specific token
    redis-cli GET password_reset:token:<token_uuid>
    ```
    
    *Alternatively*, for local testing without email, you can temporarily modify `AuthService.java` to print the token to the console logs.

3.  **Reset Password**:
    - **Method**: `POST`
    - **URL**: `http://localhost:8081/auth/reset-password`
    - **Headers**: 
      - `Content-Type`: `application/json`
    - **Body** (raw JSON):
      ```json
      {
        "token": "<PASTE_TOKEN_HERE>",
        "newPassword": "NewStrongPassword123!"
      }
      ```

4.  **Verify Login with New Password**:
    - **Method**: `POST`
    - **URL**: `http://localhost:8081/auth/login`
    - **Headers**: 
      - `Content-Type`: `application/json`
    - **Body** (raw JSON):
      ```json
      {
        "email": "admin@example.com",
        "password": "NewStrongPassword123!"
      }
      ```
    - **Expected**: Login should **succeed** and return a JWT token
    
    Try logging in with the **old** password - it should **fail**.

5.  **Verify Session Invalidation**:
    
    **Step 1: Get Old Token**
    - Login to get a JWT token (before password reset):
      - **Method**: `POST`
      - **URL**: `http://localhost:8081/auth/login`
      - **Body**: 
        ```json
        {
          "email": "admin@example.com",
          "password": "OldPassword123!"
        }
        ```
      - Save the JWT token from the response
    
    **Step 2: Perform Password Reset**
    - Follow steps 1-3 above to reset the password
    
    **Step 3: Test Old Token**
    - Try to use the *old* JWT token to access a secured endpoint:
      - **Method**: `GET`
      - **URL**: `http://localhost:8081/users/me` (or any protected endpoint)
      - **Headers**: 
        - `Authorization`: `Bearer <OLD_JWT_TOKEN>`
    - **Expected Result**: You should receive a `403 Forbidden` or `401 Unauthorized` response, proving the old session is invalidated.

### Postman Collection Tips

1. **Environment Variables**: Create a Postman environment with:
   - `base_url`: `http://localhost:8081`
   - `reset_token`: (save token here after forgot-password request)
   - `access_token`: (save JWT token for authorization tests)

2. **Tests Script** (Add to forgot-password request):
   ```javascript
   // Save token from console logs or email for next request
   if (pm.response.code === 200) {
       console.log("Check your email for reset token");
   }
   ```

3. **Tests Script** (Add to reset-password request):
   ```javascript
   pm.test("Password reset successful", function () {
       pm.response.to.have.status(200);
       var jsonData = pm.response.json();
       pm.expect(jsonData.status).to.eql("success");
   });
   ```
