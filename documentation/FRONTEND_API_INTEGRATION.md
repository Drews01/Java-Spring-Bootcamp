# Frontend API Integration Guide

Complete reference for frontend developers to integrate with the Spring Boot backend, including authentication, CSRF protection, and all Loan Workflow endpoints.

---

## Table of Contents

1. [Authentication Flow](#authentication-flow)
2. [Making Authenticated Requests](#making-authenticated-requests)
3. [Loan Workflow API Reference](#loan-workflow-api-reference)
4. [Complete Angular Service Example](#complete-angular-service-example)
5. [Quick Troubleshooting](#quick-troubleshooting)

---

## Authentication Flow

### Step 1: Login (Get JWT Cookie)

```http
POST /auth/login
Content-Type: application/json

{
  "usernameOrEmail": "marketing_user",
  "password": "password123"
}
```

**Response:** Sets `jwt` cookie (HttpOnly) automatically.

### Step 2: Fetch CSRF Token

```http
GET /api/csrf-token
```

**Response:**
```json
{
  "token": "abc123xyz...",
  "headerName": "X-XSRF-TOKEN"
}
```

### Step 3: Check Current User

```http
GET /auth/me
```

**Response:**
```json
{
  "status": 200,
  "message": "Current user retrieved successfully",
  "data": {
    "id": 1,
    "username": "marketing_user",
    "email": "marketing@example.com",
    "roles": ["MARKETING"],
    "branchName": "Branch A"
  }
}
```

---

## Making Authenticated Requests

### ⚠️ Critical: Two Things Required for POST/PUT/DELETE Requests

| Requirement | How to Include |
|-------------|----------------|
| **JWT Cookie** | Use `withCredentials: true` (sent automatically by browser) |
| **CSRF Token** | Add header: `X-XSRF-TOKEN: <token>` |

### Request Template

```http
POST /api/loan-workflow/action
Content-Type: application/json
X-XSRF-TOKEN: <csrf_token_from_step_2>

{
  "loanApplicationId": 123,
  "action": "COMMENT",
  "comment": "Reviewed - looks good"
}
```

---

## Loan Workflow API Reference

### Base URL: `/api/loan-workflow`

### Queue Endpoints (GET - No CSRF Required)

| Endpoint | Role | Description |
|----------|------|-------------|
| `GET /queue/marketing?page=0&size=10` | MARKETING | Loans in SUBMITTED/IN_REVIEW status |
| `GET /queue/branch-manager?page=0&size=10` | BRANCH_MANAGER | Loans in WAITING_APPROVAL status |
| `GET /queue/back-office?page=0&size=10` | BACK_OFFICE | Loans in APPROVED_WAITING_DISBURSEMENT status |

### History Endpoints (GET - No CSRF Required)

| Endpoint | Parameters |
|----------|------------|
| `GET /history/marketing` | `?month=1&year=2026&page=0&size=20` |
| `GET /history/branch-manager` | `?month=1&year=2026&page=0&size=20` |
| `GET /history/back-office` | `?month=1&year=2026&page=0&size=20` |

### Action Endpoints (POST - ✅ CSRF Required)

#### Submit a Loan Application
```http
POST /api/loan-workflow/submit
Content-Type: application/json
X-XSRF-TOKEN: <csrf_token>

{
  "productId": 1,
  "amount": 50000000,
  "tenureMonths": 12,
  "interestRateApplied": 12.5,
  "branchId": 1
}
```

#### Perform Action (Comment/Approve/Reject/Disburse)
```http
POST /api/loan-workflow/action
Content-Type: application/json
X-XSRF-TOKEN: <csrf_token>

{
  "loanApplicationId": 123,
  "action": "COMMENT",
  "comment": "This is my review comment"
}
```

**Available Actions by Role:**

| Role | Allowed Actions |
|------|-----------------|
| MARKETING | `COMMENT`, `FORWARD_TO_MANAGER` |
| BRANCH_MANAGER | `APPROVE`, `REJECT`, `RETURN_TO_REVIEW` |
| BACK_OFFICE | `DISBURSE`, `HOLD` |

#### Get Allowed Actions for a Loan
```http
GET /api/loan-workflow/{loanId}/allowed-actions
```

---

## Complete Angular Service Example

### 1. CSRF Interceptor (`csrf.interceptor.ts`)

```typescript
import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { CsrfService } from './csrf.service';

export const csrfInterceptor: HttpInterceptorFn = (req, next) => {
  const csrfService = inject(CsrfService);
  
  // Clone request to add withCredentials for all requests
  let modifiedReq = req.clone({ withCredentials: true });
  
  // Add CSRF token for state-changing requests
  if (['POST', 'PUT', 'DELETE', 'PATCH'].includes(req.method)) {
    const token = csrfService.getToken();
    if (token) {
      modifiedReq = modifiedReq.clone({
        headers: modifiedReq.headers.set('X-XSRF-TOKEN', token)
      });
    }
  }
  
  return next(modifiedReq);
};
```

### 2. CSRF Service (`csrf.service.ts`)

```typescript
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

interface CsrfResponse {
  token: string;
  headerName: string;
}

@Injectable({ providedIn: 'root' })
export class CsrfService {
  private http = inject(HttpClient);
  private csrfToken: string | null = null;

  async fetchToken(): Promise<void> {
    try {
      const response = await firstValueFrom(
        this.http.get<CsrfResponse>('/api/csrf-token', { withCredentials: true })
      );
      this.csrfToken = response.token;
      console.log('CSRF token fetched successfully');
    } catch (error) {
      console.error('Failed to fetch CSRF token:', error);
    }
  }

  getToken(): string | null {
    return this.csrfToken;
  }
}
```

### 3. App Initialization (`app.config.ts`)

```typescript
import { ApplicationConfig, APP_INITIALIZER, inject } from '@angular/core';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { csrfInterceptor } from './csrf.interceptor';
import { CsrfService } from './csrf.service';

function initializeCsrf(): () => Promise<void> {
  const csrfService = inject(CsrfService);
  return () => csrfService.fetchToken();
}

export const appConfig: ApplicationConfig = {
  providers: [
    provideHttpClient(withInterceptors([csrfInterceptor])),
    {
      provide: APP_INITIALIZER,
      useFactory: initializeCsrf,
      multi: true
    }
  ]
};
```

### 4. Loan Workflow Service (`loan-workflow.service.ts`)

```typescript
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

interface LoanActionRequest {
  loanApplicationId: number;
  action: string;
  comment?: string;
}

interface ApiResponse<T> {
  status: number;
  message: string;
  data: T;
}

@Injectable({ providedIn: 'root' })
export class LoanWorkflowService {
  private http = inject(HttpClient);
  private baseUrl = '/api/loan-workflow';

  // Queue endpoints (GET - no CSRF needed)
  getMarketingQueue(page = 0, size = 10): Observable<ApiResponse<any>> {
    return this.http.get<ApiResponse<any>>(
      `${this.baseUrl}/queue/marketing?page=${page}&size=${size}`
    );
  }

  getBranchManagerQueue(page = 0, size = 10): Observable<ApiResponse<any>> {
    return this.http.get<ApiResponse<any>>(
      `${this.baseUrl}/queue/branch-manager?page=${page}&size=${size}`
    );
  }

  getBackOfficeQueue(page = 0, size = 10): Observable<ApiResponse<any>> {
    return this.http.get<ApiResponse<any>>(
      `${this.baseUrl}/queue/back-office?page=${page}&size=${size}`
    );
  }

  // Action endpoint (POST - CSRF required, handled by interceptor)
  performAction(request: LoanActionRequest): Observable<ApiResponse<any>> {
    return this.http.post<ApiResponse<any>>(
      `${this.baseUrl}/action`,
      request
    );
  }

  // Get allowed actions for a loan
  getAllowedActions(loanId: number): Observable<ApiResponse<string[]>> {
    return this.http.get<ApiResponse<string[]>>(
      `${this.baseUrl}/${loanId}/allowed-actions`
    );
  }
}
```

### 5. Component Usage Example

```typescript
// In your component
async submitComment(loanId: number, comment: string) {
  const request: LoanActionRequest = {
    loanApplicationId: loanId,
    action: 'COMMENT',
    comment: comment
  };
  
  this.loanWorkflowService.performAction(request).subscribe({
    next: (response) => {
      console.log('Comment submitted:', response.message);
      // Refresh the queue
      this.loadQueue();
    },
    error: (error) => {
      if (error.status === 403) {
        console.error('403 Forbidden - Check CSRF token');
        // Re-fetch CSRF token and retry
        this.csrfService.fetchToken().then(() => {
          // Retry the request
        });
      }
    }
  });
}
```

---

## Quick Troubleshooting

| Error | Cause | Solution |
|-------|-------|----------|
| `403 Forbidden` on POST | Missing/invalid CSRF token | 1. Check token is fetched via `/api/csrf-token` <br> 2. Verify `X-XSRF-TOKEN` header is present in request |
| `401 Unauthorized` | JWT cookie not sent | Ensure `withCredentials: true` on all requests |
| `CSRF token not found` | Token not fetched on app init | Call `/api/csrf-token` in `APP_INITIALIZER` |
| Cookie not set after login | CORS configuration issue | Backend must have `allowCredentials(true)` |

### Browser DevTools Check

1. **Network Tab** → Check your failing request:
   - Is `X-XSRF-TOKEN` header present?
   - Is `Cookie: jwt=...` header present?

2. **Application Tab** → Cookies:
   - `jwt` cookie exists with `HttpOnly` flag
   - `XSRF-TOKEN` cookie exists (readable)

### Quick Debug Steps

```typescript
// Add this before making the request
console.log('CSRF Token:', this.csrfService.getToken());

// Check if cookies are being sent (in interceptor)
console.log('Request withCredentials:', req.withCredentials);
```

---

## Summary: Checklist for Every POST Request

- [x] User is logged in (`jwt` cookie exists)
- [x] CSRF token fetched from `/api/csrf-token`
- [x] `withCredentials: true` is set
- [x] `X-XSRF-TOKEN` header contains the token
- [x] `Content-Type: application/json` header is set
- [x] Request body matches expected DTO structure
