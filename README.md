# Skylink Media Service Backend

A Spring Boot application for project management with photo attachments, supporting admin and contractor roles.

## Features

### Admin Features
- Login with username/password
- Register new admin users
- Create projects with work order number, location, and client code
- Create contractor accounts
- Assign contractors to projects
- View all photos uploaded by contractors for each project

### Contractor Features
- Login with username/password
- View assigned projects
- Upload multiple photos to assigned projects

### Storage Optimization Features
- **Automatic WebP Conversion**: All uploaded images are converted to WebP format with 75% quality
- **Thumbnail Generation**: 200x200px thumbnails created for project list views
- **Storage Cleanup**: Raw images automatically deleted after 24 hours to save space
- **Optimized File Serving**: WebP images served with proper content types

## Technology Stack

- **Backend**: Spring Boot 4.0.3, Java 21
- **Database**: PostgreSQL
- **Security**: Spring Security with role-based access
- **Frontend**: Thymeleaf templates with Tailwind CSS
- **Build Tool**: Gradle

## Setup Instructions

### Prerequisites
- Java 21
- PostgreSQL 12+
- Gradle (or use included wrapper)
- WebP tools (cwebp binary)

### WebP Tools Installation
Run the installation script:
```bash
./install-webp.sh
```

Or install manually:
- **Ubuntu/Debian**: `sudo apt-get install webp`
- **CentOS/RHEL**: `sudo yum install libwebp-tools`
- **macOS**: `brew install webp`

### Database Setup
1. Create PostgreSQL database:
   ```sql
   CREATE DATABASE skylink_media_service;
   ```

2. Run the database schema script:
   ```bash
   psql -U your_username -d skylink_media_service -f database-schema.sql
   ```

3. Apply the photo optimization migration:
   ```bash
   psql -U your_username -d skylink_media_service -f photo-optimization-migration.sql
   ```

4. Update `src/main/resources/application.properties` with your database credentials:
   ```properties
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```

### Application Setup
1. Clone and navigate to the project directory
2. Make gradlew executable:
   ```bash
   chmod +x gradlew
   ```
3. Run tests:
   ```bash
   ./gradlew test
   ```
4. Start the application:
   ```bash
   ./gradlew bootRun
   ```

### Default Login
- **Admin**: username=`admin`, password=`admin123`

## Usage

1. **Access the application**: http://localhost:8080
2. **Login** with admin credentials
3. **Create contractors** and **projects**
4. **Assign contractors** to projects
5. **Contractors can login** and upload photos to their assigned projects
6. **Admins can view** all uploaded photos by project

## Project Structure

```
src/main/java/root/cyb/mh/skylink_media_service/
├── domain/entities/          # Domain entities (User, Admin, Contractor, Project, Photo)
├── application/services/     # Business logic services
├── infrastructure/
│   ├── persistence/         # JPA repositories
│   ├── security/           # Security configuration
│   ├── web/               # Controllers
│   └── config/            # Configuration classes
└── resources/
    ├── templates/         # Thymeleaf templates
    └── application.properties
```

## API Endpoints

### Authentication
- `GET /login` - Login page
- `POST /login` - Login processing
- `POST /logout` - Logout

### Admin Endpoints
- `GET /admin/dashboard` - Admin dashboard
- `GET /admin/register-admin` - Register admin form
- `POST /admin/register-admin` - Create new admin
- `GET /admin/create-project` - Create project form
- `POST /admin/create-project` - Create new project
- `GET /admin/create-contractor` - Create contractor form
- `POST /admin/create-contractor` - Create new contractor
- `POST /admin/assign-contractor` - Assign contractor to project
- `GET /admin/project/{id}/photos` - View project photos

### Contractor Endpoints
- `GET /contractor/dashboard` - Contractor dashboard
- `GET /contractor/upload-photo/{projectId}` - Upload photo form
- `POST /contractor/upload-photo/{projectId}` - Upload photos

## File Storage

Photos are stored in the `uploads/` directory by default. Configure the path in `application.properties`:
```properties
app.upload.dir=uploads
```

## Security

- Role-based access control (ADMIN, CONTRACTOR)
- Password encryption using BCrypt
- CSRF protection
- Session-based authentication
- File upload validation

## Testing

Run tests with:
```bash
./gradlew test
```

Tests use H2 in-memory database for isolation.
