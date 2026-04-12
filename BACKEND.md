# Lost & Found API – Frontend Integration Guide

## 📌 Overview

This document defines the **contract between the frontend and the Spring Boot backend**.

All API endpoints are prefixed with:

```
/api/v1
```

**Base URL (Local):**

```
http://localhost:8080/api/v1
```

The backend follows a **global response wrapper pattern**, meaning **every response is standardized** and must be unwrapped on the frontend.

---

# 1️⃣ Global Response Structures

All responses follow a consistent structure.

The frontend **must always unwrap the `data` field** and never directly assume raw payload.

---

## ✅ Success Response (HTTP 200 / 201)

```ts
interface BaseResponse<T> {
  timestamp: string;     // ISO-8601 timestamp
  apiId: string;         // Example: AUTH-002, USER-004
  traceId: string;       // Unique request ID for debugging
  message: string;       // Human-readable success message
  data: T | null;        // Actual response payload
}
```

### Example

```json
{
  "timestamp": "2026-04-09T10:20:12",
  "apiId": "AUTH-002",
  "traceId": "abc123xyz",
  "message": "Login successful",
  "data": {
    "accessToken": "...",
    "refreshToken": "...",
    "user": { }
  }
}
```

---

## ❌ Error Response (HTTP 400 / 401 / 403 / 404 / 500)

```ts
interface BaseErrorResponse {
  timestamp: string;
  httpStatus: number;
  errorCode: string;
  message: string;
  path: string;
  apiId: string;
  traceId: string;
  validationErrors: Record<string, string>;
}
```

### Example

```json
{
  "timestamp": "2026-04-09T10:25:12",
  "httpStatus": 400,
  "errorCode": "VALIDATION_ERROR",
  "message": "Invalid request",
  "path": "/api/v1/auth/register",
  "apiId": "AUTH-001",
  "traceId": "xyz987",
  "validationErrors": {
    "password": "Password must contain uppercase letter"
  }
}
```

### 💡 Frontend / AI Handling

When using Axios:

```ts
error.response.data.validationErrors
```

Use this to show **form field errors directly in UI**.

Example:

```
password → "Password must contain uppercase letter"
email → "Email already exists"
```

---

# 2️⃣ Authentication & Headers

Protected endpoints require **JWT Access Token**.

### Header Format

```http
Authorization: Bearer <access_token>
```

Optional (if API Gateway is used):

```http
X-API-KEY: your-api-key
```

---

# 3️⃣ TypeScript Data Models (DTOs)

## 👤 User

```ts
interface UserResponse {
  id: number;
  email: string;
  displayName: string;
  contactInfo: string;
  role: "USER" | "ADMIN";
}
```

---

## 🔐 Auth Response

```ts
interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: UserResponse;
}
```

---

## 📄 Pagination

```ts
interface PaginatedResponse<T> {
  content: T[];
  pageable: any;
  last: boolean;
  totalPages: number;
  totalElements: number;
  first: boolean;
  size: number;
  number: number;
  empty: boolean;
}
```

---

# 4️⃣ Auth Endpoints

Base Path:

```
/auth
```

---

## 🟢 Register

**POST**

```
/auth/register
```

Auth Required: ❌ No

### Body

```json
{
  "email": "user@email.com",
  "password": "StrongPassword123!",
  "displayName": "John",
  "contactInfo": "Telegram or Phone (optional)"
}
```

### Password Rules

- 8–128 characters
- Uppercase
- Lowercase
- Number
- Special character

### Response

```
BaseResponse<UserResponse>
```

---

## 🟢 Login

**POST**

```
/auth/login
```

Auth Required: ❌ No

### Body

```json
{
  "email": "user@email.com",
  "password": "password"
}
```

### Response

```
BaseResponse<AuthResponse>
```

---

## 🟢 Refresh Token

**POST**

```
/auth/refresh
```

Auth Required: ❌ No

### Body

