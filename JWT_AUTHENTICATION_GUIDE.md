# JWT Authentication Implementation - Complete Walkthrough

## Summary
Successfully implemented enterprise-grade JWT authentication system for the Spring Boot application with secure registration, login, and token-based authorization.

---

## Components Created

### 1. Security Layer

#### ✅ JwtService.java
**Location**: `src/main/java/com/example/demo/security/JwtService.java`

**Features**:
- Generate JWT tokens with configurable expiration
- Extract username and claims from tokens
- Validate token authenticity and expiration
- Uses HS256 algorithm with Base64-encoded secret

#### ✅ JwtAuthFilter.java  
**Location**: `src/main/java/com/example/demo/security/JwtAuthFilter.java`

**Features**:
- Intercepts all HTTP requests
- Extracts JWT from `Authorization: Bearer <token>` header
- Validates token and sets authentication in SecurityContext
- Skips `/auth/**` endpoints (public access)

#### ✅ CustomUserDetails.java (Updated)
**Location**: `src/main/java/com/example/demo/security/CustomUserDetails.java`

**Changes**:
- Added `getUser()` method to expose underlying User entity
- Implements Spring Security's `UserDetails` interface
- Maps User roles to Spring Security authorities

### 2. Configuration

#### ✅ SecurityConfig.java
**Location**: `src/main/java/com/example/demo/config/SecurityConfig.java`

**Configuration**:
- Disabled CSRF (stateless JWT)
- Permitted `/auth/**` endpoints
- Required authentication for all other endpoints
- Stateless session management
- Added JWT filter before UsernamePasswordAuthenticationFilter
- Configured BCrypt password encoder

#### ✅ application.yml (Updated)
**Location**: `src/main/resources/application.yml`

**Added**:
```yaml
app:
  security:
    jwt:
      secret: 404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
      expiration-seconds: 86400  # 24 hours
```

---

## Understanding JWT Structure

A JWT (JSON Web Token) consists of **three parts** separated by dots (`.`):

```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyMTIzIiwicm9sZSI6IkFETUlOIiwiaWF0IjoxNzA0MDY3MjAwLCJleHAiOjE3MDQxNTM2MDB9.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
└──────────── Header ────────────┘.└────────────────────────── Payload ──────────────────────────┘.└───────── Signature ─────────┘
```

### 1. Header

The **header** contains metadata about the token type and signing algorithm.

```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

| Field | Description |
|-------|-------------|
| `alg` | Signing algorithm (e.g., `HS256`, `RS256`) |
| `typ` | Token type (always `JWT`) |

**Common Algorithms**:
| Algorithm | Type | Description |
|-----------|------|-------------|
| `HS256` | Symmetric | HMAC with SHA-256 (shared secret) |
| `HS384` | Symmetric | HMAC with SHA-384 |
| `HS512` | Symmetric | HMAC with SHA-512 |
| `RS256` | Asymmetric | RSA with SHA-256 (public/private key) |

### 2. Payload (Claims)

The **payload** contains **claims** - statements about the user and additional data.

```json
{
  "sub": "user123",
  "email": "user@example.com",
  "role": "ADMIN",
  "iat": 1704067200,
  "exp": 1704153600
}
```

#### Registered Claims (Standard)

| Claim | Full Name | Description |
|-------|-----------|-------------|
| `sub` | Subject | User identifier (username or user ID) |
| `iat` | Issued At | Timestamp when token was created |
| `exp` | Expiration | Timestamp when token expires |
| `iss` | Issuer | Who issued the token (your app name) |
| `aud` | Audience | Intended recipient of the token |
| `nbf` | Not Before | Token not valid before this time |
| `jti` | JWT ID | Unique identifier for the token |

#### Custom Claims (Your Data)

You can add any custom data to the payload:

```json
{
  "sub": "user123",
  "role": "ADMIN",
  "permissions": ["READ", "WRITE", "DELETE"],
  "department": "Engineering",
  "iat": 1704067200,
  "exp": 1704153600
}
```

> [!WARNING]
> **Never store sensitive data in the payload!** The payload is only Base64-encoded, NOT encrypted. Anyone can decode and read it.

### 3. Signature

The **signature** ensures the token hasn't been tampered with.

```
HMACSHA256(
  base64UrlEncode(header) + "." + base64UrlEncode(payload),
  secret
)
```

**How it works**:
1. Server creates token → signs with secret key
2. Client sends token back → server verifies signature
3. If payload is modified → signature becomes invalid

### Java Implementation Example

#### Creating Claims in JwtService

```java
@Service
public class JwtService {

