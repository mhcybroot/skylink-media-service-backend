# Getting Started

<cite>
**Referenced Files in This Document**
- [README.md](file://README.md)
- [database-schema.sql](file://database-schema.sql)
- [photo-optimization-migration.sql](file://photo-optimization-migration.sql)
- [application.properties](file://src/main/resources/application.properties)
- [install-webp.sh](file://install-webp.sh)
- [SystemCommandExecutor.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/storage/SystemCommandExecutor.java)
- [ThumbnailGenerator.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/storage/ThumbnailGenerator.java)
- [SecurityConfig.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/security/SecurityConfig.java)
- [JwtTokenProvider.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/security/jwt/JwtTokenProvider.java)
- [DataInitializer.java](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/config/DataInitializer.java)
- [build.gradle](file://build.gradle)
- [settings.gradle](file://settings.gradle)
- [application-test.properties](file://src/test/resources/application-test.properties)
</cite>

## Table of Contents
1. [Introduction](#introduction)
2. [Prerequisites](#prerequisites)
3. [Step-by-Step Setup](#step-by-step-setup)
4. [Environment Configuration](#environment-configuration)
5. [First-Time Setup and Initial User Creation](#first-time-setup-and-initial-user-creation)
6. [Basic System Verification](#basic-system-verification)
7. [Troubleshooting Guide](#troubleshooting-guide)
8. [Conclusion](#conclusion)

## Introduction
Skylink Media Service is a Spring Boot application designed for project management with photo attachment capabilities. It supports administrator and contractor roles, featuring automatic image optimization to WebP format, thumbnail generation, and secure file storage management.

## Prerequisites
Before installing Skylink Media Service, ensure your system meets these requirements:

- **Java 21**: Required for running the Spring Boot application
- **PostgreSQL 12+**: Database server for storing application data
- **Gradle**: Build tool (or use the included wrapper)
- **WebP Tools**: cwebp binary for image optimization
- **Git**: For cloning the repository (recommended)

**Section sources**
- [README.md:36-41](file://README.md#L36-L41)
- [build.gradle:11-15](file://build.gradle#L11-L15)

## Step-by-Step Setup

### 1. Install Prerequisites
Install the required software components:

**Java 21 Installation:**
- Download from Oracle JDK or OpenJDK official website
- Verify installation: `java -version`

**PostgreSQL Installation:**
- Download from PostgreSQL official website
- Create a database user with appropriate permissions
- Verify installation: `psql --version`

**WebP Tools Installation:**
Choose one of the following methods:

**Using the installation script:**
```bash
chmod +x install-webp.sh
./install-webp.sh
```

**Manual installation:**
- **Ubuntu/Debian**: `sudo apt-get install webp`
- **CentOS/RHEL**: `sudo yum install libwebp-tools`
- **macOS**: `brew install webp`

**Verify WebP installation:**
```bash
cwebp -version
```

**Section sources**
- [README.md:42-52](file://README.md#L42-L52)
- [install-webp.sh:1-40](file://install-webp.sh#L1-L40)

### 2. Database Setup
Create and initialize the database schema:

**Create the database:**
```sql
CREATE DATABASE skylink_media_service;
```

**Apply the base schema:**
```bash
psql -U your_username -d skylink_media_service -f database-schema.sql
```

**Apply photo optimization migration:**
```bash
psql -U your_username -d skylink_media_service -f photo-optimization-migration.sql
```

**Verify database setup:**
- Connect to the database and verify tables exist
- Check that indexes were created successfully
- Confirm the default admin user was inserted

**Section sources**
- [README.md:53-67](file://README.md#L53-L67)
- [database-schema.sql:1-76](file://database-schema.sql#L1-L76)
- [photo-optimization-migration.sql:1-16](file://photo-optimization-migration.sql#L1-L16)

### 3. Application Setup
Clone and configure the application:

**Clone the repository:**
```bash
git clone <repository-url>
cd skylink-media-service-backend
```

**Make Gradle wrapper executable:**
```bash
chmod +x gradlew
```

**Run tests to verify setup:**
```bash
./gradlew test
```

**Start the application:**
```bash
./gradlew bootRun
```

**Section sources**
- [README.md:75-88](file://README.md#L75-L88)

## Environment Configuration

### Database Connection Settings
Configure your database connection in `application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/skylink_media_service
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### File Storage Configuration
Set up local file storage for uploaded images:

```properties
app.upload.dir=uploads
```

### JWT Security Configuration
Configure JWT settings for authentication:

```properties
jwt.secret=your-secret-key-here
jwt.expiration=86400000
jwt.header=Authorization
jwt.prefix=Bearer
```

### CORS Configuration
Configure cross-origin resource sharing:

```properties
cors.allowed-origins=http://localhost:3000,http://localhost:8080
cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS
cors.allowed-headers=*
cors.max-age=3600
```

### Development Mode
Enable development features during setup:

```properties
app.dev=true
```

**Section sources**
- [application.properties:1-58](file://src/main/resources/application.properties#L1-L58)
- [README.md:141-146](file://README.md#L141-L146)

## First-Time Setup and Initial User Creation

### Default Credentials
The system creates default administrative accounts:

**Primary Admin:**
- Username: `admin`
- Password: `admin123`

**Super Admin (automatically created):**
- Username: `superadmin`
- Password: `superadmin123`

### Initial System Setup Steps
1. **Access the application**: Navigate to `http://localhost:8085`
2. **Login with admin credentials**: Use the default admin account
3. **Create contractor accounts**: Register new contractor users
4. **Create projects**: Set up initial projects with work order numbers
5. **Assign contractors**: Link contractors to specific projects
6. **Upload test photos**: Have contractors upload sample photos to verify optimization

### Automatic User Creation
The application automatically creates default users during startup if they don't exist:

```java
// Default super-admin creation
SuperAdmin superAdmin = new SuperAdmin("superadmin", passwordEncoder.encode("superadmin123"));

// Default admin creation  
Admin admin = new Admin("admin", passwordEncoder.encode("admin123"));
```

**Section sources**
- [README.md:90-92](file://README.md#L90-L92)
- [DataInitializer.java:30-45](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/config/DataInitializer.java#L30-L45)

## Basic System Verification

### Verification Steps
Complete these checks to ensure proper installation:

**1. Database Connectivity:**
- Verify PostgreSQL is running
- Confirm database connection with configured credentials
- Check that all tables were created successfully

**2. WebP Optimization:**
- Test cwebp binary: `cwebp -version`
- Verify thumbnail generation works
- Check that WebP conversion is functioning

**3. Application Startup:**
- Confirm application starts without errors
- Verify port 8085 is accessible
- Check that all services are initialized

**4. File Storage:**
- Verify upload directory exists
- Test file upload functionality
- Confirm WebP optimization occurs

**5. Security Features:**
- Test JWT token generation
- Verify role-based access control
- Check CORS configuration

### Validation Commands
```bash
# Test WebP installation
cwebp -version

# Test database connectivity
psql -U your_username -d skylink_media_service -c "SELECT version();"

# Start application
./gradlew bootRun

# Access application
curl http://localhost:8085
```

**Section sources**
- [SystemCommandExecutor.java:11-30](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/storage/SystemCommandExecutor.java#L11-L30)
- [ThumbnailGenerator.java:17-40](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/storage/ThumbnailGenerator.java#L17-L40)

## Troubleshooting Guide

### Common Issues and Solutions

**1. Database Connection Problems**
- **Issue**: Unable to connect to PostgreSQL
- **Solution**: Verify database credentials and PostgreSQL service status
- **Check**: `psql -U username -d skylink_media_service`

**2. WebP Conversion Failures**
- **Issue**: Images not converting to WebP format
- **Solution**: Ensure cwebp binary is installed and accessible
- **Check**: `cwebp -version` should return version information

**3. Port Conflicts**
- **Issue**: Application fails to start on port 8085
- **Solution**: Change server.port in application.properties
- **Alternative**: Stop process using port 8085

**4. File Upload Issues**
- **Issue**: Uploads failing or not saving
- **Solution**: Verify app.upload.dir exists and has write permissions
- **Check**: Directory permissions and disk space availability

**5. JWT Token Problems**
- **Issue**: Authentication failures
- **Solution**: Verify JWT secret key is properly configured
- **Check**: JWT expiration settings and header configuration

**6. CORS Configuration Issues**
- **Issue**: Frontend requests blocked
- **Solution**: Update cors.allowed-origins with your frontend URLs
- **Check**: Verify allowed methods and headers

### Debug Information Collection
Enable debug logging for troubleshooting:

```properties
logging.level.root.cyb.mh.skylink_media_service.infrastructure.web.AdminController=DEBUG
```

**Section sources**
- [SecurityConfig.java:43-87](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/security/SecurityConfig.java#L43-L87)
- [JwtTokenProvider.java:39-48](file://src/main/java/root/cyb/mh/skylink_media_service/infrastructure/security/jwt/JwtTokenProvider.java#L39-L48)

## Conclusion
Skylink Media Service provides a comprehensive solution for project management with integrated photo optimization capabilities. The setup process involves configuring Java 21, PostgreSQL, and WebP tools, followed by database schema initialization and application configuration. The system includes automatic user creation, secure authentication with JWT, and automated image optimization to WebP format with thumbnail generation.

For ongoing maintenance, monitor the application logs, verify database backups, and ensure WebP tools remain updated. The modular architecture allows for easy extension and customization while maintaining security and performance standards.