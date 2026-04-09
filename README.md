# Skylink Media Service

[![Java](https://img.shields.io/badge/Java-21-blue.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-12+-blue.svg)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-Proprietary-red.svg)]()

**A professional-grade media project management platform for photography studios, media agencies, and production companies.**

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Key Features](#-key-features)
- [Technology Stack](#-technology-stack)
- [System Architecture](#-system-architecture)
- [Getting Started](#-getting-started)
- [Database Design](#-database-design)
- [API Documentation](#-api-documentation)
- [Configuration](#-configuration)
- [Deployment](#-deployment)
- [Testing](#-testing)
- [Development Guidelines](#-development-guidelines)
- [Troubleshooting](#-troubleshooting)
- [Project Structure](#-project-structure)

---

## 🎯 Overview

Skylink Media Service is a comprehensive media production workflow management platform designed to streamline complex media workflows by centralizing contractor coordination, project tracking, and photo asset management into a unified system.

### Core Problem It Solves

The platform addresses three critical pain points in media production environments:

- **Contractor Coordination**: Managing multiple photographers, assistants, and freelancers across concurrent shoots
- **Project Tracking**: Real-time visibility into project status, deadlines, and deliverables
- **Photo Management**: Efficient ingestion, processing, and distribution of high-volume media assets

### Target Audience

- Professional photographers and photography studios
- Media agencies handling multiple client projects
- Production companies coordinating complex shoot logistics
- Freelance contractors requiring centralized project access

### Business Value

- **Operational Efficiency**: Reduce administrative overhead by up to 40% through automation
- **Quality Assurance**: Standardized workflows ensure consistent project delivery
- **Scalability**: Support for growing teams and increasing project volumes
- **Transparency**: Clear visibility for all stakeholders into project status and timelines

---

## ✨ Key Features

### 📊 Project Management
- Complete project lifecycle management from creation to completion
- Advanced search and filtering capabilities
- Status tracking with customizable workflows
- Payment status monitoring
- Due date tracking and deadline management
- Invoice price management
- CSV export functionality

### 👥 Contractor Management
- Automated contractor assignment based on availability
- Workload balancing (maximum 4 active projects per contractor)
- Performance tracking and analytics
- Profile management with avatar support
- Role-based access control

### 📸 Photo Management System
- Multi-format image ingestion with automatic WebP conversion
- Thumbnail generation (200x200px)
- Metadata extraction and categorization
- Bulk upload support (up to 50MB per request)
- Photo optimization with configurable quality settings
- Category-based organization

### 💬 Communication System
- Real-time project-specific chat channels
- WebSocket-based live messaging
- Email notifications for important updates
- Message history and search
- Unread message tracking

### 🛡️ Administration Features
- **Three-tier user hierarchy**: Super Admin, Admin, Contractor
- Comprehensive audit logging for all project activities
- User management and access control
- System monitoring and analytics
- Dashboard with real-time statistics
- Contractor blocking/unblocking capabilities

### 🔐 Security
- JWT-based authentication for API access
- Spring Security with role-based access control
- Form-based login for web interface
- CORS configuration for frontend integration
- Secure password hashing with BCrypt

### 📱 Progressive Web App (PWA)
- Offline-capable experiences
- Service worker caching strategies
- Mobile-optimized responsive design
- Install prompts for mobile devices
- Modern UI with Tailwind CSS

---

## 🛠 Technology Stack

### Backend
- **Framework**: Spring Boot 4.0.3
- **Language**: Java 21
- **Build Tool**: Gradle with Spring Dependency Management
- **Database**: PostgreSQL 12+
- **ORM**: Hibernate/JPA
- **Security**: Spring Security + JWT (jjwt 0.13.0)
- **API Documentation**: SpringDoc OpenAPI 3.0.1 (Swagger UI)
- **Image Processing**: WebP tools (cwebp)
- **Metadata Extraction**: metadata-extractor 2.19.0
- **Email**: Spring Mail with SMTP

### Frontend
- **Templating**: Thymeleaf with Spring Security integration
- **Styling**: Tailwind CSS (CDN)
- **Fonts**: Inter (data) and Outfit (headings)
- **PWA**: Service Worker + Web App Manifest
- **JavaScript**: Vanilla JS with Alpine.js for reactivity
- **WebSocket**: SockJS + STOMP for real-time communication

### Development & Testing
- **Testing**: JUnit 5, Spring Security Test, H2 Database
- **Code Quality**: Spring Boot Actuator
- **Logging**: SLF4J with structured output

---

## 🏗 System Architecture

### Clean Architecture Layers

Skylink Media Service follows Clean Architecture principles with clear separation of concerns:

```
┌─────────────────────────────────────────┐
│      Presentation Layer                 │
│  (Controllers, Thymeleaf Templates)     │
└────────────────┬────────────────────────┘
                 │
┌────────────────▼────────────────────────┐
│      Application Layer                  │
│  (Services, Use Cases, DTOs)            │
└────────────────┬────────────────────────┘
                 │
┌────────────────▼────────────────────────┐
│      Domain Layer                       │
│  (Entities, Value Objects, Events)      │
└────────────────┬────────────────────────┘
                 │
┌────────────────▼────────────────────────┐
│      Infrastructure Layer               │
│  (Repositories, Security, Storage)      │
└─────────────────────────────────────────┘
```

### Architecture Principles

- **High Cohesion**: Related functionality grouped within focused services
- **Low Coupling**: Clear interfaces between layers reduce dependency complexity
- **Single Responsibility**: Each component has a well-defined primary responsibility
- **Extensibility**: Modular design supports easy addition of new features

### Data Flow

1. **Client Request** → Controller (Presentation)
2. **Controller** → Service/Use Case (Application)
3. **Service** → Repository (Infrastructure)
4. **Repository** → Database
5. **Response** flows back through layers

---

## 🚀 Getting Started

### Prerequisites

Before installing Skylink Media Service, ensure your system meets these requirements:

- **Java 21**: Required for running the Spring Boot application
- **PostgreSQL 12+**: Database server for storing application data
- **Gradle**: Build tool (or use the included wrapper)
- **WebP Tools**: `cwebp` binary for image optimization
- **Git**: For cloning the repository (recommended)

### Step-by-Step Setup

#### 1. Install Prerequisites

**Java 21 Installation:**
```bash
# Download from Oracle JDK or OpenJDK official website
java -version  # Verify installation
```

**PostgreSQL Installation:**
```bash
# Download from PostgreSQL official website
psql --version  # Verify installation
```

**WebP Tools Installation:**

Using the installation script:
```bash
chmod +x install-webp.sh
./install-webp.sh
```

Manual installation:
```bash
# Ubuntu/Debian
sudo apt-get install webp

# CentOS/RHEL
sudo yum install libwebp-tools

# macOS
brew install webp

# Verify installation
cwebp -version
```

#### 2. Database Setup

```sql
-- Create the database
CREATE DATABASE skylink_media_service;
```

Apply the base schema:
```bash
psql -U your_username -d skylink_media_service -f database-schema.sql
```

Apply migrations:
```bash
psql -U your_username -d skylink_media_service -f photo-optimization-migration.sql
psql -U your_username -d skylink_media_service -f contractor-status-migration.sql
psql -U your_username -d skylink_media_service -f audit-log-action-types-migration.sql
```

#### 3. Application Setup

Clone the repository:
```bash
git clone <repository-url>
cd skylink-media-service-backend
```

Make Gradle wrapper executable:
```bash
# Linux/macOS
chmod +x gradlew

# Windows (PowerShell) - already executable
```

Configure database connection in `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/skylink_media_service
spring.datasource.username=your_username
spring.datasource.password=your_password
```

Run tests to verify setup:
```bash
./gradlew test
```

Start the application:
```bash
./gradlew bootRun
```

The application will be available at: **http://localhost:8085**

#### 4. First-Time Login

**Default Super Admin Credentials:**
- Username: `superadmin`
- Password: `superadmin123`

**Default Admin Credentials:**
- Username: `admin`
- Password: `admin123`

⚠️ **Important**: Change these default passwords immediately after first login!

---

## 🗄 Database Design

### Core Entities

#### Users (Single Table Inheritance)
Base entity with discriminator-based polymorphism:
- **Admin**: System administrators with project management capabilities
- **Super Admin**: Full system access including user management
- **Contractor**: External workers assigned to projects

Key fields: `id`, `username`, `password`, `user_type`, `full_name`, `avatar_path`, `is_blocked`, `created_at`

#### Projects
Central entity representing media production jobs:
- Work order tracking
- Client information
- Location and scheduling
- Status and payment tracking
- Financial data (invoice price)

Key fields: `id`, `work_order_number`, `location`, `client_code`, `description`, `status`, `payment_status`, `due_date`, `invoice_price`

#### Project Assignments
Many-to-many relationship between Projects and Contractors:
- Tracks which contractors are assigned to which projects
- Enforces workload limits

#### Photos
Media assets associated with projects:
- Original file storage
- WebP optimized versions
- Thumbnails (200x200px)
- Metadata and categorization

Key fields: `id`, `file_name`, `file_path`, `webp_path`, `thumbnail_path`, `category`, `file_size`, `is_optimized`

#### Project Audit Log
Comprehensive audit trail for all project activities:
- Action types: PROJECT_CREATED, PROJECT_UPDATED, PROJECT_VIEWED, CONTRACTOR_ASSIGNED, STATUS_CHANGED, etc.
- Tracks who did what, when, and what changed

### Database Indexes

Performance-optimized indexes on:
- User lookups: `username`, `user_type`
- Project searches: `work_order_number`, `status`
- Assignment queries: `project_id`, `contractor_id`
- Photo retrieval: `project_id`, `contractor_id`
- Audit logs: `project_id`, `timestamp`, `admin_id`

---

## 📡 API Documentation

### REST API Endpoints

The application provides comprehensive REST APIs documented via Swagger UI:

**Access Swagger UI**: http://localhost:8085/swagger-ui.html  
**API Documentation**: http://localhost:8085/v3/api-docs

### API Categories

#### Authentication APIs
- `POST /api/v1/auth/login` - JWT authentication
- Token-based access for all API endpoints

#### Project Management APIs
- CRUD operations for projects
- Advanced search and filtering
- Status management
- Contractor assignment
- CSV export

#### Photo Management APIs
- Photo upload with automatic optimization
- Bulk download (ZIP)
- Photo categorization
- Thumbnail retrieval

#### Contractor APIs
- Dashboard data retrieval
- Project assignment management
- Photo upload for assigned projects
- Chat functionality

#### Admin & Super Admin APIs
- User management
- System analytics
- Audit log access
- Contractor management

### Authentication

All API endpoints under `/api/v1/**` require JWT authentication:

```http
Authorization: Bearer <your-jwt-token>
```

---

## ⚙️ Configuration

### Application Properties

Key configuration categories in `application.properties`:

#### Database Connection
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/skylink_media_service
spring.datasource.username=postgres
spring.datasource.password=root
spring.jpa.hibernate.ddl-auto=update
```

#### File Upload Settings
```properties
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=50MB
app.upload.dir=uploads
```

#### JWT Configuration
```properties
jwt.secret=5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437
jwt.expiration=86400000  # 24 hours
jwt.header=Authorization
jwt.prefix=Bearer
```

#### Mail Configuration
```properties
spring.mail.host=skylink-ltd.com
spring.mail.port=465
spring.mail.username=support@skylink-ltd.com
spring.mail.password=<your-password>
app.mail.from=support@skylink-ltd.com
app.mail.from-name=Skylink Hub
```

#### Development Mode
```properties
app.dev=false  # Set to false in production
spring.thymeleaf.cache=false  # Disable for development
```

### Environment Variables

Override sensitive configuration using environment variables:
```bash
export SPRING_DATASOURCE_PASSWORD=your_secure_password
export JWT_SECRET=your_secure_secret
export SPRING_MAIL_PASSWORD=your_mail_password
```

---

## 🚢 Deployment

### Production Checklist

- [ ] Set `app.dev=false`
- [ ] Configure strong JWT secret
- [ ] Update database credentials
- [ ] Enable Thymeleaf caching: `spring.thymeleaf.cache=true`
- [ ] Configure HTTPS/SSL certificates
- [ ] Set up database backups
- [ ] Configure proper CORS origins
- [ ] Enable production logging levels
- [ ] Set up monitoring and alerting
- [ ] Configure reverse proxy (nginx/Apache)

### Build for Production

```bash
# Clean and build
./gradlew clean build

# Run JAR file
java -jar build/libs/skylink-media-service-backend-0.0.1-SNAPSHOT.jar
```

### Database Migrations

Always apply migrations in order:
1. `database-schema.sql` (base schema)
2. `photo-optimization-migration.sql`
3. `contractor-status-migration.sql`
4. `status-management-migration.sql`
5. `invoice-price-migration.sql`
6. `audit-log-action-types-migration.sql`

### Backup Strategy

**Database Backup:**
```bash
pg_dump -U postgres skylink_media_service > backup_$(date +%Y%m%d).sql
```

**File Storage Backup:**
```bash
tar -czf uploads_backup_$(date +%Y%m%d).tar.gz uploads/
```

---

## 🧪 Testing

### Run Tests

```bash
# Run all tests
./gradlew test

# Run with coverage
./gradlew test jacocoTestReport

# Run specific test class
./gradlew test --tests "root.cyb.mh.skylink_media_service.*"
```

### Testing Strategy

- **Unit Tests**: Service layer and use case testing
- **Integration Tests**: Repository and controller testing
- **Security Tests**: Authentication and authorization validation
- **Test Database**: H2 in-memory database for isolated testing

---

## 📝 Development Guidelines

### Code Structure

```
src/main/java/root/cyb/mh/skylink_media_service/
├── application/
│   ├── dto/              # Data Transfer Objects
│   ├── services/         # Business services
│   └── usecases/         # Application use cases
├── domain/
│   ├── entities/         # JPA entities
│   ├── exceptions/       # Domain exceptions
│   └── valueobjects/     # Value objects (enums, etc.)
├── infrastructure/
│   ├── config/           # Configuration classes
│   ├── persistence/      # Repository interfaces
│   ├── security/         # Security configuration
│   ├── storage/          # File storage services
│   └── web/              # Controllers
└── SkylinkMediaServiceApplication.java
```

### Coding Standards

1. **Follow Clean Architecture**: Maintain layer boundaries
2. **Use Meaningful Names**: Descriptive class and method names
3. **Document APIs**: Use OpenAPI annotations for REST endpoints
4. **Handle Exceptions**: Use global exception handling
5. **Log Appropriately**: Use SLF4J with proper log levels
6. **Write Tests**: Maintain test coverage for new features

### Git Workflow

```bash
# Create feature branch
git checkout -b feature/your-feature-name

# Commit changes
git add .
git commit -m "feat: add your feature description"

# Push and create PR
git push origin feature/your-feature-name
```

---

## 🔧 Troubleshooting

### Common Issues

#### Authentication Problems
- Verify JWT token configuration in `application.properties`
- Check user credentials against database records
- Review security filter chain configuration
- Ensure `Authorization: Bearer <token>` header format

#### File Upload Failures
- Confirm file storage directory permissions: `uploads/`
- Validate supported file formats and sizes (max 10MB per file)
- Check disk space availability
- Verify `cwebp` is installed and in PATH

#### Database Connectivity
- Validate JDBC URL format: `jdbc:postgresql://localhost:5432/skylink_media_service`
- Check PostgreSQL server is running
- Review connection credentials in `application.properties`
- Verify database exists and user has permissions

#### WebP Conversion Issues
- Install WebP tools: `./install-webp.sh`
- Verify installation: `cwebp -version`
- Check system PATH includes cwebp binary
- Review `SystemCommandExecutor` logs for errors

#### Real-time Communication Issues
- Verify WebSocket endpoint accessibility: `/ws`
- Check server port configuration (default: 8085)
- Review browser compatibility for SockJS
- Check CORS configuration for frontend origin

### Enable Debug Logging

```properties
# application.properties
logging.level.root.cyb.mh.skylink_media_service=DEBUG
logging.level.org.springframework.security=DEBUG
```

### Check Application Health

```bash
# Test API endpoint
curl http://localhost:8085/api/v1/health

# Check logs
tail -f app.log
```

---

## 📁 Project Structure

```
skylink-media-service-backend/
├── .qoder/
│   └── repowiki/              # Project documentation
├── gradle/
│   └── wrapper/               # Gradle wrapper
├── src/
│   ├── main/
│   │   ├── java/              # Java source code
│   │   └── resources/         # Configuration & templates
│   │       ├── application.properties
│   │       ├── templates/     # Thymeleaf templates
│   │       └── static/        # Static assets (PWA)
│   └── test/                  # Test source code
├── uploads/                   # File storage directory
├── build.gradle               # Build configuration
├── database-schema.sql        # Database schema
├── *-migration.sql            # Database migrations
├── install-webp.sh            # WebP installation script
└── README.md                  # This file
```

---

## 📊 Performance Considerations

### Optimization Strategies

- **Image Optimization**: WebP conversion reduces bandwidth by ~30%
- **Lazy Loading**: Efficient database queries with proper indexes
- **Caching**: Service worker caching for static assets
- **Pagination**: Large dataset handling with Spring Data pagination
- **Connection Pooling**: HikariCP for database connections

### Scalability

- **Horizontal Scaling**: Stateless service design enables load balancing
- **Asynchronous Processing**: Background tasks for image processing
- **Database Optimization**: Indexed queries and connection pooling
- **WebSocket**: In-memory broker for single-instance (consider Redis for clustering)

---

## 🤝 Contributing

This is a proprietary application. For internal development:

1. Create feature branch from `main`
2. Follow coding standards and architecture principles
3. Write comprehensive tests
4. Update documentation for new features
5. Submit pull request for review

---

## 📞 Support

For technical support or questions:
- **Email**: support@skylink-ltd.com
- **Documentation**: See `.qoder/repowiki/en/content/` for detailed guides
- **API Docs**: http://localhost:8085/swagger-ui.html

---

## 📄 License

Proprietary - All rights reserved. This software is the confidential property of Skylink Media Service.

---

**Built with ❤️ using Spring Boot & Java 21**

*Last Updated: April 2026*