    @Value("${app.security.jwt.secret}")
    private String secretKey;

    @Value("${app.security.jwt.expiration-seconds}")
    private long expirationSeconds;

    public String generateToken(String username, Map<String, Object> extraClaims) {
        return Jwts.builder()
                // Header is set automatically by the library
                
                // Payload - Custom Claims
                .claims(extraClaims)
                
                // Payload - Registered Claims
                .subject(username)                                    // sub
                .issuedAt(new Date())                                 // iat
                .expiration(new Date(System.currentTimeMillis() 
                    + expirationSeconds * 1000))                      // exp
                .issuer("my-spring-app")                              // iss (optional)
                
                // Signature
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    public String generateTokenWithRoles(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", user.getEmail());
        claims.put("roles", user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList()));
        
        return generateToken(user.getUsername(), claims);
    }
}
```

#### Extracting Claims

```java
public Claims extractAllClaims(String token) {
    return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
}

public String extractUsername(String token) {
    return extractAllClaims(token).getSubject();  // Gets "sub" claim
}

public List<String> extractRoles(String token) {
    Claims claims = extractAllClaims(token);
    return claims.get("roles", List.class);  // Gets custom "roles" claim
}

public Date extractExpiration(String token) {
    return extractAllClaims(token).getExpiration();  // Gets "exp" claim
}
```

### Token Lifecycle Diagram

```mermaid
sequenceDiagram
    participant Client
    participant Server
    participant Database
    
    Note over Client,Server: Registration/Login
    Client->>Server: POST /auth/login (credentials)
    Server->>Database: Validate user
    Database-->>Server: User found
    Server->>Server: Create JWT (Header + Payload + Signature)
    Server-->>Client: Return JWT token
    
    Note over Client,Server: Protected Request
    Client->>Server: GET /api/data (Authorization: Bearer token)
    Server->>Server: Extract & verify JWT signature
    Server->>Server: Check expiration (exp claim)
    Server->>Server: Extract user info (sub, roles)
    Server-->>Client: Return protected data
