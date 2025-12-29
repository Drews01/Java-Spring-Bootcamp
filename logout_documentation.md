# Secure Logout Documentation

This document outlines the implementation and testing procedures for the secure logout feature using Redis-based token blacklisting.

## Architecture

The logout mechanism is stateless from the perspective of the server's session management but uses Redis to maintain a state of "invalidated" tokens until their natural expiration.

1.  **Client** sends a `POST /auth/logout` request with the JWT in the `Authorization` header.
2.  **Server** (`JwtAuthFilter`) validates the token signature and checks if it exists in the **Redis Blacklist**.
3.  **Server** (`AuthService`) extracts the expiration time from the token and adds it to Redis with a TTL (Time-To-Live) equal to the remaining time until the token expires.
4.  **Client** is responsible for clearing the token from local storage and redirecting the user to the login page.

## Endpoint Details

### Logout User

*   **URL**: `/auth/logout`
*   **Method**: `POST`
*   **Headers**:
    *   `Authorization`: `Bearer <jwt_token>`

#### Success Response

*   **Code**: `200 OK`
*   **Content**:
    ```json
    {
      "status": "success",
      "message": "Logout successful",
      "data": null
    }
    ```

#### Error Response (Already Logout / Invalid Token)

*   **Code**: `401 Unauthorized` (if token is blacklisted or invalid)

## Client-Side Redirection

> [!NOTE]
> The backend does not perform an HTTP redirect (302). It returns a `200 OK`. The client application (Frontend/Mobile) must handle the redirection to the Login page upon receiving this success response.

## Redis Configuration

Ensure Redis is running and configured in `application.yml`:

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
```

## Postman Testing Guide

Follow these steps to manually verify the secure logout flow using Postman.

### Prerequisites
- Application is running on `http://localhost:8081`.
- Redis is running.

### Step 1: Login
1.  Create a **POST** request to `http://localhost:8081/auth/login`.
2.  **Body** (JSON):
    ```json
    {
      "username": "testuser",
      "password": "password"
    }
    ```
    *(Note: Use a valid user. If needed, Register first via `/auth/register`)*
3.  **Send**.
4.  Copy the `token` from the response `data`.

### Step 2: Access Protected Resource (Verify Login)
1.  Create a **GET** or **POST** request to any protected endpoint (e.g., `/auth/logout` itself is protected).
2.  **Headers**:
    *   Key: `Authorization`
    *   Value: `Bearer <paste_token_here>`
3.  **Send**.
4.  Verify response is **200 OK**.

### Step 3: Logout
1.  Create a **POST** request to `http://localhost:8081/auth/logout`.
2.  **Headers**:
    *   Key: `Authorization`
    *   Value: `Bearer <paste_token_here>`
3.  **Send**.
4.  Verify response is **200 OK** with message "Logout successful".

### Step 4: Verify Blacklist (Access Resource Again)
1.  Repeat **Step 2** (Access Protected Resource) using the **SAME** token.
2.  **Send**.
3.  **Expected Result**: `401 Unauthorized` (or `403 Forbidden`).
    *   This confirms the token has been successfully blacklisted in Redis and is no longer valid.
