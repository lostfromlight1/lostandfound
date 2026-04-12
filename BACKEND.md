# Lost & Found API – Frontend Integration Guide

## 📌 Overview

This document defines the **contract between the frontend and the Spring Boot backend**.

All API endpoints are prefixed with:
```text
/api/v1
```

**Base URL (Local):**
```text
http://localhost:8080/api/v1
```

The backend follows a **global response wrapper pattern**, meaning **every response is standardized** and must be unwrapped on the frontend.

---

# 1️⃣ Global Response Structures

All responses follow a consistent structure. The frontend **must always unwrap the `data` field** and never directly assume raw payload.

## ✅ Success Response (HTTP 200 / 201)
```ts
interface BaseResponse<T> {
  timestamp: string;     // ISO-8601 timestamp
  apiId: string;         // Example: AUTH-002, USER-004
  traceId: string;       // Unique request ID for debugging end-to-end
  message: string;       // Human-readable success message
  data: T | null;        // Actual response payload
}
```

## ❌ Error Response (HTTP 400 / 401 / 403 / 404 / 500)
```ts
interface BaseErrorResponse {
  timestamp: string;
  httpStatus: number;
  errorCode: string;     // Example: "VALIDATION_ERROR", "TOKEN_EXPIRED"
  message: string;
  path: string;
  apiId: string;
  traceId: string;
  validationErrors: Record<string, string>;
}
```

### 💡 Frontend / AI Handling
When using Axios, catch validation errors to show form field errors directly in the UI:
```ts
error.response.data.validationErrors
// Example: { "password": "Password must contain uppercase letter" }
```

---

# 2️⃣ Authentication & Headers

The backend utilizes a dual-token system (Access + Refresh).
* The **Access Token** is returned in the JSON payload (and as a cookie). The frontend should attach it as a Bearer token.
* The **Refresh Token** is managed strictly via **Secure HttpOnly Cookies**.

### Header Format
```http
Authorization: Bearer <access_token>
```

**CRITICAL AXIOS CONFIG:** Because the refresh token is in an HttpOnly cookie, your Axios instance **must** include:
```ts
axios.defaults.withCredentials = true;
```

---

# 3️⃣ TypeScript Data Models (DTOs)

## 👤 User & Auth
```ts
interface UserResponse {
  id: number;
  email: string;
  displayName: string;
  contactInfo?: string;
  role: "USER" | "ADMIN";
  avatarUrl?: string; // Cloudinary Image URL
}

interface AuthResponse {
  accessToken: string;
  refreshToken: string; // Also set automatically in HttpOnly cookie
  tokenType: string;
  expiresIn: number;
  user: UserResponse;
}
```

## 📄 Pagination (Custom PageResponse)
*Note: This replaces the default Spring Page object for a cleaner frontend experience.*
```ts
interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
```

## 🖼️ Media & Shared Types
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

# 4️⃣ Auth Endpoints (`/auth`)

## 🟢 Register (`POST /auth/register`)
**Auth Required:** ❌ No
**Body:** `{ "email": "", "password": "", "displayName": "", "contactInfo": "" }`
**Response:** `BaseResponse<UserResponse>`

## 🟢 Login (`POST /auth/login`)
**Auth Required:** ❌ No
**Body:** `{ "email": "", "password": "" }`
**Response:** `BaseResponse<AuthResponse>` *(Sets HttpOnly Cookies)*

## 🟢 Google Login (`POST /auth/google`)
**Auth Required:** ❌ No
**Body:** `{ "idToken": "google_jwt_token" }`
**Response:** `BaseResponse<AuthResponse>` *(Sets HttpOnly Cookies)*

## 🟢 Refresh Token (`POST /auth/refresh`)
**Auth Required:** ❌ No *(Relies on HttpOnly `app_refresh_token` Cookie)*
**Body:** *None required*
**Response:** `BaseResponse<AuthResponse>` *(Sets new HttpOnly Cookies)*

## 🔴 Logout (`POST /auth/logout`)
**Auth Required:** ✅ Yes
**Body:** *None*
**Response:** `BaseResponse<null>` *(Clears HttpOnly Cookies & invalidates DB token)*

## 🔐 Change Password (`POST /auth/change-password`)
**Auth Required:** ✅ Yes
**Body:** `{ "oldPassword": "", "newPassword": "" }`
**Response:** `BaseResponse<null>`

---

# 5️⃣ User Endpoints (`/users`)

## 👤 Get Current User (`GET /users/me`)
**Auth Required:** ✅ Yes
**Response:** `BaseResponse<UserResponse>`