```

---

### 3. Business Logic

#### ✅ AuthService.java
**Location**: `src/main/java/com/example/demo/service/AuthService.java`

**Methods**:
- `register(RegisterRequest)` - Register new user with validation
  - Checks username/email uniqueness
  - Hashes password with BCrypt
  - Assigns default "USER" role
  - Generates JWT token
- `login(AuthRequest)` - Authenticate and generate token
  - Validates credentials
  - Generates JWT token
  - Returns user info with token

### 4. REST API

#### ✅ AuthController.java
**Location**: `src/main/java/com/example/demo/controller/AuthController.java`

**Endpoints**:
- `POST /auth/register` - Register new user
- `POST /auth/login` - Login user

**Features**:
- Uses `ApiResponse` wrapper for consistent responses
- Bean validation with `@Valid`
- Proper HTTP status codes (201 for register, 200 for login)

### 5. Error Handling

#### ✅ GlobalExceptionHandler.java
**Location**: `src/main/java/com/example/demo/exception/GlobalExceptionHandler.java`

**Handles**:
- Validation errors (400)
- Bad credentials (401)
- User not found (404)
- Duplicate username/email (409)
- Generic errors (500)

**Returns**: Consistent `ApiResponse` format with error details

---

## Testing Guide

### Prerequisites
**IMPORTANT**: Restart the application to load all new components!

```powershell
# Stop the current application (Ctrl+C in terminal)
# Then restart it
mvn spring-boot:run
```

### Test 1: Register New User

```powershell
$registerBody = @{
    username = "testuser"
    email = "test@example.com"
    password = "password123"
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri "http://localhost:8081/auth/register" `
    -Method Post `
    -Headers @{'Content-Type'='application/json'} `
    -Body $registerBody

$response | ConvertTo-Json -Depth 10

# Save token for later use
$token = $response.data.token
```

**Expected Response**:
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresAt": "2025-12-19T15:00:00Z",
    "username": "testuser",
    "email": "test@example.com",
    "roles": ["USER"]
  },
  "timestamp": "2025-12-18T15:00:00",
  "statusCode": 201
}
```

### Test 2: Login with Registered User

```powershell
$loginBody = @{
    usernameOrEmail = "testuser"
    password = "password123"
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri "http://localhost:8081/auth/login" `
    -Method Post `
    -Headers @{'Content-Type'='application/json'} `
    -Body $loginBody

$response | ConvertTo-Json -Depth 10

# Save token
$token = $response.data.token
```

**Expected Response**: Same structure as registration

### Test 3: Access Protected Endpoint with Token

```powershell
# Use token from login/register
Invoke-RestMethod -Uri "http://localhost:8081/products" `
    -Method Get `
    -Headers @{
        'Authorization' = "Bearer $token"
        'Content-Type' = 'application/json'
    } | ConvertTo-Json -Depth 10
```

**Expected**: Successfully retrieve products

### Test 4: Access Without Token (Should Fail)

```powershell
try {
    Invoke-RestMethod -Uri "http://localhost:8081/products" -Method Get
} catch {
    Write-Host "Expected error: Unauthorized"
    $_.ErrorDetails.Message
}
```

**Expected**: 401 Unauthorized or 403 Forbidden

### Test 5: Invalid Credentials

```powershell
$invalidLogin = @{
    usernameOrEmail = "testuser"
    password = "wrongpassword"
} | ConvertTo-Json

try {
    Invoke-RestMethod -Uri "http://localhost:8081/auth/login" `
        -Method Post `
        -Headers @{'Content-Type'='application/json'} `
        -Body $invalidLogin
} catch {
    $_.ErrorDetails.Message | ConvertFrom-Json | ConvertTo-Json -Depth 10
}
```

**Expected Response**:
```json
{
  "success": false,
  "message": "Invalid username or password",
  "error": {
    "errorCode": "INVALID_CREDENTIALS"
  },
  "statusCode": 401
}
```

### Test 6: Duplicate Registration

```powershell
# Try to register same username again
$duplicateBody = @{
    username = "testuser"  # Same as before
    email = "different@example.com"
    password = "password456"
} | ConvertTo-Json

