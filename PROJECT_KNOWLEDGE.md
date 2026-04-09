# Skylink Media Service Backend - Project Knowledge

## Project Overview
A Spring Boot application for project management with photo attachments, supporting admin and contractor roles. Built for Skylink Ltd to manage work orders and contractor assignments.

## Technology Stack
- **Backend**: Spring Boot 4.0.3, Java 21
- **Database**: PostgreSQL
- **Security**: Spring Security with role-based access (ADMIN, CONTRACTOR)
- **Frontend**: Thymeleaf templates with Tailwind CSS
- **Build Tool**: Gradle
- **API Documentation**: Swagger/OpenAPI 3.0
- **Authentication**: Session-based (Web) + JWT (API)

## Project Structure

### Domain Layer (`domain/`)
#### Entities (`domain/entities/`)
- **User** - Abstract base entity with username, password, createdAt, avatarPath
- **Admin** - Extends User, adds email field
- **Contractor** - Extends User, adds fullName, email, assignments
- **Project** - Work order with extensive fields (WO number, location, client code, status, payment status, due date, invoice price, etc.)
- **Photo** - Image files with WebP optimization, thumbnails, metadata, categories
- **ProjectAssignment** - Links contractors to projects
- **ProjectMessage** - Chat messages between admins and contractors
- **ProjectAuditLog** - Audit trail for all project changes
- **ProjectViewLog** - Tracks contractor project views
- **AdminChatReadLog** - Tracks admin chat read status

#### Value Objects (`domain/valueobjects/`)
- **ProjectStatus** - Enum: UNASSIGNED → ASSIGNED → UNREAD → INFIELD → READY_TO_OFFICE → CLOSED
- **PaymentStatus** - Enum: UNPAID → PARTIAL → PAID
- **ImageCategory** - Enum: BEFORE, DURING, AFTER, UNCATEGORIZED

#### Domain Services (`domain/services/`)
- **ProjectStatusTransitionService** - Handles status transitions with event publishing

#### Events (`domain/events/`)
- **ProjectCompletedEvent** - Published when project marked complete
- **ProjectOpenedEvent** - Published when contractor opens project

#### Exceptions (`domain/exceptions/`)
- **InvalidStatusTransitionException** - Thrown for invalid status changes

### Application Layer (`application/`)

#### DTOs (`application/dto/`)
- **ProjectDTO** - Data transfer object for projects
- **ProjectMapper** - MapStruct mapper (entity ↔ DTO)
- **ProjectSearchCriteria** - Advanced search filters
- **api/ErrorResponse** - Standard API error format
- **api/LoginRequest** - Login request validation
- **api/LoginResponse** - Login response with JWT

#### Services (`application/services/`)
- **ProjectService** - Core project CRUD, assignment, search logic
- **UserService** - User/contractor management
- **PhotoService** - Photo upload with metadata extraction
- **AuditLogService** - Centralized audit logging
- **ChatService** - Message sending/retrieval
- **EmailService** - Async email notifications
- **ProjectExportService** - CSV export functionality
- **ProjectSpecifications** - JPA Specifications for advanced search

#### Use Cases (`application/usecases/`)
- **ChangeProjectStatusUseCase** - Admin status changes with validation
- **CompleteProjectUseCase** - Contractor project completion
- **ContractorLoginUseCase** - JWT-based contractor authentication
- **GetContractorProjectsUseCase** - Contractor project list with actions
- **OpenProjectUseCase** - Contractor project opening with status transition

### Infrastructure Layer (`infrastructure/`)

#### Persistence (`infrastructure/persistence/`)
All JPA repositories extending Spring Data JPA:
- UserRepository, AdminRepository, ContractorRepository
- ProjectRepository (with search method)
- PhotoRepository
- ProjectAssignmentRepository (with active assignment queries)
- ProjectMessageRepository
- ProjectAuditLogRepository
- ProjectViewLogRepository
- AdminChatReadLogRepository
- DatabaseMigrationRunner - Schema patches

