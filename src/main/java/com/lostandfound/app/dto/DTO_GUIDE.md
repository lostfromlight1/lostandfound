# 📦 Lost and Found: DTO Guide

This document explains how **Data Transfer Objects (DTOs)** are used in the Lost and Found application and provides a clear guide on how to design, structure, and use them correctly.

DTOs define the exact shape of data exchanged between the **Frontend and Backend**, ensuring:

* sensitive database fields are never exposed
* request data is validated before reaching services
* API responses remain consistent
* frontend integration is predictable and safe

---

# 🧠 What is a DTO?

A **Data Transfer Object (DTO)** is a simple data structure used to transfer data between layers of the application.

DTOs act as a **contract** between:

```text
Frontend ⇄ Controller ⇄ Service ⇄ Entity ⇄ Database
```

Instead of exposing database entities directly, DTOs control:

* what data enters the system
* what data leaves the system

---

# 🏗️ DTO Structure in the Project

DTOs are organized into two main categories:

```text
dto/
   request/
   response/
```

---

# 🛡️ Request DTOs (Incoming Data)

Request DTOs define **data sent from the frontend to the backend**.

They are used in:

```java
@RequestBody
```

inside controllers.

---

## Purpose

Request DTOs ensure:

* valid input
* clean data
* safe database operations
* structured API requests

---

## Example

```java
public record CreateItemRequest(

    @NotBlank(message = "Title is required")
    @Size(max = 100)
    String title,

    @NotBlank(message = "Description is required")
    String description,

    @NotNull(message = "Category is required")
    ItemCategory category

) {}
```

---

# 🧩 Validation Annotations

Spring Boot uses **Bean Validation** to automatically validate incoming requests.

---

## @NotBlank

Ensures:

* not null
* not empty
* not whitespace

```java
@NotBlank(message = "Title is required")
String title
```

---

## @Email

Ensures valid email format.

```java
@Email(message = "Invalid email format")
String email
```

---

## @Size

Controls string length.

```java
@Size(min = 8, max = 100)
String password
```

---

## @NotNull

Ensures value exists.

```java
@NotNull
ItemCategory category
```

---

## @Pattern

Used for regex validation.

Example: strong password

```java
@Pattern(
    regexp = "^(?=.*[A-Z])(?=.*[0-9]).{8,}$",
    message = "Password must contain uppercase and number"
)
String password
```

---

# 📌 Request DTO Rules

### Always include validation

Every field should be validated.

---

### Always include messages

```java
@NotBlank(message = "Email is required")
```

This message appears in:

```json
validationErrors
```

inside API error responses.

---

### Keep DTOs simple

DTOs should only contain:

* fields
* validation annotations

No logic.

---

# 📤 Response DTOs (Outgoing Data)

Response DTOs define **data returned to the frontend**.

They prevent exposing:

* passwords
* internal IDs
* system fields
* database structure

---

## Example

```java
@Builder
public record ItemSummaryResponse(

    Long id,
    String title,
    LocalDateTime createdAt,
    String creatorName

) {}
```

---

# 🧱 Why Use Response DTOs?

Never return:

```java
@Entity
```

directly.

Because entities may contain:

* password
* internal fields
* relationships
* sensitive data

DTOs protect the system.

---

# 🛠️ Mapping Entity to DTO

Inside Service layer:

```java
return ItemSummaryResponse.builder()
        .id(item.getId())
        .title(item.getTitle())
        .createdAt(item.getCreatedAt())
        .creatorName(item.getUser().getDisplayName())
        .build();
```

---

# 📦 Standard Response DTOs

---

# UserResponse

Used to return user profile.

Contains:

```text
id
displayName
email
role
createdAt
```

No password.

---

# AuthResponse

Used during login and registration.

Contains:

```text
accessToken
user
```

Example:

```json
{
  "accessToken": "jwt_token",
  "user": {
    "id": 1,
    "displayName": "John",
    "role": "USER"
  }
}
```

---

# 🏢 Global Response Wrappers

All responses are wrapped inside global response classes.

This ensures **consistent API structure**.

---

# ✅ BaseResponse<T> (Success Response)

Used for successful API calls.

---

## Structure

| Field     | Description        |
| --------- | ------------------ |
| timestamp | response time      |
| traceId   | request identifier |
| message   | success message    |
| data      | returned DTO       |

---

## Example

```json
{
  "timestamp": "2026-04-07T12:00:00",
  "traceId": "A8F23KD91LQ2",
  "message": "Fetched successfully",
  "data": {}
}
```

---

## Controller Usage

```java
return BaseResponse.success("Fetched successfully", data);
```

or

```java
return BaseResponse.created("Created successfully", data);
```

---

# ❌ BaseErrorResponse (Failure Response)

Used automatically by:

```text
GlobalExceptionHandler
```

---

## Structure

| Field            | Description   |
| ---------------- | ------------- |
| timestamp        | error time    |
| httpStatus       | status code   |
| errorCode        | internal code |
| message          | error message |
| path             | endpoint      |
| traceId          | request id    |
| validationErrors | field errors  |

---

# 🧭 DTO Naming Convention

---

## Request DTO

```text
CreatePostRequest
LoginRequest
RegisterRequest
UpdateUserRequest
```

Always end with:

```text
Request
```

---

## Response DTO

```text
UserResponse
PostResponse
ItemSummaryResponse
AuthResponse
```

Always end with:

```text
Response
```

---

# 📐 DTO Design Guidelines

---

## Keep DTOs Flat

Avoid deep nesting.

Good:

```text
PostResponse
  title
  description
  creatorName
```

Avoid:

```text
PostResponse
  user
     address
        city
```

---

# No Business Logic

DTOs must not contain:

* database calls
* services
* calculations
* business rules

DTOs are only data holders.

---

# Immutable DTOs

Use Java Records.

```java
public record UserResponse(
    Long id,
    String name
) {}
```

Benefits:

* immutable
* thread safe
* lightweight
* clean syntax

---

# 🧩 DTO Flow

```text
Frontend Request
        ↓
Request DTO
        ↓
Controller
        ↓
Service
        ↓
Entity
        ↓
Database
        ↓
Entity
        ↓
Response DTO
        ↓
BaseResponse
        ↓
Frontend
```

---

# ✅ Best Practices

### Use Records

Cleaner and safer.

---

### Validate All Requests

Never trust frontend input.

---

### Never Return Entities

Always map to DTOs.

---

### Use BaseResponse

Keep API consistent.

---

### Keep DTOs Simple

No logic, only fields.

---

# 🎯 Goal

The DTO system ensures:

* safe data transfer
* consistent API responses
* protected database structure
* clean architecture
* predictable frontend integration
* scalable system design