try {
    Invoke-RestMethod -Uri "http://localhost:8081/auth/register" `
        -Method Post `
        -Headers @{'Content-Type'='application/json'} `
        -Body $duplicateBody
} catch {
    $_.ErrorDetails.Message | ConvertFrom-Json | ConvertTo-Json -Depth 10
}
```

**Expected Response**:
```json
{
  "success": false,
  "message": "Username already exists",
  "error": {
    "errorCode": "INVALID_ARGUMENT"
  },
  "statusCode": 400
}
```

### Test 7: Validation Errors

```powershell
$invalidBody = @{
    username = "ab"  # Too short (min 3)
    email = "invalid-email"  # Invalid format
    password = "123"  # Too short (min 8)
} | ConvertTo-Json

try {
    Invoke-RestMethod -Uri "http://localhost:8081/auth/register" `
        -Method Post `
        -Headers @{'Content-Type'='application/json'} `
        -Body $invalidBody
} catch {
    $_.ErrorDetails.Message | ConvertFrom-Json | ConvertTo-Json -Depth 10
}
```

**Expected Response**:
```json
{
  "success": false,
  "message": "Validation failed",
  "error": {
    "errorCode": "VALIDATION_ERROR",
    "fieldErrors": {
      "username": "username must be between 3 and 50 characters",
      "email": "email must be valid",
      "password": "password must be at least 8 characters"
    }
  },
  "statusCode": 400
}
```

---

## Security Features Implemented

✅ **Password Hashing**: BCrypt with default strength (10 rounds)  
✅ **JWT Signing**: HS256 algorithm with 256-bit secret key  
✅ **Token Expiration**: 24 hours (configurable)  
✅ **Stateless Sessions**: No server-side session storage  
✅ **Input Validation**: Bean validation on all DTOs  
✅ **Password Hiding**: `@JsonIgnore` on User.password field  
✅ **Error Handling**: No sensitive data in error responses  
✅ **CORS Ready**: Can be configured for production

---

## Password Hashing with BCrypt

Password hashing is essential for secure authentication. **Never store plain-text passwords!** Spring Security provides `BCryptPasswordEncoder` for secure password hashing.

### How BCrypt Works

BCrypt is a password hashing algorithm that:
- Automatically generates a **random salt** for each password
- Uses a **cost factor** (work factor) to slow down brute-force attacks
- Produces a **60-character hash** that includes the salt

### Step 1: Configure Password Encoder Bean

Add the `PasswordEncoder` bean in your `SecurityConfig.java`:

```java
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Default strength: 10
    }
    
    // With custom strength (4-31, higher = slower but more secure)
    @Bean
    public PasswordEncoder passwordEncoderWithStrength() {
        return new BCryptPasswordEncoder(12); // Strength: 12
    }
}
```

### Step 2: Hash Password During Registration

In your `AuthService.java`, inject and use the `PasswordEncoder`:

```java
@Service
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public AuthService(PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    public User register(RegisterRequest request) {
        // Hash the password before saving
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(hashedPassword); // Store hashed password
        
        return userRepository.save(user);
    }
}
```

### Step 3: Verify Password During Login

Use the `matches()` method to verify passwords:

```java
public AuthResponse login(AuthRequest request) {
    User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

    // Compare plain-text password with stored hash
    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
        throw new BadCredentialsException("Invalid credentials");
    }

    // Generate JWT token if password matches
    String token = jwtService.generateToken(user.getUsername());
    return new AuthResponse(token, user);
}
```

### Example: Hashed Password Output

```
Plain text:  password123
BCrypt hash: $2a$10$N9qo8uLOickgx2ZMRZoMye.IjqQBrkH1Qo2w5jT5TY5zKIhvOJN4G
             └─┬─┘└┬┘└──────────────────────────┬────────────────────────────┘
               │   │                             └── Hash + Salt
               │   └── Cost factor (10)
               └── BCrypt algorithm version
