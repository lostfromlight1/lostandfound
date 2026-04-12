# Lost & Found Frontend Architecture Guide (AI & Developer Friendly)

This document complements:

```
Lost & Found API – Frontend Integration Guide
```

and defines how the **frontend must be structured** so that:

- APIs are easy to create
- Request and response are separated
- AI tools (Cursor, Copilot, Claude, ChatGPT) can generate consistent code
- New features can be added quickly
- The project remains scalable

---

# 📌 Core Principle

Every feature must follow:

```
Feature-based architecture
Request / Response separation
Central API client
Service layer
Hook layer
Component layer
```

Flow:

```
Page → Hook → Service → API → Axios → Backend
```

---

# 📁 Folder Structure

```
src/
│
├── app/                     # Next.js App Router
│   ├── (auth)
│   │   ├── login
│   │   │   └── page.tsx
│   │   └── register
│   │       └── page.tsx
│   │
│   ├── api
│   │   └── auth
│   │       └── [...nextauth]
│   │           └── route.ts
│   │
│   ├── layout.tsx
│   ├── page.tsx
│   └── globals.css
│
│
├── components/              # Shared reusable UI
│   ├── form
│   └── ui
│
│
├── features/
│
│   ├── auth/
│   │   ├── api/
│   │   │   ├── auth.api.ts
│   │   │
│   │   │   ├── request/
│   │   │   │   ├── login.request.ts
│   │   │   │   ├── register.request.ts
│   │   │   │   ├── refresh.request.ts
│   │   │   │   └── change-password.request.ts
│   │   │   │
│   │   │   └── response/
│   │   │       ├── auth.response.ts
│   │   │       └── user.response.ts
│   │   │
│   │   ├── services/
│   │   │   └── auth.service.ts
│   │   │
│   │   ├── hooks/
│   │   │   └── useAuthActions.ts
│   │   │
│   │   ├── components/
│   │   │   ├── SignInForm.tsx
│   │   │   └── RegisterForm.tsx
│   │   │
│   │   ├── store/
│   │   │   └── auth.store.ts
│   │   │
│   │   └── index.ts
│
│
│   ├── users/
│   │   ├── api/
│   │   │   ├── users.api.ts
│   │   │   ├── request/
│   │   │   └── response/
│   │   │
│   │   ├── services/
│   │   │   └── users.service.ts
│   │   │
│   │   ├── hooks/
│   │   │   └── useUsers.ts
│   │   │
│   │   ├── components/
│   │   │   └── UserProfileCard.tsx
│   │   │
│   │   └── index.ts
│
│
│   ├── posts/        # future
│   │   ├── api/
│   │   ├── services/
│   │   ├── hooks/
│   │   └── components/
│
│
├── lib/
│   ├── api/
│   │   ├── axios.ts
│   │   ├── interceptor.ts
│   │   ├── api-client.ts
│   │   └── api-response.ts
│   │
│   ├── utils.ts
│   └── constants.ts
│
│
├── hooks/
│   ├── useAppForm.ts
│   └── use-mobile.ts
│
│
├── providers/
│   └── AppProviders.tsx
│
│
├── store/
│   └── app.store.ts
│
│
├── types/
│   ├── api.types.ts
│   ├── pagination.types.ts
│   └── next-auth.d.ts
```

---

# 🧠 Architecture Rules

---

# 1️⃣ Request and Response Separation

Every API must follow:

```
api/
   request/
   response/
   *.api.ts
```

Example:

```
auth/api/request/login.request.ts
auth/api/response/auth.response.ts
auth/api/auth.api.ts
```

---

# 2️⃣ Request File Rule

Request files define **only request body**.

Example:

```ts
export interface LoginRequest {
  email: string
  password: string
}
```

No API calls here.

No logic here.

Only DTO.

---

# 3️⃣ Response File Rule

Response files define **backend response structure**.

Example:

```ts
export interface AuthResponse {
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresIn: number
  user: UserResponse
}
```

---

# 4️⃣ API File Rule

API files call backend.

Example:

```ts
import apiClient from "@/lib/api/api-client"
import { LoginRequest } from "./request/login.request"
import { AuthResponse } from "./response/auth.response"
import { BaseResponse } from "@/types/api.types"

export const loginApi = (data: LoginRequest) =>
  apiClient.post<BaseResponse<AuthResponse>>(
    "/auth/login",
    data
  )
```

---

# 5️⃣ Service Layer Rule

Service unwraps response.

Example:

```ts
import { loginApi } from "../api/auth.api"

export const loginService = async (data) => {
  const response = await loginApi(data)
  return response.data.data
}
```

Always return:

```
response.data.data
```

---

# 6️⃣ Hook Layer Rule

Hooks manage UI logic.

Example:

```ts
export const useAuthActions = () => {

  const login = async (data) => {
    return await loginService(data)
  }

  return { login }
}
```

---

# 7️⃣ Page Uses Hook

```ts
const { login } = useAuthActions()
```

No API call directly in page.

---

# 🔁 API Flow

```
page.tsx
   ↓
hook
   ↓
service
   ↓
api
   ↓
axios
   ↓
backend
```

---

# 📦 Global API Client

```
lib/api/api-client.ts
```

Handles:

- base URL
- headers
- tokens
- interceptors

---

# 🔐 Axios Interceptor Responsibilities

```
lib/api/interceptor.ts
```

Must handle:

### Attach Token

```
Authorization: Bearer accessToken
```

---

### Handle 401

```
call /auth/refresh
retry request
```

---

### Refresh Fails

```
logout
redirect to login
```

---

# 🧠 How To Create New API

Example:

```
POST /users/update
```

### Step 1

Create request

```
users/api/request/update-user.request.ts
```

---

### Step 2

Create response

```
users/api/response/user.response.ts
```

---

### Step 3

Create API

```
users/api/users.api.ts
```

---

### Step 4

Create service

```
users/services/users.service.ts
```

---

### Step 5

Create hook

```
users/hooks/useUsers.ts
```

---

Done.

---

# 🤖 AI Instructions

When AI creates new feature:

It must follow:

```
feature/
   api/
      request/
      response/
      *.api.ts

   services/
   hooks/
   components/
```

---

# 🧠 AI Prompt

Use this when working with Cursor or Copilot:

```
Read:

Lost & Found API – Frontend Integration Guide
Frontend Architecture Guide

Create a new feature called:

users

Generate:

request files
response files
api file
service file
hook file

Follow folder structure strictly.

Always unwrap response.data.data
Use axios client
Use interceptor
Handle validation errors
```

---

# 🚫 Forbidden

Do not:

```
call axios inside page
mix request and response
skip service layer
skip hook layer
duplicate API logic
```

---

# ✅ Required

Always:

```
request folder
response folder
api file
service file
hook file
```

---

# 🎯 Goal

This architecture ensures:

- clean frontend
- predictable API usage
- easy AI generation
- scalable features
- clear separation
- fast development
- minimal bugs
- easy onboarding

---

# 📘 Works With

```
Lost & Found API – Frontend Integration Guide
```

Both documents together provide:

Backend Contract + Frontend Architecture

Complete system for AI-driven development.