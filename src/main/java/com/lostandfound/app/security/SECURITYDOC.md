## 🛡️ Lost and Found: Security Architecture Documentation

This document explains how we protect our API and how developers can use built-in tools to manage user permissions and data ownership.

---

## 🏗️ The Security "Filter Chain"

When a request hits our server, it must pass through a series of **checkpoints (Filters)** before it reaches a Controller.
If any checkpoint fails, the request is rejected immediately with a standardized error.

---

### 1️⃣ TraceIdFilter (The Labeler)

**Purpose:**
Every request is assigned a unique 12-character ID.

**How it works:**

* Generates a unique `traceId`
* Stores it in MDC (Logging Context)

**Why we need it:**

* Makes debugging easier
* Allows searching logs using `traceId` to track a full request lifecycle

---

### 2️⃣ ApiKeyFilter (The Gateway Guard)

**Purpose:**
Ensures the request comes from a trusted source (Frontend, Bruno, etc.)

**How it works:**

* Looks for `X-API-KEY` header
* Compares it with value in `application.yml`
* If missing or incorrect → returns **401 Unauthorized**

**Note:**

> This acts as the **front door** of the API.
> No API key = No access.

---

### 3️⃣ JwtAuthenticationFilter (The Identity Verifier)

**Purpose:**
Checks if the user is authenticated.

**How it works:**

* Reads `Authorization: Bearer <token>`
* Sends token to `JwtService`
* Validates token
* Loads user from database
* Stores user in `SecurityContextHolder`

**Result:**

> Once the user is inside **Security Context**, Spring Security recognizes the authenticated user throughout the request.

---

## 🛠️ Developer Tools

These helper tools reduce the need for complex security logic inside Controllers.

---

### 👤 @CurrentUser

Use this annotation when you need the logged-in user.

#### Usage

```java
@GetMapping("/me")
public ResponseEntity<BaseResponse<UserResponse>> getMe(@CurrentUser User user) {

    return BaseResponse.success(
        "Fetched",
        userService.mapToResponse(user)
    );
}
```

**How it works:**

* Uses `@AuthenticationPrincipal`
* Automatically injects the logged-in user into the controller method

---

### 🕵️ ResourceGuard

Utility class used to check **ownership and permissions**.

#### Methods

**checkOwner(Long ownerId)**

* Throws exception if logged-in user is not the owner

**checkOwnerOrAdmin(Long ownerId)**

* Allows access for:

    * Owner
    * Admin

---

## 🏷️ Controller Security Annotations (CheckSecurity)

Custom annotations define **who is allowed to access endpoints**.

| Annotation                          | Who can use it         |
| ----------------------------------- | ---------------------- |
| `@CheckSecurity.Public.canRead`     | Anyone                 |
| `@CheckSecurity.Admin.isRequired`   | Admin only             |
| `@CheckSecurity.Posts.canManage`    | Admin or Post Owner    |
| `@CheckSecurity.Comments.canManage` | Admin or Comment Owner |

**Note:**

> These annotations use `@PreAuthorize` internally, meaning security is checked **before the controller method executes**.

---

## 🔑 JwtService

The **core component** of the token system.

### Responsibilities

**1. Generate Tokens**

* userId
* email
* roles
* signed JWT

**2. Validate Tokens**

* ensures token is not expired
* ensures token has not been modified

**3. Extract Claims**

* email
* userId
* roles

---

## ⚙️ Configuration Files

### SecurityConfig

Main security blueprint.

Defines:

* filter order
* public routes (`/login`, `/register`)
* API key filter
* JWT filter
* authentication entry point
* exception handling

Includes:

* `RestAuthenticationEntryPoint`
* `AccessDeniedHandler`

---

### SwaggerConfig

API documentation configuration.

Adds support for:

* X-API-KEY
* Bearer Token

This allows testing APIs directly inside Swagger.

---

## 💡 Troubleshooting Guide

### ❌ Getting 401 Unauthorized

Check:

* API Key present
* JWT Token present
* Authorization header format correct

Required headers:

```
X-API-KEY: your_api_key
Authorization: Bearer your_token
```

---

### ❌ Debugging Failed Requests

Steps:

1. Check response header
2. Find `X-Trace-Id`
3. Search logs

Example:

```
X-Trace-Id: A8F23KD91LQ2
```

Search in logs:

```
A8F23KD91LQ2
```

---

### ❌ Using @CurrentUser in Public Routes

Public routes do not require JWT.

No JWT → No Security Context → `@CurrentUser` will be null

---

## ✅ Security Flow

```
Request
   ↓
TraceIdFilter
   ↓
ApiKeyFilter
   ↓
JwtAuthenticationFilter
   ↓
Controller
```

---

## 🎯 Goal

Make the API:

* Secure
* Traceable
* Easy to use
* Easy to debug
* Easy to maintain
