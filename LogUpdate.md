# Log Updates

## Project Feature Summary (Updated: 2025-01-05)

This document provides a comprehensive overview of all features implemented in the Java Spring Bootcamp Loan Management System.

---

## 🏗️ Core Architecture

### Technology Stack
- **Framework**: Spring Boot 4.0+
- **Language**: Java 21
- **Database**: SQL Server with Hibernate JPA
- **Cache**: Redis (session management, token blacklisting, caching)
- **Build Tool**: Maven
- **Utilities**: Lombok

### Project Structure
```
src/main/java/com/example/demo/
├── base/           # ResponseUtil, ApiResponse wrapper
├── config/         # DataInitializer, RedisConfig, SecurityConfig
├── controller/     # 17 REST controllers
├── dto/            # 14 Data Transfer Objects
├── entity/         # 12 JPA entities
├── enums/          # 2 enum types (LoanStatus, LoanAction)
├── exception/      # Global exception handling
├── repository/     # 11 JPA repositories
├── security/       # JWT, RBAC security components
└── service/        # 17 service classes
```

---

## 🔐 Authentication & Security

### 1. JWT Authentication
- **JWT Token-based authentication** for stateless API access
- Token generation on login with configurable expiration
- BCrypt password hashing for secure password storage
- `JwtAuthFilter` for request validation
- See: [JWT_AUTHENTICATION_GUIDE.md](JWT_AUTHENTICATION_GUIDE.md)

### 2. User Registration & Login
- `POST /auth/register` - New user registration with validation
- `POST /auth/login` - Login with username/email and password
- Standardized `ApiResponse` wrapper for all responses

### 3. Secure Logout with Token Blacklisting
- `POST /auth/logout` - Invalidate JWT token
- Redis-based token blacklist with TTL matching token expiration
- Prevents reuse of logged-out tokens
- See: [logout_documentation.md](logout_documentation.md)

### 4. Forgot Password / Password Reset
- `POST /auth/forgot-password` - Request password reset link
- `POST /auth/reset-password` - Reset password with token
- Redis-based token storage with 1-hour TTL
- Session invalidation after password reset (all old tokens rejected)
- Email integration (SMTP/Gmail/Mailtrap)
- See: [forgot_password.md](forgot_password.md)

---

## 👥 Role-Based Access Control (RBAC)

### Dynamic Permission System
- **User** → has many **Roles** → has many **Menus/Permissions**
- `DynamicAuthorizationManager` for URL pattern-based access control
- `AntPathMatcher` for flexible URL matching
- Permission enforcement via `RoleMenu` mapping

### Default Roles
| Role | Description |
|------|-------------|
| ADMIN | Full system access |
| MARKETING | Loan review, customer interaction |
| BRANCH_MANAGER | Loan approval/rejection |
| BACK_OFFICE | Loan disbursement |
| USER | Standard customer access |

### Permission Categories
- **User Management**: USER_READ, USER_CREATE, USER_UPDATE, USER_DELETE
- **Role Management**: ROLE_READ, ROLE_ASSIGN, ROLE_MANAGE
- **Product Management**: PRODUCT_READ, PRODUCT_MANAGE
- **Loan Workflow**: LOAN_CREATE, LOAN_REVIEW, LOAN_APPROVE, LOAN_REJECT, LOAN_DISBURSE
- **Profile**: PROFILE_READ, PROFILE_UPDATE

See: [rbac.md](rbac.md)

---

## 💰 Loan Approval Workflow

### State Machine Implementation
Complete loan lifecycle management with role-based queues:

```
SUBMITTED → IN_REVIEW → WAITING_APPROVAL → APPROVED_WAITING_DISBURSEMENT → DISBURSED
                ↓                ↓
             REJECTED         REJECTED
```

### API Endpoints
- `POST /api/loan-workflow/submit` - Submit loan application
- `POST /api/loan-workflow/action` - Perform workflow action
- `GET /api/loan-workflow/queue/marketing` - Marketing queue
- `GET /api/loan-workflow/queue/branch-manager` - Manager queue  
- `GET /api/loan-workflow/queue/back-office` - Back office queue
- `GET /api/loan-workflow/{loanId}/allowed-actions` - Get allowed actions

### Workflow Actions
| Action | Allowed By | Status Transition |
|--------|-----------|-------------------|
| COMMENT | MARKETING | SUBMITTED → IN_REVIEW |
| FORWARD_TO_MANAGER | MARKETING | IN_REVIEW → WAITING_APPROVAL |
| APPROVE | BRANCH_MANAGER | WAITING_APPROVAL → APPROVED_WAITING_DISBURSEMENT |
| REJECT | BRANCH_MANAGER | any → REJECTED |
| DISBURSE | BACK_OFFICE | APPROVED_WAITING_DISBURSEMENT → DISBURSED |

### Loan History & Notifications
- All actions logged in `loan_history` table
- Automatic notifications to stakeholders on status changes

See: [LOAN_WORKFLOW_DOCUMENTATION.md](LOAN_WORKFLOW_DOCUMENTATION.md), [Simulation.md](Simulation.md)