```

### Best Practices

| Practice | Description |
|----------|-------------|
| **Never log passwords** | Don't log plain-text or hashed passwords |
| **Use strength ≥ 10** | Default is 10; increase for higher security |
| **Hide password field** | Use `@JsonIgnore` on User entity password field |
| **Use authentication manager** | Let Spring Security handle password verification |

### Using AuthenticationManager (Recommended)

For cleaner code, use Spring's `AuthenticationManager`:

```java
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;

    public AuthResponse login(AuthRequest request) {
        // Spring Security handles password verification automatically
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getUsernameOrEmail(),
                request.getPassword()
            )
        );
        
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(userDetails.getUsername());
        
        return new AuthResponse(token, userDetails.getUser());
    }
}
```

### Common Errors

| Error | Cause | Solution |
|-------|-------|----------|
| `No PasswordEncoder mapped` | Missing PasswordEncoder bean | Add `@Bean` for `BCryptPasswordEncoder` |
| Password always fails | Storing plain-text, comparing with hash | Always encode during registration |
| Hash looks different each time | BCrypt uses random salt (this is normal!) | Use `matches()` to compare, not `equals()` |

---

## Troubleshooting

### Issue: 404 Not Found on /auth/register
**Solution**: Restart the application to load AuthController

### Issue: 401 Unauthorized on protected endpoints
**Solution**: Make sure to include `Authorization: Bearer <token>` header

### Issue: Token expired
**Solution**: Login again to get a new token (24-hour expiration)

### Issue: "Username already exists"
**Solution**: Use a different username or delete the user from database

---

## Product Page Authentication

The product page endpoints are secured using Spring Security. The `SecurityConfig` class defines the security rules for the application. By default, all endpoints, including `/products`, require authentication unless explicitly permitted.

### How It Works
1. **Security Configuration**: The `SecurityConfig` class uses the `SecurityFilterChain` bean to configure HTTP security. The line `.anyRequest().authenticated()` ensures that all endpoints, including `/products`, require authentication.
2. **JWT Authentication**: The `JwtAuthFilter` is added to the security filter chain to validate JWT tokens for incoming requests. If the token is valid, the user is authenticated.
3. **Controller Layer**: The `ProductController` handles requests to `/products`. Since the security configuration requires authentication for all endpoints, users must provide a valid JWT token to access these endpoints.

### How to Remove Authentication for the Product Page
To make the product page publicly accessible, modify the `SecurityConfig` class:

1. Locate the `SecurityConfig` class in `src/main/java/com/example/demo/config/SecurityConfig.java`.
2. Update the `authorizeHttpRequests` method to permit access to `/products`:

   ```java
   .authorizeHttpRequests(auth -> auth
           .requestMatchers("/auth/**").permitAll()
           .requestMatchers("/error").permitAll()
           .requestMatchers("/products/**").permitAll() // Allow public access to products
           .anyRequest().authenticated()
   )
   ```

3. Save the changes and restart the application. The product page will no longer require authentication.

---

## Adding Another Page to Authentication

To secure additional pages with authentication, you need to update the `SecurityConfig` class to include the new endpoint in the security rules.

### Steps to Add Authentication for a New Page

1. **Define the New Endpoint**:
   - Create a new controller or add a new method in an existing controller to handle the new page.
   - For example, if you want to add a `/dashboard` page, define it in a controller:

     ```java
     @RestController
     @RequestMapping("/dashboard")
     public class DashboardController {
         @GetMapping
         public ResponseEntity<String> getDashboard() {
             return ResponseEntity.ok("Welcome to the Dashboard");
         }
     }
     ```

2. **Update the Security Configuration**:
   - Open the `SecurityConfig` class located at `src/main/java/com/example/demo/config/SecurityConfig.java`.
   - Ensure the new endpoint is not explicitly permitted in the `authorizeHttpRequests` method. By default, `.anyRequest().authenticated()` will secure all endpoints unless explicitly allowed.

3. **Test the Authentication**:
   - Restart the application.
   - Access the new page (e.g., `/dashboard`) in your browser or API client.
   - Ensure that a valid JWT token is required to access the page.

### Example Security Configuration

If you want to explicitly secure the `/dashboard` endpoint while keeping other endpoints unchanged, you can modify the `SecurityConfig` as follows:

```java
.authorizeHttpRequests(auth -> auth
        .requestMatchers("/auth/**").permitAll()
        .requestMatchers("/error").permitAll()
        .requestMatchers("/products/**").authenticated() // Ensure products require authentication
        .requestMatchers("/dashboard/**").authenticated() // Add authentication for dashboard
        .anyRequest().authenticated()
)
```

### Notes
- Ensure the new page is properly tested to verify that authentication is enforced.
- If you want to allow specific roles or permissions for the new page, you can use `@PreAuthorize` annotations in the controller methods. For example:

  ```java
  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping
  public ResponseEntity<String> getDashboard() {
      return ResponseEntity.ok("Admin Dashboard");
  }
  ```

---

## Files Created

1. `JwtService.java` - JWT token management
2. `JwtAuthFilter.java` - JWT authentication filter
3. `SecurityConfig.java` - Spring Security configuration
4. `AuthService.java` - Authentication business logic
5. `AuthController.java` - Authentication REST endpoints
6. `GlobalExceptionHandler.java` - Centralized error handling

---

## Conclusion

The JWT authentication system is now fully implemented and ready for testing. All components follow enterprise-grade best practices with proper security, validation, and error handling.

**Remember to restart the application before testing!**

## Step-by-Step Guide to Building Authentication

To build a complete authentication system in a Spring Boot application, follow these steps:

### 1. Create the Security Configuration
- **File**: `src/main/java/com/example/demo/config/SecurityConfig.java`
- **Purpose**: Configure Spring Security to handle authentication and authorization.
- **Steps**:
  1. Annotate the class with `@Configuration`, `@EnableWebSecurity`, and `@EnableMethodSecurity`.
  2. Define a `SecurityFilterChain` bean to configure HTTP security.
  3. Add a JWT filter to the security filter chain.
  4. Disable CSRF and configure stateless session management.

### 2. Implement JWT Token Management
- **File**: `src/main/java/com/example/demo/security/JwtService.java`
- **Purpose**: Handle JWT token generation, validation, and extraction.
- **Steps**:
  1. Use the `io.jsonwebtoken` library to create and parse JWT tokens.
  2. Define methods to generate tokens, validate tokens, and extract claims.
  3. Store the secret key in `application.yml`.

### 3. Create a JWT Authentication Filter
- **File**: `src/main/java/com/example/demo/security/JwtAuthFilter.java`
- **Purpose**: Intercept HTTP requests to validate JWT tokens.
- **Steps**:
  1. Extend `OncePerRequestFilter`.
  2. Extract the token from the `Authorization` header.
  3. Validate the token and set the authentication in the `SecurityContext`.

### 4. Implement User Details Service
- **File**: `src/main/java/com/example/demo/security/CustomUserDetailsService.java`
- **Purpose**: Load user details from the database for authentication.
- **Steps**:
  1. Implement `UserDetailsService`.
  2. Fetch the user from the database using the `UserRepository`.
  3. Map the user entity to Spring Security's `UserDetails`.

### 5. Define User Entity and Repository
- **File**: `src/main/java/com/example/demo/entity/User.java`
- **Purpose**: Represent the user in the database.
- **Steps**:
  1. Annotate the class with `@Entity`.
  2. Define fields for username, email, password, and roles.
  3. Use `@JsonIgnore` to hide the password in API responses.

- **File**: `src/main/java/com/example/demo/repository/UserRepository.java`
- **Purpose**: Provide database access for user-related operations.
- **Steps**:
  1. Extend `JpaRepository`.
  2. Define methods to find users by username or email.

### 6. Create Authentication Service
- **File**: `src/main/java/com/example/demo/service/AuthService.java`
- **Purpose**: Handle business logic for authentication and registration.
- **Steps**:
  1. Implement methods for user registration and login.
  2. Hash passwords using `BCryptPasswordEncoder`.
  3. Generate JWT tokens upon successful login.

### 7. Build Authentication Controller
- **File**: `src/main/java/com/example/demo/controller/AuthController.java`
- **Purpose**: Expose REST endpoints for authentication.
- **Steps**:
  1. Define endpoints for `/auth/register` and `/auth/login`.
  2. Use `@Valid` to validate request bodies.
  3. Return JWT tokens in the response.

### 8. Configure Application Properties
- **File**: `src/main/resources/application.yml`
- **Purpose**: Store configuration values for JWT and security.
- **Steps**:
  1. Add properties for the JWT secret and expiration time.
  2. Configure database connection settings.

### 9. Test the Authentication System
- **Steps**:
  1. Use tools like Postman or cURL to test the `/auth/register` and `/auth/login` endpoints.
  2. Verify that protected endpoints require a valid JWT token.
  3. Test edge cases like invalid tokens, expired tokens, and duplicate registrations.

### Summary
By following these steps, you can build a robust authentication system for your Spring Boot application. Each component plays a critical role in ensuring security and functionality.
