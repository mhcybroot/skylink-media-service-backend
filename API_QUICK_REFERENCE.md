# REST API Quick Reference

## Endpoint

```
POST http://localhost:8085/api/v1/auth/login
Content-Type: application/json
```

## Request Body

```json
{
  "username": "contractor1",
  "password": "password123"
}
```

## Success Response (200 OK)

```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "tokenType": "Bearer",
  "contractorId": 1,
  "fullName": "John Doe",
  "expiresAt": "2026-03-19T00:06:50Z",
  "expiresIn": 86400
}
```

## Error Responses

### 401 Unauthorized
```json
{
  "timestamp": "2026-03-18T00:06:50Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid username or password",
  "path": "/api/v1/auth/login"
}
```

### 400 Bad Request (Validation Error)
```json
{
  "timestamp": "2026-03-18T00:06:50Z",
  "status": 400,
  "error": "Validation Failed",
  "message": "Invalid input parameters",
  "path": "/api/v1/auth/login",
  "validationErrors": {
    "username": "Username must be between 3 and 50 characters",
    "password": "Password must be between 8 and 100 characters"
  }
}
```

## Using the Token

```bash
curl -X GET http://localhost:8085/api/v1/contractor/projects \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

## Validation Rules

- **username**: 3-50 characters, required
- **password**: 8-100 characters, required

## Token Details

- **Algorithm**: HMAC-SHA512
- **Expiry**: 24 hours
- **Format**: JWT (JSON Web Token)
- **Header**: `Authorization: Bearer <token>`

## Quick Test

```bash
# Test login
curl -X POST http://localhost:8085/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"contractor1","password":"password123"}'

# Run automated tests
./test-api.sh
```

## Common Issues

| Issue | Solution |
|-------|----------|
| 401 Unauthorized | Check username/password |
| 400 Bad Request | Validate JSON format and field lengths |
| 500 Internal Error | Check logs: `tail -f app.log` |

## Configuration

Located in `application.properties`:
```properties
jwt.secret=<your-secret>
jwt.expiration=86400000
cors.allowed-origins=http://localhost:3000,http://localhost:8080
```

## Build & Run

```bash
# Build
./gradlew build

# Run
./gradlew bootRun

# Test
./gradlew test
```
