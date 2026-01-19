# Refresh Token - Simple Explanation

## 🤔 What is a Refresh Token?

Think of it like this:
- **Access Token** = Your office badge (expires in 1 hour)
- **Refresh Token** = Your permanent ID card (expires in 7 days)

When your office badge expires, you don't need to go through security again. You just show your permanent ID card and get a new badge!

---

## 🎯 Why Do We Need Refresh Tokens?

### The Problem
- Access tokens expire quickly (1 hour) for security
- Users don't want to login every hour
- Solution: Use refresh token to get new access token without logging in again!

### Real-World Example
```
1. You login → Get 2 tokens:
   - Access Token (1 hour)
   - Refresh Token (7 days)

2. After 1 hour, access token expires
   - ❌ Without refresh token: User must login again
   - ✅ With refresh token: Automatically get new access token

3. After 7 days, refresh token expires
   - User must login again (for security)
```

---

## 🔧 How It Works in Your Project

### 1. **Login/Register** - You Get BOTH Tokens
```http
POST /auth/login
{
  "usernameOrEmail": "admin",
  "password": "admin123"
}

Response:
{
  "status": "success",
  "data": {
    "token": "eyJhbGc...",           ← Access Token (1 hour)
    "refreshToken": "abc-123-xyz",  ← Refresh Token (7 days)
    "tokenType": "Bearer",
    "accessTokenExpiry": "2026-01-05T17:30:00Z",
    "refreshTokenExpiry": "2026-01-12T16:30:00Z"
  }
}
```

### 2. **Access Token Expires** - Use Refresh Token
```http
POST /auth/refresh
{
  "refreshToken": "abc-123-xyz"
}

Response:
{
  "status": "success",
  "data": {
    "token": "eyJnew...",           ← NEW Access Token
    "refreshToken": "xyz-789-new",  ← NEW Refresh Token (rotated)
    ...
  }
}
```

### 3. **Behind the Scenes**
```
Client sends refresh token
    ↓
Check Redis: Is token valid?
    ↓
Check User: Is user still active?
    ↓
Generate NEW access token
    ↓
Rotate refresh token (delete old, create new)
    ↓
Return BOTH new tokens
```

---

## 💾 Where Are Refresh Tokens Stored?

### In Redis (Server-Side)
```
Key: refresh_token:abc-123-xyz
Value: 1 (userId)
TTL: 7 days

Key: user_refresh_tokens:1
Value: [abc-123-xyz, def-456-xyz, ...]
TTL: 7 days
```

### Why Redis?
- Fast lookup
- Automatic expiration (TTL)
- Easy to revoke (delete from Redis)
- Can track all user's tokens

---

## 📝 Key Functions

### 1. `RefreshTokenService.createRefreshToken(userId, ttl)`
**What it does:** Creates a new refresh token
```java
// Generates UUID: "f47ac10b-58cc-4372-a567-0e02b2c3d479"
// Stores in Redis with userId
// Tracks token in user's token list
```

### 2. `RefreshTokenService.validateRefreshToken(token)`
**What it does:** Checks if token is valid
```java
// Looks up token in Redis
// Returns userId if found, empty if expired/invalid
```

### 3. `RefreshTokenService.revokeRefreshToken(token)`
**What it does:** Deletes a single token
```java
// Used during token rotation
// Removes from Redis
```

### 4. `RefreshTokenService.revokeAllUserTokens(userId)`
**What it does:** Deletes ALL user's tokens
```java
// Used when password is reset
// Logs user out from all devices
```

### 5. `AuthService.refreshAccessToken(refreshToken)`
**What it does:** Main refresh logic
```java
1. Validate refresh token in Redis
2. Get user from database
3. Generate new access token
4. Rotate refresh token (delete old, create new)
5. Return both new tokens
```

---

## 🧪 How to Test

### Scenario 1: Normal Refresh Flow

#### Step 1: Login
```bash
curl -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "admin",
    "password": "admin123"
  }'
```

**Copy from response:**
- `token` → Save as ACCESS_TOKEN
- `refreshToken` → Save as REFRESH_TOKEN

#### Step 2: Use Access Token (Works)
```bash
curl http://localhost:8081/api/users \
  -H "Authorization: Bearer ACCESS_TOKEN"
```
✅ Response: User list

#### Step 3: Wait 1 Hour (or change JWT expiration to 1 minute for testing)

#### Step 4: Use Access Token Again (Fails)
```bash
curl http://localhost:8081/api/users \
  -H "Authorization: Bearer ACCESS_TOKEN"
```
❌ Response: 401 Unauthorized (Token expired)

#### Step 5: Refresh the Token
```bash
curl -X POST http://localhost:8081/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "REFRESH_TOKEN"
  }'
```

