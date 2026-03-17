# Skylink Contractor REST API Documentation

## Overview

This REST API provides contractor-specific functionality for the Skylink Media Service. Contractors can authenticate, view assigned projects, and upload photos through these endpoints.

**Base URL:** `http://localhost:8085/api/v1`

**Authentication:** JWT Bearer Token

**API Documentation:** `http://localhost:8085/swagger-ui.html`

---

## Quick Start

### 1. Authenticate

```bash
curl -X POST http://localhost:8085/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "contractor1",
    "password": "password"
  }'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "contractor": {
    "id": 1,
    "username": "contractor1",
    "fullName": "John Doe",
    "role": "CONTRACTOR"
  }
}
```

### 2. Access Interactive API Documentation

**Swagger UI is publicly accessible** - no authentication required to view documentation:

```
http://localhost:8085/swagger-ui.html
```

You can browse all API endpoints and their specifications without logging in. To test protected endpoints in Swagger UI:
1. Click the "Authorize" button
2. Enter: `Bearer YOUR_JWT_TOKEN`
3. Click "Authorize"
4. Now you can test protected endpoints directly from the browser

### 2. List Assigned Projects

```bash
curl -X GET http://localhost:8085/api/v1/contractor/projects \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "workOrderNumber": "WO-2026-001",
      "location": "123 Main St",
      "clientCode": "CLIENT-A",
      "description": "Roof inspection",
      "status": "IN_PROGRESS",
      "dueDate": "2026-03-25",
      "photoCount": 5,
      "assignedAt": "2026-03-10T10:00:00"
    }
  ],
  "page": {
    "number": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

### 3. Get Project Details

```bash
curl -X GET http://localhost:8085/api/v1/contractor/projects/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 4. Upload Photos

```bash
curl -X POST http://localhost:8085/api/v1/contractor/projects/1/photos \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "files=@photo1.jpg" \
  -F "files=@photo2.jpg"
```

**Response:**
```json
{
  "uploaded": 2,
  "photos": [
    {
      "id": 10,
      "fileName": "20260317_143022_abc123_photo1.webp",
      "originalName": "photo1.jpg",
      "fileSize": 2048576,
      "thumbnailUrl": "/api/v1/files/thumb_20260317_143022_abc123_photo1.webp",
      "uploadedAt": "2026-03-17T14:30:22"
    }
  ]
}
```

---

## API Endpoints

### Authentication

#### POST /api/v1/auth/login
Authenticate contractor and receive JWT token.

**Request Body:**
```json
{
  "username": "string (required)",
  "password": "string (required)"
}
```

**Success Response:** `200 OK`
**Error Responses:**
- `400 Bad Request` - Invalid credentials or validation error
- `401 Unauthorized` - Authentication failed

---

### Projects

#### GET /api/v1/contractor/projects
List all projects assigned to the authenticated contractor.

**Query Parameters:**
- `page` (optional, default: 0) - Page number
- `size` (optional, default: 20) - Page size
- `status` (optional) - Filter by project status
- `sort` (optional, default: "id,desc") - Sort field and direction

**Headers:**
- `Authorization: Bearer {token}` (required)

**Success Response:** `200 OK`
**Error Responses:**
- `401 Unauthorized` - Missing or invalid token
- `403 Forbidden` - Access denied

---

#### GET /api/v1/contractor/projects/{id}
Get detailed information about a specific project.

**Path Parameters:**
- `id` (required) - Project ID

**Headers:**
- `Authorization: Bearer {token}` (required)

**Success Response:** `200 OK`
**Error Responses:**
- `401 Unauthorized` - Missing or invalid token
- `403 Forbidden` - Not assigned to this project
- `404 Not Found` - Project does not exist

---

### Photos

#### POST /api/v1/contractor/projects/{projectId}/photos
Upload one or more photos to a project.

**Path Parameters:**
- `projectId` (required) - Project ID

**Headers:**
- `Authorization: Bearer {token}` (required)
- `Content-Type: multipart/form-data`

**Form Data:**
- `files` (required) - One or more image files

**Validation:**
- Maximum file size: 10MB per file
- Maximum files per request: 10
- Allowed types: JPEG, PNG, WebP

**Success Response:** `201 Created`
**Error Responses:**
- `400 Bad Request` - Invalid file type or size
- `401 Unauthorized` - Missing or invalid token
- `403 Forbidden` - Not assigned to this project
- `404 Not Found` - Project does not exist
- `413 Payload Too Large` - File exceeds size limit

---

## Error Response Format

All error responses follow this structure:

```json
{
  "error": "ERROR_CODE",
  "message": "Human-readable error message",
  "details": {
    "field1": "Validation error message",
    "field2": "Another validation error"
  },
  "timestamp": "2026-03-17T14:30:22"
}
```

**Common Error Codes:**
- `VALIDATION_ERROR` - Request validation failed
- `NOT_FOUND` - Resource not found
- `FORBIDDEN` - Access denied
- `BAD_REQUEST` - Invalid request
- `INTERNAL_ERROR` - Server error
- `FILE_TOO_LARGE` - File size exceeds limit

---

## Authentication Flow

1. **Login:** POST credentials to `/api/v1/auth/login`
2. **Receive Token:** Store the JWT token securely
3. **Use Token:** Include in `Authorization: Bearer {token}` header for all subsequent requests
4. **Token Expiry:** Token expires after 24 hours (86400 seconds)
5. **Re-authenticate:** Login again when token expires

---

## Security Best Practices

1. **Never expose JWT tokens** in URLs or logs
2. **Store tokens securely** in mobile app secure storage
3. **Use HTTPS** in production environments
4. **Implement token refresh** before expiration
5. **Validate all inputs** on client side before sending

---

## Rate Limiting

Currently no rate limiting is enforced. This may be added in future versions.

---

## Pagination

All list endpoints support pagination with these parameters:
- `page` - Zero-based page number (default: 0)
- `size` - Number of items per page (default: 20, max: 100)
- `sort` - Sort field and direction (e.g., "dueDate,asc")

---

## Testing with Swagger UI

Access the interactive API documentation at:
```
http://localhost:8085/swagger-ui.html
```

1. Click "Authorize" button
2. Enter: `Bearer YOUR_JWT_TOKEN`
3. Click "Authorize"
4. Test endpoints directly from the browser

---

## Example: Complete Workflow

```bash
# 1. Login
TOKEN=$(curl -s -X POST http://localhost:8085/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"contractor1","password":"password"}' \
  | jq -r '.token')

# 2. List projects
curl -X GET http://localhost:8085/api/v1/contractor/projects \
  -H "Authorization: Bearer $TOKEN"

# 3. Get project details
curl -X GET http://localhost:8085/api/v1/contractor/projects/1 \
  -H "Authorization: Bearer $TOKEN"

# 4. Upload photos
curl -X POST http://localhost:8085/api/v1/contractor/projects/1/photos \
  -H "Authorization: Bearer $TOKEN" \
  -F "files=@photo1.jpg" \
  -F "files=@photo2.jpg"
```

---

## Support

For issues or questions, contact: support@skylink.com

---

## Version History

- **v1.0** (2026-03-17) - Initial REST API release
  - JWT authentication
  - Project listing and details
  - Photo upload functionality