#### Security (`infrastructure/security/`)
- **SecurityConfig** - Spring Security configuration
- **CustomUserDetailsService** - UserDetailsService implementation
- **jwt/JwtTokenProvider** - JWT generation/validation
- **jwt/JwtAuthenticationFilter** - JWT filter for API requests
- **jwt/JwtAuthenticationEntryPoint** - Unauthorized handler

#### Storage (`infrastructure/storage/`)
- **FileStorageService** - File upload with WebP conversion
- **SystemCommandExecutor** - cwebp command execution
- **ThumbnailGenerator** - 200x200 thumbnail generation
- **StorageCleanupService** - Scheduled cleanup of old raw images

#### Web Controllers (`infrastructure/web/`)
- **AuthController** - Login page, dashboard routing
- **AdminController** - Admin dashboard, project/contractor management, chat
- **ContractorController** - Contractor dashboard, photo upload, project actions
- **FileController** - File serving (uploads, thumbnails, avatars)
- **ProfileController** - Profile management for both roles
- **api/AuthApiController** - REST API login endpoint
- **api/exception/GlobalApiExceptionHandler** - Global exception handling

#### Configuration (`infrastructure/config/`)
- **DataInitializer** - Creates default admin on startup
- **DevModeConfig** - Development mode configuration
- **OpenApiConfig** - Swagger/OpenAPI configuration

### Resources (`resources/`)

