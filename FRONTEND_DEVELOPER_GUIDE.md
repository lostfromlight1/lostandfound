```markdown
# Lost & Found – Frontend Developer Guide

## 📌 Overview

This document defines the **contract between the frontend and the Lost & Found Spring Boot backend**.

### Base URL (Local)
```

[http://localhost:8080/api/v1](http://localhost:8080/api/v1)

````

### Backend Features
- JWT Authentication (Access + Refresh Token)
- Refresh Token via **Secure HttpOnly Cookies**
- Google OAuth2 Login
- Email Verification & Password Reset
- Cloudinary Image Uploads
- Standardized API responses (`apiId`, `traceId`)

> ⚠️ **CRITICAL RULES**
> - Always unwrap `response.data.data`
> - Always use `withCredentials: true`
> - Never store tokens in `localStorage`

---

# 1️⃣ Global Response Structure

## ✅ Success Response
```ts
interface BaseResponse<T> {
  timestamp: string;
  apiId: string;     // e.g., AUTH-002
  traceId: string;   // request tracking
  message: string;
  data: T | null;
}
````

## ❌ Error Response

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

### 🔥 Frontend Handling Rule

```ts
error.response.data.validationErrors
```

Use this directly to map form errors.

---

# 2️⃣ Authentication System

## Token Strategy

* **Access Token**

    * Returned in response body
    * Stored in memory (Zustand / Context)
    * Sent via Authorization header

* **Refresh Token**

    * Stored in **HttpOnly Cookie**
    * Automatically sent by browser

## Header Format

```
Authorization: Bearer <access_token>
```

## Axios Requirement (MANDATORY)

```ts
axios.defaults.withCredentials = true;
```

---

# 3️⃣ TypeScript Models (DTOs)

## 👤 Auth & User

```ts
interface UserResponse {
  id: number;
  email: string;
  displayName: string;
  contactInfo?: string;
  role: "USER" | "ADMIN";
  avatarUrl?: string;
}

interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: UserResponse;
}
```

## 📄 Pagination

```ts
interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
```

## 🖼️ Shared Types

```ts
interface ImageDto {
  id: number;
  url: string;
  sortOrder: number;
}

interface ImageUploadResponse {
  url: string;
  publicId: string;
}

interface CategoryDto {
  id: number;
  name: string;
}
```

---

# 4️⃣ API Endpoints

## 🔐 Auth (`/auth`)

| Endpoint                  | Method | Auth | Description            |
| ------------------------- | ------ | ---- | ---------------------- |
| `/register`               | POST   | ❌    | Register               |
| `/login`                  | POST   | ❌    | Login                  |
| `/google`                 | POST   | ❌    | Google OAuth           |
| `/refresh`                | POST   | ❌    | Refresh token (cookie) |
| `/logout`                 | POST   | ✅    | Logout                 |
| `/change-password`        | POST   | ✅    | Change password        |
| `/reset-password`         | POST   | ❌    | Request reset          |
| `/reset-password/confirm` | POST   | ❌    | Confirm reset          |
| `/verify-email`           | GET    | ❌    | Verify email           |
| `/resend-verification`    | POST   | ❌    | Resend email           |

---

## 👤 Users (`/users`)

| Endpoint        | Method | Auth | Description    |
| --------------- | ------ | ---- | -------------- |
| `/me`           | GET    | ✅    | Current user   |
| `/me`           | PUT    | ✅    | Update profile |
| `/{id}/profile` | GET    | ❌    | Public profile |

---

## 📝 Posts (`/posts`)

| Endpoint  | Method | Auth | Description      |
| --------- | ------ | ---- | ---------------- |
| `/create` | POST   | ✅    | Create post      |
| `/{id}`   | PUT    | ✅    | Update post      |
| `/`       | GET    | ❌    | Feed (paginated) |
| `/{id}`   | DELETE | ✅    | Delete post      |

---

## 💬 Comments (`/comments`)

| Endpoint         | Method | Auth | Description    |
| ---------------- | ------ | ---- | -------------- |
| `/`              | POST   | ✅    | Create comment |
| `/post/{postId}` | GET    | ❌    | Get comments   |
| `/{id}`          | PUT    | ✅    | Update comment |
| `/{id}`          | DELETE | ✅    | Delete comment |

---

## 🖼️ Images (`/images`)

| Endpoint  | Method | Auth | Description  |
| --------- | ------ | ---- | ------------ |
| `/upload` | POST   | ✅    | Upload image |

---

## 🛡️ Admin (ROLE_ADMIN)

* `GET /users`
* `GET /users/search`
* `PUT /users/{id}/ban`
* `PUT /users/{id}/unban`
* `POST /categories`
* `PUT /categories/{id}`
* `DELETE /categories/{id}`

---

# 5️⃣ Critical Flows

## 🔄 Auth Flow (JWT + Refresh)

1. Login → receive access token + cookie
2. Store access token in memory
3. Attach token to requests
4. On 401:

    * Call `/auth/refresh`
    * Update token
    * Retry original request

---

## 🔐 Google OAuth Flow

1. Get Google ID token (frontend)
2. Send to `/auth/google`
3. Backend returns tokens + sets cookies
4. Handle account collision errors

---

## 🚪 Logout Flow

1. Call `/auth/logout`
2. Backend clears cookies
3. Frontend clears memory token
4. Redirect to login

---

## 📤 Image Upload Flow

1. Upload file → `/images/upload`
2. Receive `{ url, publicId }`
3. Attach to post or profile

---

# 6️⃣ Frontend Architecture

## Recommended Structure

```
src/
├── api/
│   ├── axios.ts
│   ├── auth.api.ts
│   ├── user.api.ts
│   └── post.api.ts
├── types/
│   └── api.types.ts
├── services/
├── hooks/
├── store/
├── features/
└── pages/
```

---

# 7️⃣ Axios Rules (VERY IMPORTANT)

## Request Interceptor

* Attach access token

## Response Interceptor

* Auto unwrap `data`
* Handle 401 → refresh → retry

## Example Behavior

```ts
response.data.data
```

---

# 8️⃣ Security Rules

* ❌ No localStorage for tokens
* ✅ Access token in memory only
* ✅ HttpOnly cookie for refresh
* ✅ Always `withCredentials`
* ✅ Backend enforces auth

---

# 9️⃣ API Development Rules

When adding new APIs:

1. Define DTOs in `types`
2. Follow `BaseResponse<T>`
3. Use consistent naming (`AUTH-001`, `USR-002`)
4. Keep endpoints RESTful
5. Support pagination via `PageResponse`

---

# 🔟 AI Integration Prompt

Use this with Cursor / Copilot:

```
Read FRONTEND_DEVELOPER_GUIDE.md

Create:
1. Axios instance with withCredentials: true
2. Request interceptor (attach Bearer token from Zustand)
3. Response interceptor:
   - Handle 401
   - Call /auth/refresh
   - Retry request
4. Global error handler (validationErrors)
5. API services for:
   - Auth
   - Users
   - Posts
   - Comments
   - Images
   - Categories
```

```

---

### What I fixed (important)
- Removed duplicate sections (you had 2 full guides merged raw)
- Unified endpoint naming (`/posts` root vs blank)
- Standardized wording (no contradictions)
- Cleaned flow explanations (now matches real JWT flow)
- Tightened architecture → actually usable for devs + AI tools

---

If you want next step, I can:
- generate your **full Axios + interceptor implementation**
- or scaffold **entire frontend API layer (ready to paste)**
```
