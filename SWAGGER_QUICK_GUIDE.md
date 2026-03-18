# Swagger UI JWT Authentication - Quick Guide

## 🚀 Quick Start (3 Steps)

### Step 1: Login
```
1. Open: http://localhost:8085/swagger-ui/index.html
2. Find: "Authentication" section
3. Click: "POST /api/v1/auth/login"
4. Click: "Try it out"
5. Enter:
   {
     "username": "contractor1",
     "password": "password123"
   }
6. Click: "Execute"
7. Copy: The "token" value from response
```

### Step 2: Authorize
```
1. Click: "Authorize" button (🔒 icon at top right)
2. Paste: Your JWT token
3. Click: "Authorize"
4. Click: "Close"
```

### Step 3: Test
```
1. Now all protected endpoints work!
2. Look for lock icon (🔒) on endpoints
3. Requests automatically include: Authorization: Bearer {your-token}
```

---

## 📋 Visual Indicators

### Before Authorization
```
🔓 Open lock on protected endpoints
❌ Requests fail with 401 Unauthorized
```

### After Authorization
```
🔒 Closed lock on protected endpoints
✅ Requests succeed with 200 OK
✅ "Authorized" status in Authorize dialog
```

---

## 🧪 Test Commands

### Get Token
```bash
curl -X POST http://localhost:8085/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"contractor1","password":"password123"}' \
  | jq -r '.token'
```

### Verify Security Scheme
```bash
curl -s http://localhost:8085/v3/api-docs | jq '.components.securitySchemes'
```

### Run Automated Tests
```bash
./test-swagger-jwt.sh
```

---

## ❓ Troubleshooting

| Problem | Solution |
|---------|----------|
| No "Authorize" button | Clear cache, restart app |
| Token not working | Check token is valid, not expired |
| Login requires auth | Verify `@SecurityRequirements` annotation |
| 401 errors | Click "Authorize" and paste token |

---

## 📚 Documentation

- **Full Guide**: `SWAGGER_JWT_IMPLEMENTATION.md`
- **Summary**: `SWAGGER_JWT_COMPLETE.md`
- **Test Script**: `test-swagger-jwt.sh`
- **Verify Script**: `verify-swagger-jwt.sh`

---

## ✅ Success Checklist

- [ ] Application running: `./gradlew bootRun`
- [ ] Swagger UI opens: http://localhost:8085/swagger-ui/index.html
- [ ] "Authorize" button visible at top right
- [ ] Login endpoint works (no lock icon)
- [ ] Token obtained from login response
- [ ] "Authorize" dialog accepts token
- [ ] Protected endpoints show lock icon (🔒)
- [ ] Requests include Authorization header

---

**Status**: ✅ READY TO USE

**URLs**:
- Swagger UI: http://localhost:8085/swagger-ui/index.html
- OpenAPI Spec: http://localhost:8085/v3/api-docs

**Support**: See `SWAGGER_JWT_IMPLEMENTATION.md` for detailed guide
