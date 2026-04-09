# User Authentication Flow

<cite>
**Referenced Files in This Document**
- [AuthController.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/web/AuthController.java)
- [AuthApiController.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/web/api/AuthApiController.java)
- [ContractorLoginUseCase.java](file://src/main/java/root/cyb/mh/skylink_media_service/application/usecases/ContractorLoginUseCase.java)
- [LoginRequest.java](file://src/main/java/root/cyb/mh/skylink_media_service/application/dto/api/LoginRequest.java)
- [LoginResponse.java](file://src/main/java/root/cyb/mh/skylink_media_service/application/dto/api/LoginResponse.java)
- [JwtTokenProvider.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/security/jwt/JwtTokenProvider.java)
- [CustomUserDetailsService.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/security/CustomUserDetailsService.java)
- [SecurityConfig.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/config/SecurityConfig.java)
- [CustomLogoutHandler.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/security/CustomLogoutHandler.java)
- [LoginAuditLog.java](file://src/main/java/root/cyb/mh/skylink_media_service/domain/entities/LoginAuditLog.java)
- [LoginAuditLogRepository.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/persistence/LoginAuditLogRepository.java)
- [Contractor.java](file://src/main/java/root/cyb/mh/skylink_media_service/domain/entities/Contractor.java)
- [Admin.java](file://src/main/java/root/cyb/mh/skylink_media_service/domain/entities/Admin.java)
- [SuperAdmin.java](file://src/main/java/root/cyb/mh/skylink_media_service/domain/entities/SuperAdmin.java)
- [User.java](file://src/main/java/root/cyb/mh/skylink_media_service/domain/entities/User.java)
- [ContractorRepository.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/persistence/ContractorRepository.java)
- [UserRepository.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/persistence/UserRepository.java)
- [GlobalApiExceptionHandler.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/web/api/exception/GlobalApiExceptionHandler.java)
</cite>

## Table of Contents
1. [Introduction](#introduction)
2. [Project Structure](#project-structure)
3. [Core Components](#core-components)
4. [Architecture Overview](#architecture-overview)
5. [Detailed Component Analysis](#detailed-component-analysis)
6. [Dependency Analysis](#dependency-analysis)
7. [Performance Considerations](#performance-considerations)
8. [Troubleshooting Guide](#troubleshooting-guide)
9. [Conclusion](#conclusion)

## Introduction
This document explains the complete user authentication workflow from login initiation to session establishment. It traces the flow through both MVC and API controllers, credential validation, password verification, token generation, and role-specific authentication paths. It also documents login schemas, error handling, session management, remember-me functionality, concurrent session control, brute force protection, and logout procedures.

## Project Structure
Authentication spans MVC controllers, API controllers, use cases, JWT infrastructure, domain entities, repositories, and security configuration.

```mermaid
graph TB
subgraph "Controllers"
MVC["AuthController<br/>MVC Login Form"]
API["AuthApiController<br/>REST Login Endpoint"]
end
subgraph "Application Layer"
UC["ContractorLoginUseCase<br/>Role-specific Login"]
end
subgraph "Security Infrastructure"
SEC["SecurityConfig<br/>Security Filters"]
JWT["JwtTokenProvider<br/>Token Generation/Validation"]
DETAIL["CustomUserDetailsService<br/>User Details Loader"]
LOGOUT["CustomLogoutHandler<br/>Logout Handler"]
end
subgraph "Domain & Persistence"
ENT_USERS["User/Contractor/Admin/SuperAdmin"]
REPO_USERS["UserRepository"]
REPO_CONTRACTOR["ContractorRepository"]
AUDIT["LoginAuditLog<br/>LoginAuditLogRepository"]
end
MVC --> API
API --> UC
UC --> DETAIL
DETAIL --> REPO_USERS
DETAIL --> REPO_CONTRACTOR
UC --> JWT
SEC --> JWT
SEC --> LOGOUT
UC --> AUDIT
```

**Diagram sources**
- [AuthController.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/web/AuthController.java)
- [AuthApiController.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/web/api/AuthApiController.java)
- [ContractorLoginUseCase.java](file://src/main/java/root/cyb/mh/skylink_media_service/application/usecases/ContractorLoginUseCase.java)
- [JwtTokenProvider.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/security/jwt/JwtTokenProvider.java)
- [CustomUserDetailsService.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/security/CustomUserDetailsService.java)
- [SecurityConfig.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/config/SecurityConfig.java)
- [CustomLogoutHandler.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/security/CustomLogoutHandler.java)
- [LoginAuditLog.java](file://src/main/java/root/cyb/mh/skylink_media_service/domain/entities/LoginAuditLog.java)
- [LoginAuditLogRepository.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/persistence/LoginAuditLogRepository.java)
- [UserRepository.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/persistence/UserRepository.java)
- [ContractorRepository.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/persistence/ContractorRepository.java)

**Section sources**
- [AuthController.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/web/AuthController.java)
- [AuthApiController.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/web/api/AuthApiController.java)
- [ContractorLoginUseCase.java](file://src/main/java/root/cyb/mh/skylink_media_service/application/usecases/ContractorLoginUseCase.java)
- [JwtTokenProvider.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/security/jwt/JwtTokenProvider.java)
- [CustomUserDetailsService.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/security/CustomUserDetailsService.java)
- [SecurityConfig.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/config/SecurityConfig.java)
- [CustomLogoutHandler.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/security/CustomLogoutHandler.java)
- [LoginAuditLog.java](file://src/main/java/root/cyb/mh/skylink_media_service/domain/entities/LoginAuditLog.java)
- [LoginAuditLogRepository.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/persistence/LoginAuditLogRepository.java)
- [UserRepository.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/persistence/UserRepository.java)
- [ContractorRepository.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/persistence/ContractorRepository.java)

## Core Components
- AuthController: Handles form-based login via MVC, delegates to use cases, and manages session attributes.
- AuthApiController: Provides REST endpoint for login, returning tokens and user info.
- ContractorLoginUseCase: Orchestrates contractor-specific login, credential validation, and token issuance.
- LoginRequest/LoginResponse: Request/response DTOs for login operations.
- JwtTokenProvider: Generates and validates JWT tokens.
- CustomUserDetailsService: Loads user details for authentication and authorization.
- SecurityConfig: Configures Spring Security filters, CSRF, session management, and logout behavior.
- CustomLogoutHandler: Implements logout cleanup and token invalidation.
- LoginAuditLog/LoginAuditLogRepository: Tracks login attempts for audit and brute-force detection.
- Role Entities: User, Contractor, Admin, SuperAdmin define roles and authorities.

**Section sources**
- [AuthController.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/web/AuthController.java)
- [AuthApiController.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/web/api/AuthApiController.java)
- [ContractorLoginUseCase.java](file://src/main/java/root/cyb/mh/skylink_media_service/application/usecases/ContractorLoginUseCase.java)
- [LoginRequest.java](file://src/main/java/root/cyb/mh/skylink_media_service/application/dto/api/LoginRequest.java)
- [LoginResponse.java](file://src/main/java/root/cyb/mh/skylink_media_service/application/dto/api/LoginResponse.java)
- [JwtTokenProvider.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/security/jwt/JwtTokenProvider.java)
- [CustomUserDetailsService.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/security/CustomUserDetailsService.java)
- [SecurityConfig.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/config/SecurityConfig.java)
- [CustomLogoutHandler.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/security/CustomLogoutHandler.java)
- [LoginAuditLog.java](file://src/main/java/root/cyb/mh/skylink_media_service/domain/entities/LoginAuditLog.java)
- [LoginAuditLogRepository.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/persistence/LoginAuditLogRepository.java)
- [User.java](file://src/main/java/root/cyb/mh/skylink_media_service/domain/entities/User.java)
- [Contractor.java](file://src/main/java/root/cyb/mh/skylink_media_service/domain/entities/Contractor.java)
- [Admin.java](file://src/main/java/root/cyb/mh/skylink_media_service/domain/entities/Admin.java)
- [SuperAdmin.java](file://src/main/java/root/cyb/mh/skylink_media_service/domain/entities/SuperAdmin.java)

## Architecture Overview
The authentication pipeline integrates MVC and REST login paths, centralized credential validation, role-specific use cases, JWT issuance, and robust session management.

```mermaid
sequenceDiagram
participant Browser as "Browser"
participant MVC as "AuthController"
participant API as "AuthApiController"
participant UC as "ContractorLoginUseCase"
participant UDS as "CustomUserDetailsService"
participant JWT as "JwtTokenProvider"
participant CFG as "SecurityConfig"
Browser->>MVC : "GET /login"
MVC-->>Browser : "Render login form"
Browser->>API : "POST /api/auth/login {email,password}"
API->>UC : "execute(loginRequest)"
UC->>UDS : "loadUserByUsername(email)"
UDS-->>UC : "UserDetails"
UC->>UC : "validate credentials"
alt "Credentials valid"
UC->>JWT : "generateToken(userDetails)"
JWT-->>UC : "JWT token"
UC-->>API : "LoginResponse(token,user)"
API-->>Browser : "200 OK {token,user}"
else "Credentials invalid"
UC-->>API : "Authentication failure"
API-->>Browser : "401 Unauthorized"
end
```

**Diagram sources**
- [AuthController.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/web/AuthController.java)
- [AuthApiController.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/web/api/AuthApiController.java)
- [ContractorLoginUseCase.java](file://src/main/java/root/cyb/mh/skylink_media_service/application/usecases/ContractorLoginUseCase.java)
- [CustomUserDetailsService.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/security/CustomUserDetailsService.java)
- [JwtTokenProvider.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/security/jwt/JwtTokenProvider.java)
- [SecurityConfig.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/config/SecurityConfig.java)

## Detailed Component Analysis

### Login Request/Response Schemas
- LoginRequest: Contains email and password fields for authentication.
- LoginResponse: Contains the issued token and user identity information.

These DTOs define the contract for the login API and are validated by the controller layer.

**Section sources**
- [LoginRequest.java](file://src/main/java/root/cyb/mh/skylink_media_service/application/dto/api/LoginRequest.java)
- [LoginResponse.java](file://src/main/java/root/cyb/mh/skylink_media_service/application/dto/api/LoginResponse.java)
- [AuthApiController.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/web/api/AuthApiController.java)

### AuthController (MVC Login)
- Renders the login page.
- Processes form submissions.
- Delegates to use cases for authentication.
- Manages session attributes and redirects after successful login.

```mermaid
flowchart TD
Start(["User visits /login"]) --> Render["Render login form"]
Render --> Submit["Form submit"]
Submit --> Validate["Validate input fields"]
Validate --> Valid{"Valid?"}
Valid --> |No| Error["Show validation errors"]
Valid --> |Yes| Delegate["Delegate to use case"]
Delegate --> Result{"Authenticated?"}
Result --> |Yes| SetAttrs["Set session attributes"]
SetAttrs --> Redirect["Redirect to dashboard"]
Result --> |No| Error
```

**Diagram sources**
- [AuthController.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/web/AuthController.java)

**Section sources**
- [AuthController.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/web/AuthController.java)

### AuthApiController (REST Login)
- Accepts POST requests to /api/auth/login.
- Validates LoginRequest payload.
- Calls ContractorLoginUseCase to authenticate.
- Returns LoginResponse with token and user info on success; otherwise returns appropriate error responses.

```mermaid
sequenceDiagram
participant Client as "Client"
participant API as "AuthApiController"
participant UC as "ContractorLoginUseCase"
participant JWT as "JwtTokenProvider"
Client->>API : "POST /api/auth/login {email,password}"
API->>UC : "execute(LoginRequest)"
UC->>JWT : "generateToken(userDetails)"
JWT-->>UC : "token"
UC-->>API : "LoginResponse"
API-->>Client : "200 OK {token,user}"
```

**Diagram sources**
- [AuthApiController.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/web/api/AuthApiController.java)
- [ContractorLoginUseCase.java](file://src/main/java/root/cyb/mh/skylink_media_service/application/usecases/ContractorLoginUseCase.java)
- [JwtTokenProvider.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/security/jwt/JwtTokenProvider.java)

**Section sources**
- [AuthApiController.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/web/api/AuthApiController.java)

### ContractorLoginUseCase Implementation
- Loads user details via CustomUserDetailsService.
- Validates credentials (password encoding verification).
- Generates JWT token using JwtTokenProvider.
- Records login audit events.
- Returns LoginResponse containing token and user identity.

```mermaid
classDiagram
class ContractorLoginUseCase {
+execute(loginRequest) LoginResponse
}
class CustomUserDetailsService {
+loadUserByUsername(username) UserDetails
}
class JwtTokenProvider {
+generateToken(userDetails) String
}
class LoginAuditLogRepository {
+save(auditLog) LoginAuditLog
}
ContractorLoginUseCase --> CustomUserDetailsService : "loads user"
ContractorLoginUseCase --> JwtTokenProvider : "generates token"
ContractorLoginUseCase --> LoginAuditLogRepository : "records audit"
```

**Diagram sources**
- [ContractorLoginUseCase.java](file://src/main/java/root/cyb/mh/skylink_media_service/application/usecases/ContractorLoginUseCase.java)
- [CustomUserDetailsService.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/security/CustomUserDetailsService.java)
- [JwtTokenProvider.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/security/jwt/JwtTokenProvider.java)
- [LoginAuditLogRepository.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/persistence/LoginAuditLogRepository.java)

**Section sources**
- [ContractorLoginUseCase.java](file://src/main/java/root/cyb/mh/skylink_media_service/application/usecases/ContractorLoginUseCase.java)
- [CustomUserDetailsService.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/security/CustomUserDetailsService.java)
- [JwtTokenProvider.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/security/jwt/JwtTokenProvider.java)
- [LoginAuditLogRepository.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/persistence/LoginAuditLogRepository.java)

### Credential Validation and Password Encoding Verification
- CustomUserDetailsService loads user details by email.
- Password verification is performed during authentication checks.
- The system relies on Spring Security's encoded password comparison.

```mermaid
sequenceDiagram
participant UC as "ContractorLoginUseCase"
participant UDS as "CustomUserDetailsService"
participant User as "User Entity"
UC->>UDS : "loadUserByUsername(email)"
UDS-->>UC : "UserDetails with encoded password"
UC->>UC : "Compare provided password with encoded password"
alt "Match"
UC-->>UC : "Proceed to token generation"
else "Mismatch"
UC-->>UC : "Fail authentication"
end
```

**Diagram sources**
- [ContractorLoginUseCase.java](file://src/main/java/root/cyb/mh/skylink_media_service/application/usecases/ContractorLoginUseCase.java)
- [CustomUserDetailsService.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/security/CustomUserDetailsService.java)
- [User.java](file://src/main/java/root/cyb/mh/skylink_media_service/domain/entities/User.java)

**Section sources**
- [ContractorLoginUseCase.java](file://src/main/java/root/cyb/mh/skylink_media_service/application/usecases/ContractorLoginUseCase.java)
- [CustomUserDetailsService.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/security/CustomUserDetailsService.java)
- [User.java](file://src/main/java/root/cyb/mh/skylink_media_service/domain/entities/User.java)

### Token Generation and Management
- JwtTokenProvider generates tokens for authenticated users.
- Tokens are returned in LoginResponse for client consumption.
- SecurityConfig configures JWT filter chain and session management.

```mermaid
classDiagram
class JwtTokenProvider {
+generateToken(userDetails) String
+validateToken(token) boolean
+getUsernameFromToken(token) String
}
class SecurityConfig {
+configure(http) void
}
SecurityConfig --> JwtTokenProvider : "uses for filter chain"
```

**Diagram sources**
- [JwtTokenProvider.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/security/jwt/JwtTokenProvider.java)
- [SecurityConfig.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/config/SecurityConfig.java)

**Section sources**
- [JwtTokenProvider.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/security/jwt/JwtTokenProvider.java)
- [SecurityConfig.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/config/SecurityConfig.java)

### Session Management, Remember-Me, and Concurrent Sessions
- SecurityConfig defines session fixation protection and maximum sessions per user.
- Remember-me functionality can be configured for persistent login.
- Logout handler clears session and performs additional cleanup.

```mermaid
flowchart TD
A["Login Success"] --> B["Create Session"]
B --> C{"Concurrent sessions allowed?"}
C --> |Yes| D["Allow new session"]
C --> |No| E["Limit to one active session"]
D --> F["Remember-me optional"]
E --> F
F --> G["Logout"]
G --> H["Invalidate session and tokens"]
```

**Diagram sources**
- [SecurityConfig.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/config/SecurityConfig.java)
- [CustomLogoutHandler.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/security/CustomLogoutHandler.java)

**Section sources**
- [SecurityConfig.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/config/SecurityConfig.java)
- [CustomLogoutHandler.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/security/CustomLogoutHandler.java)

### Brute Force Protection, Account Lockout, and Failed Attempt Tracking
- LoginAuditLogRepository persists login attempts with outcomes.
- Failed attempts can trigger temporary locks or rate limits.
- GlobalApiExceptionHandler centralizes error responses for authentication failures.

```mermaid
flowchart TD
Start(["Login Attempt"]) --> Validate["Validate credentials"]
Validate --> Pass{"Pass?"}
Pass --> |Yes| Success["Record success"]
Pass --> |No| Record["Record failure"]
Record --> Count["Increment failure count"]
Count --> Threshold{"Exceed threshold?"}
Threshold --> |Yes| Lock["Lock account temporarily"]
Threshold --> |No| Continue["Allow retry"]
Success --> End(["Complete"])
Lock --> End
Continue --> End
```

**Diagram sources**
- [LoginAuditLog.java](file://src/main/java/root/cyb/mh/skylink_media_service/domain/entities/LoginAuditLog.java)
- [LoginAuditLogRepository.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/persistence/LoginAuditLogRepository.java)
- [GlobalApiExceptionHandler.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/web/api/exception/GlobalApiExceptionHandler.java)

**Section sources**
- [LoginAuditLog.java](file://src/main/java/root/cyb/mh/skylink_media_service/domain/entities/LoginAuditLog.java)
- [LoginAuditLogRepository.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/persistence/LoginAuditLogRepository.java)
- [GlobalApiExceptionHandler.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/web/api/exception/GlobalApiExceptionHandler.java)

### Role-Specific Authentication Paths
- User: Base authenticated user.
- Contractor: Specialized contractor entity with contractor-specific repositories and use cases.
- Admin/SuperAdmin: Higher privilege roles with administrative endpoints.

```mermaid
classDiagram
class User
class Contractor
class Admin
class SuperAdmin
User <|-- Contractor
User <|-- Admin
User <|-- SuperAdmin
```

**Diagram sources**
- [User.java](file://src/main/java/root/cyb/mh/skylink_media_service/domain/entities/User.java)
- [Contractor.java](file://src/main/java/root/cyb/mh/skylink_media_service/domain/entities/Contractor.java)
- [Admin.java](file://src/main/java/root/cyb/mh/skylink_media_service/domain/entities/Admin.java)
- [SuperAdmin.java](file://src/main/java/root/cyb/mh/skylink_media_service/domain/entities/SuperAdmin.java)

**Section sources**
- [User.java](file://src/main/java/root/cyb/mh/skylink_media_service/domain/entities/User.java)
- [Contractor.java](file://src/main/java/root/cyb/mh/skylink_media_service/domain/entities/Contractor.java)
- [Admin.java](file://src/main/java/root/cyb/mh/skylink_media_service/domain/entities/Admin.java)
- [SuperAdmin.java](file://src/main/java/root/cyb/mh/skylink_media_service/domain/entities/SuperAdmin.java)

### Logout Procedures
- CustomLogoutHandler invalidates sessions and performs cleanup.
- SecurityConfig configures logout endpoints and behavior.
- Clients should clear stored tokens upon logout.

```mermaid
sequenceDiagram
participant Client as "Client"
participant API as "AuthApiController"
participant CFG as "SecurityConfig"
participant LH as "CustomLogoutHandler"
Client->>API : "POST /api/auth/logout"
API->>CFG : "Trigger logout"
CFG->>LH : "Invoke logout handler"
LH-->>CFG : "Cleanup complete"
CFG-->>API : "Logout success"
API-->>Client : "200 OK"
```

**Diagram sources**
- [AuthApiController.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/web/api/AuthApiController.java)
- [SecurityConfig.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/config/SecurityConfig.java)
- [CustomLogoutHandler.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/security/CustomLogoutHandler.java)

**Section sources**
- [AuthApiController.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/web/api/AuthApiController.java)
- [SecurityConfig.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/config/SecurityConfig.java)
- [CustomLogoutHandler.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/security/CustomLogoutHandler.java)

## Dependency Analysis
Authentication depends on layered components with clear separation of concerns.

```mermaid
graph LR
API["AuthApiController"] --> UC["ContractorLoginUseCase"]
UC --> UDS["CustomUserDetailsService"]
UC --> JWT["JwtTokenProvider"]
UC --> AUDIT["LoginAuditLogRepository"]
UDS --> URepo["UserRepository"]
UDS --> CRepo["ContractorRepository"]
CFG["SecurityConfig"] --> JWT
CFG --> LH["CustomLogoutHandler"]
```

**Diagram sources**
- [AuthApiController.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/web/api/AuthApiController.java)
- [ContractorLoginUseCase.java](file://src/main/java/root/cyb/mh/skylink_media_service/application/usecases/ContractorLoginUseCase.java)
- [CustomUserDetailsService.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/security/CustomUserDetailsService.java)
- [JwtTokenProvider.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/security/jwt/JwtTokenProvider.java)
- [LoginAuditLogRepository.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/persistence/LoginAuditLogRepository.java)
- [UserRepository.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/persistence/UserRepository.java)
- [ContractorRepository.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/persistence/ContractorRepository.java)
- [SecurityConfig.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/config/SecurityConfig.java)
- [CustomLogoutHandler.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/security/CustomLogoutHandler.java)

**Section sources**
- [AuthApiController.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/web/api/AuthApiController.java)
- [ContractorLoginUseCase.java](file://src/main/java/root/cyb/mh/skylink_media_service/application/usecases/ContractorLoginUseCase.java)
- [CustomUserDetailsService.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/security/CustomUserDetailsService.java)
- [JwtTokenProvider.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/security/jwt/JwtTokenProvider.java)
- [LoginAuditLogRepository.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/persistence/LoginAuditLogRepository.java)
- [UserRepository.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/persistence/UserRepository.java)
- [ContractorRepository.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/persistence/ContractorRepository.java)
- [SecurityConfig.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/config/SecurityConfig.java)
- [CustomLogoutHandler.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/security/CustomLogoutHandler.java)

## Performance Considerations
- Prefer efficient password encoding algorithms and avoid unnecessary database queries.
- Cache frequently accessed user roles and permissions where appropriate.
- Limit concurrent sessions to reduce resource contention.
- Use asynchronous audit logging to minimize latency.

## Troubleshooting Guide
Common issues and resolutions:
- 401 Unauthorized on login: Verify email/password correctness and ensure the account is unlocked.
- Rate limit exceeded: Wait for the cooldown period; failed attempts are tracked to prevent brute force.
- Session conflicts: Configure maximum sessions per user to enforce single active session.
- Logout not effective: Ensure clients invalidate stored tokens and cookies; server-side session invalidation occurs via CustomLogoutHandler.

**Section sources**
- [GlobalApiExceptionHandler.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/web/api/exception/GlobalApiExceptionHandler.java)
- [LoginAuditLogRepository.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/persistence/LoginAuditLogRepository.java)
- [SecurityConfig.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/config/SecurityConfig.java)
- [CustomLogoutHandler.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/security/CustomLogoutHandler.java)

## Conclusion
The authentication system integrates MVC and REST login paths, robust credential validation, role-aware use cases, secure token generation, and comprehensive session and audit controls. By leveraging JwtTokenProvider, CustomUserDetailsService, and SecurityConfig, the system ensures secure, scalable, and maintainable authentication across contractor, admin, and super admin roles.