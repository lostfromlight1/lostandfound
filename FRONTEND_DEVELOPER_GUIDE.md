Here is the fully formatted, clean Markdown version of your guide. You can copy the entire block below and paste it directly into your `FRONTEND_DEVELOPER_GUIDE.md` file.

```markdown
# Lost & Found – Frontend Developer Guide

## 📌 Overview

This document defines the **frontend contract with the Lost & Found backend**.

**Base URL (Local):**
`http://localhost:8080/api/v1`

The backend uses:
* **JWT Access Tokens** (stored in HttpOnly cookies)
* **Refresh Tokens** (stored in HttpOnly cookies)
* **OAuth2 (Google)** login
* **Email verification**
* **Password reset flow**
* Standardized **API responses** with `apiId` for tracing
* Public and protected routes

Frontend **must always unwrap `data`** from responses and use `withCredentials: true` for Axios requests.

---

## 1️⃣ Global Response Structures

### ✅ Success Response
```typescript
interface BaseResponse<T> {
  timestamp: string;    // ISO-8601
  apiId: string;        // e.g., AUTH-002, USER-004
  traceId: string;      // Unique request ID
  message: string;      // Human-readable
  data: T | null;       // Payload
}
```

### ❌ Error Response
```typescript
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
> **Frontend Note:** Map `error.response.data.validationErrors` directly to your form fields.

---

## 2️⃣ Authentication & Headers

Protected routes require:
```http
Authorization: Bearer <access_token>
```

For Axios, ensure cookies are sent with every request:
```javascript
axios.defaults.withCredentials = true; // send HttpOnly cookies
```

Optional API key header (for API gateway):
```http
X-API-KEY: your-api-key
```

---

## 3️⃣ TypeScript Models (DTOs)

### 👤 User
```typescript
interface UserResponse {
  id: number;
  email: string;
  displayName: string;
  contactInfo: string;
  role: "USER" | "ADMIN";
}
```

### 🔐 Auth
```typescript
interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: UserResponse;
}
```

### 📄 Pagination
```typescript
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

## 4️⃣ Auth Endpoints

| Endpoint | Method | Auth | Body / Query | Response |
| :--- | :--- | :--- | :--- | :--- |
| `/auth/register` | POST | ❌ | `{ email, password, displayName, contactInfo? }` | `BaseResponse<UserResponse>` |
| `/auth/login` | POST | ❌ | `{ email, password }` | `BaseResponse<AuthResponse>` |
| `/auth/refresh` | POST | ❌ | `{ refreshToken }` | `BaseResponse<AuthResponse>` |
| `/auth/change-password` | POST | ✅ | `{ oldPassword, newPassword }` | `BaseResponse<null>` |
| `/auth/reset-password` | POST | ❌ | `?email=` | `BaseResponse<null>` |
| `/auth/reset-password/confirm` | POST | ❌ | `{ token, newPassword }` | `BaseResponse<null>` |
| `/auth/verify-email` | GET | ❌ | `?token=` | `BaseResponse<null>` |
| `/auth/resend-verification` | POST | ❌ | `?email=` | `BaseResponse<null>` |
| `/auth/logout` | POST | ✅ | *none* | `BaseResponse<null>` |

---

## 5️⃣ OAuth2 (Google) Flow

1.  **Frontend redirects to:** `http://localhost:8080/oauth2/authorization/google`
2.  **Backend handles login/register.**
3.  **Backend sets HttpOnly cookies:** `accessToken`, `refreshToken`
4.  **Backend redirects to:** `http://localhost:3000/dashboard`
5.  **Frontend fetches current user:** `GET /users/me` (using `withCredentials: true`)

> **Notes:**
> * OAuth login blocks if a LOCAL account exists with the same email.
> * Frontend reads `/login?error=...` for collision messages.

---

## 6️⃣ User Endpoints

| Endpoint | Method | Auth | Description | Response |
| :--- | :--- | :--- | :--- | :--- |
| `/users/me` | GET | ✅ | Current user profile | `BaseResponse<UserResponse>` |
| `/users/update` | PUT | ✅ | Update profile | `BaseResponse<UserResponse>` |
| `/users/{id}/profile` | GET | ❌ | Public user profile | `BaseResponse<UserResponse>` |
| `/users?page=0&size=20` | GET | ✅ ADMIN | Get all users | `BaseResponse<PaginatedResponse<UserResponse>>` |
| `/users/search` | GET | ✅ ADMIN | Search users | `BaseResponse<PaginatedResponse<UserResponse>>` |
| `/users/{id}/ban` | PUT | ✅ ADMIN | Ban user | `BaseResponse<null>` |

---

## 7️⃣ Axios Rules
* `withCredentials: true` → **Must** be set to send cookies.
* **Auto-unwrap:** Extract data using `response.data.data`.
* **Validation errors:** Extract using `error.response.data.validationErrors`.
* **Refresh token on 401:** Catch the 401, call `/auth/refresh`, and retry the original request.

---

## 8️⃣ Recommended Folder Structure (Frontend)

```text
src/
├── api/
│   ├── axios.ts
│   ├── auth.api.ts
│   └── user.api.ts
├── types/
│   └── api.types.ts
├── services/
│   ├── auth.service.ts
│   └── user.service.ts
├── hooks/
│   ├── useAuth.ts
│   └── useUser.ts
├── store/
│   ├── auth.store.ts
│   └── user.store.ts
├── features/
│   ├── auth/
│   │   ├── api/
│   │   ├── components/
│   │   └── hooks/
│   └── users/
└── pages/
    ├── login.tsx
    ├── register.tsx
    └── dashboard.tsx
```

---

## 9️⃣ Logout Flow
1.  Frontend calls `POST /auth/logout`.
2.  Backend clears HttpOnly cookies.
3.  Frontend redirects to `/login`.

---

## 🔟 Password Reset & Email Verification
* `/auth/reset-password` → Request reset link via email.
* `/auth/reset-password/confirm` → Confirm reset with token and new password.
* `/auth/verify-email` → Verify email address with token.
* `/auth/resend-verification` → Resend the verification email.

---

## 1️⃣1️⃣ Creating New APIs
1.  Add request DTO in `features/*/api`.
2.  Add response DTO in `types/api.types.ts`.
3.  Wrap all responses in `BaseResponse<T>`.
4.  Follow the `apiId` convention (e.g., `MODULE-001`, `MODULE-002`).

---

## 1️⃣2️⃣ AI Integration Prompt Example

When using AI tools (like Cursor, Copilot, or Claude) to build the frontend, paste this document into the context and use a prompt like this:

> "Read `FRONTEND_DEVELOPER_GUIDE.md`. Create:
> * An Axios instance with `withCredentials: true`.
> * An Auth interceptor for cookies.
> * A Refresh token interceptor.
> * A Global error handler.
> * A Validation error handler.
> * An API service structure.
>
> **Requirements:**
> * Auto unwrap `response.data.data`.
> * Handle validation errors mapping them to form fields.
> * Refresh token retry on 401.
> * Redirect to `/login` if the refresh fails."

---

## 1️⃣3️⃣ Security Rules
* **Always** use `withCredentials` for cookies.
* **Never** store access or refresh tokens in `localStorage`.
* Frontend should read `/login?error` to handle OAuth collisions cleanly.
* Protected routes require backend JWT validation.
* Public routes are whitelisted in the backend config.

---

## 1️⃣4️⃣ Goal
* Predictable API responses
* Clean frontend integration
* AI-assisted development optimization
* Scalable architecture
* Minimal integration bugs
* Secure OAuth + JWT implementation
```