#### Templates (`templates/`)
- **layout.html** - Base layout with Tailwind CSS
- **login.html** - Login page with PWA support
- **admin/** - Admin templates (dashboard, create-project, create-contractor, project-photos, project-chat, project-history, edit-project, profile, register-admin)
- **contractor/** - Contractor templates (dashboard, project-photos, project-chat, upload-photo, profile)

#### Static (`static/`)
- **manifest.json** - PWA manifest
- **service-worker.js** - PWA service worker
- **icons/** - PWA icons
- **js/pwa-register.js** - PWA registration script

## Key Business Rules

### Project Status Workflow
```
UNASSIGNED → ASSIGNED → UNREAD → INFIELD → READY_TO_OFFICE → CLOSED
```
- Status transitions are automatic based on contractor actions
- Admins can only manually transition: READY_TO_OFFICE → INFIELD (rework) or CLOSED

### Contractor Assignment Rules
1. One contractor can have maximum 4 active (non-CLOSED) projects
2. One project can only be assigned to one contractor at a time
3. CLOSED projects can be reassigned for record-keeping

### Payment Status
- UNPAID → PARTIAL → PAID (one-way progression)

### Photo Upload
- All images converted to WebP format (75% quality)
- 200x200 thumbnails generated
- Original files kept for 24 hours then cleaned up
- EXIF metadata extracted and stored as JSON
- Images can be categorized: BEFORE, DURING, AFTER

## API Endpoints

### Public
- `GET /login` - Login page
- `POST /login` - Form login
- `POST /logout` - Logout

### Admin (requires ROLE_ADMIN)
- `GET /admin/dashboard` - Dashboard with search/filters
- `GET /admin/projects/export` - CSV export
- `GET/POST /admin/create-project` - Create project
- `GET/POST /admin/edit-project/{id}` - Edit project
- `GET/POST /admin/create-contractor` - Create contractor
- `GET/POST /admin/register-admin` - Register admin
- `POST /admin/assign-contractor` - Assign contractor
- `POST /admin/unassign-contractor` - Unassign contractor
- `POST /admin/delete-project/{id}` - Delete project (dev mode only)
- `POST /admin/change-status/{id}` - Change project status
- `POST /admin/change-payment-status/{id}` - Change payment status
- `GET /admin/project/{id}/photos` - View project photos
- `POST /admin/project/{id}/photos/download` - Download photos as ZIP
- `GET /admin/project/{id}/chat` - Chat page
- `POST /admin/project/{id}/chat/send` - Send message
- `GET /admin/project/{id}/history` - View audit history
- `GET/POST /admin/profile` - Profile management

### Contractor (requires ROLE_CONTRACTOR)
- `GET /contractor/dashboard` - Dashboard with assigned projects
- `GET /contractor/project/{id}/photos` - View project photos
- `GET/POST /contractor/upload-photo/{id}` - Upload photos
- `POST /contractor/project/{id}/open` - Open project (status transition)
- `POST /contractor/project/{id}/complete` - Mark project complete
- `GET /contractor/project/{id}/chat` - Chat page
- `POST /contractor/project/{id}/chat/send` - Send message
- `GET/POST /contractor/profile` - Profile management

### REST API (`/api/v1/`)
- `POST /api/v1/auth/login` - Contractor JWT login

### File Serving
- `GET /uploads/{filename}` - Serve uploaded files
- `GET /thumbnails/{filename}` - Serve thumbnails
- `GET /uploads/avatars/{filename}` - Serve avatars

## Configuration (application.properties)

### Database
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/skylink_media_service
spring.datasource.username=mhcybroot
spring.datasource.password=MhR@2025
spring.jpa.hibernate.ddl-auto=update
```

### File Upload
```properties
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=50MB
app.upload.dir=uploads
```

### JWT
```properties
jwt.secret=5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437
jwt.expiration=86400000 (24 hours)
```

### Development
```properties
app.dev=false (set to true for dev features like project deletion)
```

### Mail
```properties
spring.mail.host=skylink-ltd.com
spring.mail.port=465
spring.mail.username=support@skylink-ltd.com
```

### CORS
```properties
cors.allowed-origins=http://localhost:3000,http://localhost:8080
```

## Database Schema

### Core Tables
- **users** - Base user table with discriminator for Admin/Contractor
- **projects** - Work orders with full details
- **project_assignments** - Contractor-project links
- **photos** - Image metadata and paths
- **project_messages** - Chat messages
- **project_audit_log** - Audit trail
- **project_view_logs** - View tracking
- **admin_chat_read_log** - Admin read receipts

### Indexes
- All foreign keys indexed
- Search fields indexed (username, work_order_number, etc.)
- Audit log indexed by project_id, timestamp, admin_id

## Testing
- JUnit 5 with Mockito
- Spring Boot Test with @ActiveProfiles("test")
- H2 in-memory database for tests
- Test files:
  - ContractorLoginUseCaseTest
  - JwtTokenProviderTest

## Security Features
- BCrypt password encoding
- CSRF protection (disabled for /api/v1/**)
- Role-based access control
- Session-based auth for web
- JWT for API
- CORS configured for localhost:3000, 8080

## PWA Features
- Web App Manifest
- Service Worker
- Install prompt
- Mobile-optimized UI
- Touch-friendly inputs (44px minimum)

## Email Notifications
- Chat notifications sent asynchronously
- HTML email templates
- Configurable SMTP settings

## Audit Logging
All actions logged:
- PROJECT_CREATED
- PROJECT_UPDATED
- CONTRACTOR_ASSIGNED/UNASSIGNED
- STATUS_CHANGED
- PAYMENT_STATUS_CHANGED
- PROJECT_DELETED

## Development Notes

### Adding New Fields to Project
1. Add field to Project entity
2. Add to ProjectService create/update methods
3. Add to AdminController form handling
4. Update create-project.html and edit-project.html templates
5. Update ProjectExportService CSV generation

### Adding New Status
1. Add to ProjectStatus enum
2. Update canTransitionTo() logic
3. Update badge classes for UI
4. Update any use cases that handle status

### Photo Optimization Flow
1. File uploaded via PhotoService
2. FileStorageService stores original
3. SystemCommandExecutor converts to WebP
4. ThumbnailGenerator creates thumbnail
5. EXIF metadata extracted via metadata-extractor
6. Photo entity saved with all paths
7. StorageCleanupService deletes originals after 24h

### Chat Flow
1. Message sent via ChatService
2. Email notification sent asynchronously
3. AdminChatReadLog tracks admin reads
4. ProjectViewLog tracks contractor reads
5. Unread counts calculated based on last read timestamps