---

## 📦 Product Management

### Loan Products
- Product CRUD operations with validation
- Fields: code, name, interest rate, min/max amount, tenure
- Active/inactive status management
- Redis caching for performance

### API Endpoints
- `GET /api/products` - List all products
- `POST /api/products` - Create product (ADMIN)
- `PUT /api/products/{id}` - Update product
- `DELETE /api/products/{id}` - Soft delete product

---

## 🗂️ User & Profile Management

### User Management (Admin)
- `GET /api/users` - List users
- `POST /api/users` - Create user
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Soft delete user

### User Profile
- `GET /api/user-profiles/{userId}` - Get profile
- `PUT /api/user-profiles/{userId}` - Update profile

### User-Product Relationship
- Track user's loan product applications
- `GET /api/user-products` - List user products

---

## 🚀 Redis Caching

### Implementation
- Spring Cache abstraction with Redis backend
- `@Cacheable` for read operations
- `@CacheEvict` for write operations
- Configurable TTL per cache region

### Cache Regions
- `products`, `activeProducts`, `productByCode`
- `users`, `userById`, `userByEmail`

### Features
- JSON serialization for cached objects
- Java 8 Date/Time support
- Cache warming on startup (optional)
- Redis Insight for monitoring

See: [REDIS_CACHE_GUIDE.md](REDIS_CACHE_GUIDE.md)

---

## 🗑️ Soft Delete

### Implementation
All major entities support soft delete:
- `User`, `Role`, `RoleMenu`, `Product`
- Fields: `is_deleted`, `is_active`

### Behavior
- DELETE endpoints set `is_deleted = true`, `is_active = false`
- All queries filter out deleted records
- Data preserved for audit purposes

See: [SOFT_DELETE_TESTING.md](SOFT_DELETE_TESTING.md)

---

## 🔔 Notification System

### In-App Notifications
- Automatic notifications on loan status changes
- User-specific notification inbox
- `GET /api/notifications` - Get user notifications
- `PUT /api/notifications/{id}/read` - Mark as read

---

## 🐳 Docker Deployment

### Container Support
Located in `deploy/` folder:
- `Dockerfile` - Multi-stage build for Spring Boot
- `docker-compose.yml` - Full stack (App + SQL Server + Redis + Nginx)
- `nginx.conf` - Reverse proxy configuration

### Documentation
- [migration_guide.md](deploy/migration_guide.md) - Windows to Linux migration
- [deployment_strategy.md](deploy/deployment_strategy.md) - Production deployment

---

## 📚 Documentation Files

| File | Description |
|------|-------------|
| [README.md](README.md) | Project overview & quick start |
| [DOCUMENTATION.md](DOCUMENTATION.md) | JPA annotations & ORM explanation |
| [JWT_AUTHENTICATION_GUIDE.md](JWT_AUTHENTICATION_GUIDE.md) | JWT implementation details |
| [rbac.md](rbac.md) | RBAC system documentation |
| [LOAN_WORKFLOW_DOCUMENTATION.md](LOAN_WORKFLOW_DOCUMENTATION.md) | Loan workflow guide |
| [REDIS_CACHE_GUIDE.md](REDIS_CACHE_GUIDE.md) | Caching implementation |
| [forgot_password.md](forgot_password.md) | Password reset feature |
| [logout_documentation.md](logout_documentation.md) | Secure logout |
| [Simulation.md](Simulation.md) | Loan workflow simulation |
| [TESTING.md](TESTING.md) | Testing guide |
| [QUICK_START.md](QUICK_START.md) | Quick start guide |
| [CARA_MENJALANKAN.md](CARA_MENJALANKAN.md) | Running instructions (Indonesian) |

---

## 🧪 Testing & Security

### Testing Resources
- [TESTING.md](TESTING.md) - API testing guide
- [OWASP_TOP10_TESTING.md](OWASP_TOP10_TESTING.md) - Security testing
- [pentest_history.md](pentest_history.md) - Penetration test results

---

## 📝 Change Log

### 2025-01-05
- **Documentation Update**: Created comprehensive feature summary in LogUpdate.md

### 2025-12-30
- **Docker Support**: Added Dockerfile, docker-compose.yml, and migration documentation for Linux cloud deployment

### 2025-12-29
- **RBAC Soft Delete**: Implemented soft delete for User, Role, RoleMenu entities
- **Forgot Password**: Added Redis-based password reset with session invalidation
- **Secure Logout**: Implemented token blacklisting via Redis
- **Loan Workflow**: Refined RBAC integration and queue access

### 2025-12-23
- Added centralized response builder in `ResponseUtil.java`
- Refactored `AuthController.java` to use ResponseUtil
- Updated `UserController.java` with standardized ApiResponse

### 2025-12-22
- **Redis Cache**: Implemented caching for Product entity
- **Product Schema Update**: Added loan product fields (interest, amounts, tenure)
- **Loan Approval Workflow**: Complete state machine implementation

### 2025-12-18
- **JWT Authentication**: Enterprise-grade JWT implementation
- **User & Role Management**: Many-to-Many relationship with junction table
