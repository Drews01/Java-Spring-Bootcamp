# Android CSRF & Cookie Implementation Guide

## Understanding When CSRF is Required

### The Rule
Spring Security uses CSRF protection for all **state-changing** HTTP methods:
- **Requires CSRF**: `POST`, `PUT`, `DELETE`, `PATCH`
- **Does NOT require CSRF**: `GET`, `HEAD`, `OPTIONS`

### Exceptions (CSRF Ignored)
The following endpoints have CSRF protection **disabled** in `SecurityConfig.java`:
| Endpoint | Method | CSRF Required | Auth Required |
|----------|--------|---------------|---------------|
| `/auth/login` | POST | ❌ No | ❌ No |
| `/auth/register` | POST | ❌ No | ❌ No |
| `/auth/forgot-password` | POST | ❌ No | ❌ No |
| `/auth/reset-password` | POST | ❌ No | ❌ No |
| `/api/csrf-token` | GET | ❌ No | ❌ No |
| `/auth/logout` | POST | ✅ **Yes** | ✅ Yes |

---

## Complete Endpoint Reference

### Endpoints that REQUIRE `X-XSRF-TOKEN` Header
All POST/PUT/DELETE/PATCH endpoints (except the ignored ones above) require the CSRF token.

| Controller | Endpoint | Method | CSRF |
|------------|----------|--------|------|
| **LoanWorkflow** | `/api/loan-workflow/submit` | POST | ✅ |
| **LoanWorkflow** | `/api/loan-workflow/action` | POST | ✅ |
| **UserProfile** | `/api/user-profiles` | POST | ✅ |
| **UserProfile** | `/api/user-profiles/upload-ktp` | POST | ✅ |
| **UserProfile** | `/api/user-profiles` | PUT | ✅ |
| **UserProfile** | `/api/user-profiles` | DELETE | ✅ |
| **UserProduct** | `/api/user-products` | POST | ✅ |
| **UserProduct** | `/api/user-products/{id}` | PUT | ✅ |
| **UserProduct** | `/api/user-products/{id}` | DELETE | ✅ |
| **LoanApplication** | `/api/loan-applications` | POST | ✅ |
| **LoanApplication** | `/api/loan-applications/{id}` | PUT | ✅ |
| **LoanApplication** | `/api/loan-applications/{id}` | DELETE | ✅ |
| **Notification** | `/api/notifications` | POST | ✅ |
| **Notification** | `/api/notifications/{id}/read` | PATCH | ✅ |
| **Notification** | `/api/notifications/{id}` | DELETE | ✅ |
| **Auth** | `/auth/logout` | POST | ✅ |
| **Auth** | `/auth/refresh` | POST | ✅ |
| **Branch** | `/api/branches` | POST | ✅ |
| **Branch** | `/api/branches/{id}` | PUT/DELETE | ✅ |
| **User** | `/api/users` | POST/PUT/DELETE | ✅ |
| **Role** | `/api/roles` | POST/DELETE | ✅ |
| **Menu** | `/api/menus` | POST/PUT/DELETE | ✅ |
| **Product** | `/api/products` | POST/PATCH/DELETE | ✅ |

### Endpoints that DO NOT require CSRF (GET only)
These only need the `Authorization: Bearer <token>` header.

| Controller | Endpoint | Method |
|------------|----------|--------|
| **LoanWorkflow** | `/api/loan-workflow/queue/*` | GET |
| **LoanWorkflow** | `/api/loan-workflow/history/*` | GET |
| **LoanWorkflow** | `/api/loan-workflow/{id}/allowed-actions` | GET |
| **UserProfile** | `/api/user-profiles/me` | GET |
| **UserProfile** | `/api/user-profiles/{userId}` | GET |
| **UserProduct** | `/api/user-products/my-tier` | GET |
| **UserProduct** | `/api/user-products/*` | GET |
| **LoanApplication** | `/api/loan-applications/my-history` | GET |
| **LoanApplication** | `/api/loan-applications/{id}` | GET |
| **Branch** | `/api/branches/dropdown` | GET |
| **Notification** | `/api/notifications/*` | GET |
| **Auth** | `/auth/me` | GET |
| All others | Any `GET` request | GET |

---

## Why Some Endpoints Work Without CSRF

1. **GET requests** - Spring Security automatically skips CSRF validation for safe methods (`GET`, `HEAD`, `OPTIONS`)
2. **Auth endpoints** - Explicitly excluded in `SecurityConfig.java` via `.ignoringRequestMatchers()`
3. **Public endpoints** - Like `/api/products/**` don't require auth at all

---

## The Problem You Faced
You hit `POST /api/loan-workflow/submit` which:
- Is a **POST** method → Requires CSRF ✅
- Is **NOT** in the ignored list → CSRF enforced ✅

So you **MUST** include both:
1. `Authorization: Bearer <jwt>` 
2. `X-XSRF-TOKEN: <masked_token_from_json_body>`
3. Cookie: `XSRF-TOKEN=<raw_token>` (auto-sent if CookieJar is configured)

---

## Implementation Guide for Android

### Step 1: Configure CookieJar (CRITICAL)
```kotlin
val cookieJar = PersistentCookieJar(...)  // or JavaNetCookieJar

val okHttpClient = OkHttpClient.Builder()
    .cookieJar(cookieJar)
    .build()
```

### Step 2: Fetch and Store CSRF Token
```kotlin
// Call this AFTER login and BEFORE any POST/PUT/DELETE
suspend fun fetchCsrfToken(): String {
    val response = api.getCsrfToken()  // GET /api/csrf-token
    return response.token  // Store this masked token!
}
```

### Step 3: Create CSRF Interceptor
```kotlin
class CsrfInterceptor(private val tokenManager: TokenManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        
        // Only add CSRF for state-changing methods
        val method = request.method
        if (method in listOf("POST", "PUT", "DELETE", "PATCH")) {
            val csrfToken = tokenManager.getCsrfToken()
            if (csrfToken != null) {
                val newRequest = request.newBuilder()
                    .header("X-XSRF-TOKEN", csrfToken)
                    .build()
                return chain.proceed(newRequest)
            }
        }
        return chain.proceed(request)
    }
}
```

### Step 4: Add Interceptor to OkHttp
```kotlin
val okHttpClient = OkHttpClient.Builder()
    .cookieJar(cookieJar)
    .addInterceptor(CsrfInterceptor(tokenManager))
    .addInterceptor(AuthInterceptor(tokenManager))  // For Bearer token
    .build()
```

---

## Quick Debugging Checklist

If you get `403 Forbidden: Invalid or missing CSRF Token`:

- [ ] Is the request method POST/PUT/DELETE/PATCH? → CSRF required
- [ ] Is CookieJar configured? → Must persist `XSRF-TOKEN` cookie
- [ ] Did you call `GET /api/csrf-token` first?
- [ ] Are you using the **JSON body token** (not cookie value) in the header?
- [ ] Is the header name exactly `X-XSRF-TOKEN`?
- [ ] Print request headers to verify both Cookie and X-XSRF-TOKEN are present
