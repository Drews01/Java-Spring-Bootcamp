# Google Login Implementation Documentation (Backend)

This document outlines the implementation of "Login with Google" using the **Native Flow** (best practice for mobile integration).

## Overview
The backend provides an endpoint `POST /auth/google` that accepts a simplistic Google ID Token. It verifies the token using Google's libraries, creates or retrieves the user from the database, and issues a standard JWT access token (same as email/password login).

## Prerequisites
1.  **Google Cloud Project**: You must have a project in Google Cloud Console.
2.  **Client ID**: You need an OAuth 2.0 Web Client ID (or Android Client ID, checking audience matching).
3.  **Configuration**: Set `app.google.client-id` in `application.yml`.

## Configuration
**File**: `src/main/resources/application.yml`
```yaml
app:
  google:
    client-id: YOUR_GOOGLE_CLIENT_ID
```
Replace `YOUR_GOOGLE_CLIENT_ID` with your actual Client ID.

## Database Changes
1.  **Users Table**:
    -   `password`: Now **nullable**.
    -   `auth_provider`: Added to distinguish `LOCAL` vs `GOOGLE` users.
2.  **Entities**:
    -   `User.java` updated to reflect these changes.
    -   `AuthProvider.java` enum added.

## API Endpoint

### `POST /auth/google`
Exchanges a Google ID Token for an App JWT.

**Request Body** (`application/json`):
```json
{
  "idToken": "eyJhbGciOiJSU..."
}
```

**Response** (`200 OK`):
```json
{
  "success": true,
  "message": "Google Login successful",
  "data": {
    "token": "djs... (JWT Access Token)",
    "refreshToken": "...",
    "type": "Bearer",
    "roles": ["USER"],
    ...
  }
}
```
*Note: A `Set-Cookie` header is also sent with the `accessToken` (HttpOnly).*

## Android Integration Guide (Detailed)
This guide explains how to implement the client-side logic in your Android app.

### 1. Dependencies
Add the Credential Manager dependencies to your `app/build.gradle.kts`:
```kotlin
dependencies {
    implementation("androidx.credentials:credentials:1.5.0-alpha02") // or latest
    implementation("androidx.credentials:credentials-play-services-auth:1.5.0-alpha02")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.0")
}
```

### 2. Configure Google Cloud Console
1.  Create an **Android Client ID** in Google Cloud Console (needs your package name and SHA-1).
2.  Use the **Web Client ID** (the one in your backend `application.yml`) as the `serverClientId` in your code.

### 3. Implement Login Logic (Kotlin)
Use the `CredentialManager` API to get the ID Token.

```kotlin
// 1. Setup Request
val googleIdOption = GetGoogleIdOption.Builder()
    .setFilterByAuthorizedAccounts(false)
    .setServerClientId("YOUR_WEB_CLIENT_ID_FROM_BACKEND_CONFIG") // <--- IMPORTANT
    .setAutoSelectEnabled(false) // or true for auto-signin
    .build()

val request = GetCredentialRequest.Builder()
    .addCredentialOption(googleIdOption)
    .build()

// 2. Launch Credential Manager
try {
    val result = credentialManager.getCredential(context, request)
    val credential = result.credential

    if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
        val idToken = googleIdTokenCredential.idToken
        
        // 3. Send ID Token to Backend
        sendTokenToBackend(idToken)
    }
} catch (e: GetCredentialException) {
    // Handle error (e.g. user cancelled)
}
```

### 4. Call Backend API
Make a POST request to your backend:
-   **URL**: `YOUR_BACKEND_URL/auth/google` (e.g., `http://10.0.2.2:8081/auth/google` for emulator).
-   **Method**: `POST`
-   **Body**:
    ```json
    {
      "idToken": "THE_ID_TOKEN_YOU_GOT_FROM_STEP_3"
    }
    ```
-   **Response**: Save the `token` (JWT) from the response just like a normal login.

## Troubleshooting
-   **Invalid ID Token**: Ensure the `client-id` in `application.yml` matches the one used in the Android app.
-   **Email mismatch**: The system links accounts by email. If the Google email matches an existing local user, they will be logged in.