```json
{
  "refreshToken": "your_refresh_token"
}
```

### Response

```
BaseResponse<AuthResponse>
```

---

## 🔴 Logout

**POST**

```
/auth/logout
```

Auth Required: ✅ Yes

### Response

```
BaseResponse<null>
```

Backend clears:

- refresh token
- session
- HttpOnly cookies

---

## 🔐 Change Password

**POST**

```
/auth/change-password
```

Auth Required: ✅ Yes

### Body

```json
{
  "oldPassword": "OldPassword123!",
  "newPassword": "NewPassword123!"
}
```

### Response

```
BaseResponse<null>
```

---

# 5️⃣ Password Reset Flow

## Step 1 — Request Reset

**POST**

```
/auth/reset-password?email=user@email.com
```

Auth Required: ❌ No

---

## Step 2 — Confirm Reset

**POST**

```
/auth/reset-password/confirm
```

### Body

```json
{
  "token": "reset_token",
  "newPassword": "NewPassword123!"
}
```

---

# 6️⃣ Email Verification Flow

## Verify Email

**GET**

```
/auth/verify-email?token=verification_token
```

---

## Resend Verification

**POST**

```
/auth/resend-verification?email=user@email.com
```

---

# 7️⃣ User Endpoints

Base Path:

```
/users
```

---

## 👤 Get Current User

**GET**

```
/users/me
```

Auth Required: ✅ Yes

### Response

```
BaseResponse<UserResponse>
```

---

## ✏️ Update Profile

**PUT**

```
/users/update
```

Auth Required: ✅ Yes

### Body

```json
{
  "displayName": "New Name",
  "contactInfo": "Telegram/Phone"
}
```

### Response

```
BaseResponse<UserResponse>
```

---

## 🌍 Public Profile

**GET**

```
/users/{id}/profile
```

Auth Required: ❌ No

### Response

```
BaseResponse<UserResponse>
```

✔ Public users can view profile without authentication

---

# 8️⃣ Admin Endpoints

Requires:

```
ROLE_ADMIN
```

---

## Get All Users

**GET**

```
/users?page=0&size=20
```

Response:

```
BaseResponse<PaginatedResponse<UserResponse>>
```

---

## Search Users

**GET**

```
/users/search?query=john&page=0&size=20
```

Response:

```
BaseResponse<PaginatedResponse<UserResponse>>
```

---

## Ban User

**PUT**

```
/users/{id}/ban
```

Response:

```
BaseResponse<null>
```

---

# 9️⃣ AI Integration Instructions

This file is designed for:

- Cursor
- GitHub Copilot
- Claude
- ChatGPT
- Frontend AI agents

---

## Recommended Prompt

```
Read FRONTEND_API_GUIDE.md

Create:

- Axios instance
- Authorization interceptor
- Refresh token interceptor
- Global error handler
- Validation error handler
- API service structure

Requirements:

- Access token stored in localStorage
- Refresh token stored securely
- Automatically call /auth/refresh on 401
- Retry original request
- Redirect to login if refresh fails
```

---

# 🔟 Expected Frontend Architecture

```
src/
│
├── api/
│   ├── axios.ts
│   ├── auth.api.ts
│   ├── user.api.ts
│
├── types/
│   ├── api.types.ts
│
├── services/
│   ├── auth.service.ts
│
├── hooks/
│   ├── useAuth.ts
│
└── store/
    ├── auth.store.ts
```

---

# ✅ Key Rules

### Always unwrap response

```ts
response.data.data
```

---

### Handle validation errors

```ts
error.response.data.validationErrors
```

---

### Attach JWT automatically

```ts
Authorization: Bearer token
```

---

### Refresh token on 401

```
401 → call /auth/refresh → retry request
```

---

# 🎯 Goal

This guide ensures:

- predictable API responses
- clean frontend integration
- easy AI-generated code
- strong authentication flow
- scalable architecture
- minimal integration bugs