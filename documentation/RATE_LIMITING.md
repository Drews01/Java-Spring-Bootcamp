# Rate Limiting

Rate limiting protects authentication endpoints from brute force attacks and abuse by limiting the number of requests per IP address within a time window.

## Protected Endpoints

| Endpoint | Max Attempts | Time Window | Use Case |
|----------|--------------|-------------|----------|
| `POST /auth/login` | 5 | 60 seconds | Prevents password brute force |
| `POST /auth/register` | 3 | 60 seconds | Prevents spam account creation |
| `POST /auth/forgot-password` | 2 | 60 seconds | Prevents email flooding |

## How It Works

1. **Client IP Extraction**: The system identifies clients by IP address, supporting `X-Forwarded-For` header for proxied requests
2. **Token Bucket Algorithm**: Uses Bucket4j to implement token bucket rate limiting
3. **In-Memory Storage**: Rate limit buckets are stored in memory (per instance)

## Response When Rate Limited

When the rate limit is exceeded, the API returns:

```http
HTTP/1.1 429 Too Many Requests
Content-Type: application/json
Retry-After: 60

{
  "timestamp": "2026-02-09T14:45:00",
  "status": 429,
  "error": "Too Many Requests",
  "message": "Rate limit exceeded. Please try again after 60 seconds.",
  "retryAfterSeconds": 60
}
```

## Configuration

Rate limits can be customized in `application.yml`:

```yaml
app:
  rate-limit:
    login:
      max-attempts: 5
      duration-seconds: 60
    register:
      max-attempts: 3
      duration-seconds: 60
    forgot-password:
      max-attempts: 2
      duration-seconds: 60
```

## Implementation Details

### Components

| Component | Location | Description |
|-----------|----------|-------------|
| `RateLimitConfig` | `config/RateLimitConfig.java` | Configuration properties |
| `RateLimitingService` | `service/RateLimitingService.java` | Core rate limiting logic |
| `RateLimitFilter` | `security/RateLimitFilter.java` | Servlet filter applying limits |

### Filter Chain Order

The `RateLimitFilter` is registered before the JWT authentication filter to catch abusive requests early, before any authentication processing occurs.

## Client-Side Handling

When implementing clients:

1. **Check for 429 status code** in API responses
2. **Read `Retry-After` header** for wait time
3. **Implement exponential backoff** for retries
4. **Show user-friendly messages** explaining the wait time

### Example (JavaScript)

```javascript
async function login(credentials) {
  const response = await fetch('/auth/login', {
    method: 'POST',
    body: JSON.stringify(credentials)
  });
  
  if (response.status === 429) {
    const retryAfter = response.headers.get('Retry-After');
    throw new Error(`Too many attempts. Try again in ${retryAfter} seconds.`);
  }
  
  return response.json();
}
```

## Security Considerations

> [!IMPORTANT]
> Rate limiting is **per-instance** in the current implementation. For distributed deployments with multiple instances, consider implementing Redis-backed rate limiting for consistent limits across all instances.

## Troubleshooting

### Rate limit too aggressive
Increase `max-attempts` in configuration.

### Legitimate users getting blocked
- Verify `X-Forwarded-For` header is being set correctly by your proxy
- Consider increasing limits for specific IP ranges (requires code modification)

### Rate limits not applying
- Verify the filter is registered in `SecurityConfig.java`
- Check that the endpoint path matches exactly (`/auth/login`, not `/auth/login/`)