## ✏️ Update Profile (`PUT /users/me`)
**Auth Required:** ✅ Yes
**Body:**
```json
{
  "displayName": "New Name",
  "contactInfo": "Telegram/Phone",
  "avatarUrl": "https://res.cloudinary.com/...",
  "avatarPublicId": "folder/xyz123"
}
```
**Response:** `BaseResponse<UserResponse>`

## 🌍 Public Profile (`GET /users/{id}/profile`)
**Auth Required:** ❌ No
**Response:** `BaseResponse<UserResponse>`

---

# 6️⃣ Post Endpoints (`/posts`)

## 📝 Create Post (`POST /posts/create`)
**Auth Required:** ✅ Yes
**Body:**
```json
{
  "title": "Found iPhone",
  "description": "Found near the park.",
  "type": "FOUND", // "LOST" | "FOUND"
  "categoryId": 1,
  "location": "YANGON",
  "lostFoundDate": "2026-04-12",
  "contactInfo": "09123456789",
  "reward": 50.00,
  "images": [
    { "url": "https://...", "publicId": "xyz", "sortOrder": 0 }
  ]
}
```
**Response:** `BaseResponse<PostDto>`

## ✏️ Update Post (`PUT /posts/{id}`)
**Auth Required:** ✅ Yes *(Must be Post Owner)*
**Body:** *(Same as Create + requires `status`: "OPEN" | "RESOLVED" | "CLOSED")*
**Response:** `BaseResponse<PostDto>`

## 📋 Get Feed (`GET /posts?page=0&size=10&type=LOST&categoryId=1&location=YANGON`)
**Auth Required:** ❌ No
**Response:** `BaseResponse<PageResponse<PostDto>>`

## 🗑️ Delete Post (`DELETE /posts/{id}`)
**Auth Required:** ✅ Yes *(Must be Post Owner or Admin)*
**Response:** `BaseResponse<null>`

---

# 7️⃣ Comment Endpoints (`/comments`)

## 💬 Create Comment (`POST /comments`)
**Auth Required:** ✅ Yes
**Body:** `{ "postId": 1, "content": "Is this still available?", "imageUrl": "", "imagePublicId": "" }`
**Response:** `BaseResponse<CommentResponse>`

## 📋 Get Post Comments (`GET /comments/post/{postId}`)
**Auth Required:** ❌ No
**Response:** `BaseResponse<CommentResponse[]>`

## ✏️ Update Comment (`PUT /comments/{id}`)
**Auth Required:** ✅ Yes *(Must be Comment Owner)*
**Body:** `{ "content": "Updated text", "imageUrl": "", "imagePublicId": "" }`
**Response:** `BaseResponse<CommentResponse>`

## 🗑️ Delete Comment (`DELETE /comments/{id}`)
**Auth Required:** ✅ Yes *(Must be Comment Owner or Admin)*
**Response:** `BaseResponse<null>`

---

# 8️⃣ Media Uploads (`/images`)

## 📤 Upload Image to Cloudinary (`POST /images/upload`)
**Auth Required:** ✅ Yes
**Format:** `multipart/form-data`
**Payload:** `file` (File object)
**Response:** `BaseResponse<ImageUploadResponse>`

---

# 9️⃣ Admin Endpoints (`ROLE_ADMIN` Required)

* **Get All Users:** `GET /users?page=0&size=20`
* **Search Users:** `GET /users/search?query=john&page=0&size=20`
* **Ban User:** `PUT /users/{id}/ban`
* **Unban User:** `PUT /users/{id}/unban`
* **Create Category:** `POST /categories` (Body: `{ "name": "Electronics" }`)
* **Update Category:** `PUT /categories/{id}`
* **Delete Category:** `DELETE /categories/{id}`

---

# 🔟 AI Integration Instructions (Cursor / Copilot)

**Recommended Prompt for AI Agents:**
```text
Read FRONTEND_API_GUIDE.md

Create the following API layer using TypeScript and Axios:
1. An Axios instance configured with `withCredentials: true` (Crucial for HttpOnly Refresh Cookies).
2. A request interceptor to attach the Bearer access token from a secure store (e.g., Zustand/Context).
3. A response interceptor that catches 401 errors, automatically calls `POST /api/v1/auth/refresh` (which will send the cookie), updates the access token in memory, and retries the original request.
4. A global error handler that specifically extracts `error.response.data.validationErrors` for form mapping.
5. Provide API service objects for Auth, Users, Posts, Comments, Categories, and Images based on the DTOs and endpoints in the guide.
```