✅ Response: NEW access token + NEW refresh token

#### Step 6: Use NEW Access Token (Works)
```bash
curl http://localhost:8081/api/users \
  -H "Authorization: Bearer NEW_ACCESS_TOKEN"
```
✅ Response: User list

---

### Scenario 2: Token Rotation (Security Feature)

#### What is Token Rotation?
Every time you refresh, the old refresh token is deleted and a new one is created.

#### Test It:
```bash
# 1. Use refresh token
curl -X POST http://localhost:8081/auth/refresh \
  -d '{"refreshToken": "abc-123"}'

# ✅ Works, get new tokens

# 2. Try to use the SAME refresh token again
curl -X POST http://localhost:8081/auth/refresh \
  -d '{"refreshToken": "abc-123"}'

# ❌ Fails: "Invalid or expired refresh token"
# Because it was rotated (deleted)
```

**Why?** If someone steals your old refresh token, they can't use it!

---

### Scenario 3: Revoke All Tokens (Password Reset)

```bash
# 1. Login on multiple devices (simulate)
curl -X POST http://localhost:8081/auth/login ...
# Save token as DEVICE1_TOKEN

curl -X POST http://localhost:8081/auth/login ...
# Save token as DEVICE2_TOKEN

# 2. Reset password
curl -X POST http://localhost:8081/auth/reset-password \
  -d '{
    "token": "PASSWORD_RESET_TOKEN",
    "newPassword": "NewPassword123!"
  }'

# 3. Try to use old tokens
curl http://localhost:8081/api/users \
  -H "Authorization: Bearer DEVICE1_TOKEN"

# ❌ Fails: All old sessions are invalidated

# 4. Must login again
curl -X POST http://localhost:8081/auth/login \
  -d '{
    "usernameOrEmail": "admin",
    "password": "NewPassword123!"
  }'

# ✅ Get new tokens
```

---

## 🔍 Check Redis (Optional)

### View Tokens in Redis
```bash
# Connect to Redis
docker exec -it redis-server redis-cli

# List all refresh tokens
KEYS refresh_token:*

# Get user ID for a token
GET refresh_token:abc-123-xyz

# List all tokens for user ID 1
SMEMBERS user_refresh_tokens:1

# Check TTL (time to live)
TTL refresh_token:abc-123-xyz
```

---

## 📊 Token Lifecycle

```
┌─────────────┐
│ User Login  │
└─────┬───────┘
      │
      ▼
┌─────────────────────────┐
│ Generate 2 Tokens       │
│ - Access (1h)           │
│ - Refresh (7d)          │
└─────┬───────────────────┘
      │
      ▼
┌─────────────────────────┐
│ User Makes API Calls    │
│ using Access Token      │
└─────┬───────────────────┘
      │
      ▼
┌─────────────────────────┐
│ Access Token Expires    │
│ (after 1 hour)          │
└─────┬───────────────────┘
      │
      ▼
┌─────────────────────────┐
│ Client Auto-Refresh     │
│ POST /auth/refresh      │
└─────┬───────────────────┘
      │
      ▼
┌─────────────────────────┐
│ Get NEW Tokens          │
│ Old refresh revoked     │
└─────┬───────────────────┘
      │
      ▼
┌─────────────────────────┐
│ Continue API Calls      │
│ Repeat every 1 hour     │
└─────────────────────────┘
```

---

## 🛡️ Security Benefits

1. **Short-lived Access Tokens**
   - If stolen, only valid for 1 hour
   - Limits damage

2. **Token Rotation**
   - Each refresh generates new tokens
   - Old tokens can't be reused
   - Prevents replay attacks

3. **Revocation**
   - Can revoke specific token
   - Can revoke all user tokens
   - Used on password reset, logout

4. **Redis Storage**
   - Server controls token validity
   - Can invalidate anytime
   - Automatic expiration

---

## 🎓 Summary

| Feature | Access Token | Refresh Token |
|---------|-------------|---------------|
| **Lifespan** | 1 hour | 7 days |
| **Purpose** | Access API | Get new access token |
| **Stored in** | Client (localStorage) | Client + Redis |
| **Used for** | Every API call | Only /auth/refresh |
| **If stolen** | Limited damage (1h) | Rotated on use |
| **Revoked on** | Logout, password reset | Logout, password reset |

**Key Point:** Refresh tokens improve user experience (no re-login every hour) while maintaining security!

---

## 📂 Related Files

- `RefreshTokenService.java` - Token management
- `AuthService.java` - Refresh logic (line 108-147)
- `AuthController.java` - `/auth/refresh` endpoint (line 69-75)
- `JwtService.java` - JWT generation
- `RefreshTokenRequest.java` - DTO for request

---

**Created:** 2026-01-05
