## ⚠️ Lost and Found: Exception Handling Documentation

The `exception` package provides a **centralized and standardized error handling system** for the entire application.

This ensures that every error — whether it comes from validation, security, business logic, or system failures — returns a **consistent JSON response format** to the frontend.

This improves:

* debugging
* frontend integration
* logging
* system stability
* developer experience

---

# 🏛️ Standard Error Response (BaseErrorResponse)

Every error in the system follows a consistent JSON structure.

### Response Structure

| Field            | Description                                 |
| ---------------- | ------------------------------------------- |
| timestamp        | Time the error occurred                     |
| httpStatus       | HTTP status code (400, 401, 403, 500, etc.) |
| errorCode        | Internal error identifier                   |
| message          | Human-readable error message                |
| path             | API endpoint being accessed                 |
| traceId          | Unique request identifier                   |
| validationErrors | Field-level validation issues               |

---

### Example Error Response

```json
{
  "timestamp": "2026-04-07T12:20:11",
  "httpStatus": 400,
  "errorCode": "REQ_001",
  "message": "Request validation failed",
  "path": "/api/posts",
  "traceId": "A8F23KD91LQ2",
  "validationErrors": {
    "title": "Title is required",
    "email": "Invalid email format"
  }
}
```

---

# 🧩 Core Components

---

# 1️⃣ ErrorCode.java (Error Dictionary)

Centralized enum that defines all application errors.

### Responsibilities

* assigns unique error code
* assigns HTTP status
* assigns default message
* standardizes error handling

---

### Example

```java
VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "REQ_001", "Request validation failed"),
AUTH_FAILED(HttpStatus.UNAUTHORIZED, "AUTH_001", "Authentication failed"),
ACCESS_DENIED(HttpStatus.FORBIDDEN, "AUTH_002", "Access denied"),
INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SYS_001", "Internal server error");
```

---

### Benefits

* consistent error responses
* easier debugging
* centralized error control
* structured API behavior

---

# 2️⃣ AppException.java (Custom Business Exception)

Custom runtime exception used for **business logic failures**.

### Usage

```java
throw new AppException(ErrorCode.EMAIL_ALREADY_IN_USE);
```

---

### Custom Message Example

```java
throw new AppException(
    ErrorCode.BUSINESS_ERROR,
    "You cannot delete this item because it was reported over 30 days ago."
);
```

---

### Purpose

* stops execution immediately
* returns clean error response
* avoids complex controller logic
* keeps services clean and readable

---

# 3️⃣ GlobalExceptionHandler.java (Central Error Handler)

This is the **main exception handling component**.

It automatically catches all exceptions thrown in the application.

---

## Annotation

```java
@RestControllerAdvice
```

This allows Spring to:

* intercept exceptions
* format responses
* return standardized JSON

---

# 📌 Application Exceptions

### AppException

```java
@ExceptionHandler(AppException.class)
```

Handles business logic errors.

### Flow

```
Service throws AppException
        ↓
GlobalExceptionHandler catches it
        ↓
Builds BaseErrorResponse
        ↓
Returns structured JSON
```

---

# 📌 Validation Errors

## MethodArgumentNotValidException

Triggered when:

```java
@Valid RequestDTO
```

fails.

### Example

```java
@NotBlank
String title
```

returns:

```json
{
  "title": "Title is required"
}
```

Mapped using:

```java
FieldError::getField
```

---

## ConstraintViolationException

Triggered when:

* request parameters
* query params
* path variables

fail validation.

Example:

```
GET /posts?page=-1
```

returns validation error.

---

## MaxUploadSizeExceededException

Handles file upload size limit.

### Response

```
The uploaded file exceeds the maximum allowed size of 20MB.
```

---

# 🔐 Security Errors

---

## BadCredentialsException

Triggered during login.

### Response

```
Invalid email or password
```

HTTP Status:

```
401 Unauthorized
```

---

## AccessDeniedException

Triggered when user lacks permission.

Example:

* user accessing admin route
* user modifying another user's resource

### Response

```
Access denied
```

HTTP Status:

```
403 Forbidden
```

---

# 📦 Malformed Request Errors

---

## HttpMessageNotReadableException

Triggered when JSON body is invalid.

Example:

```
missing brackets
wrong data type
invalid JSON
```

### Response

```
Malformed JSON request body
```

---

# 🌐 Spring Web Routing Errors

---

## HttpRequestMethodNotSupportedException

Triggered when wrong HTTP method is used.

Example:

```
POST /api/posts
but only GET is allowed
```

### Response

```
HTTP method 'POST' is not supported for this endpoint.
```

---

## MissingServletRequestParameterException

Triggered when required parameter is missing.

Example:

```
GET /posts?page=
```

### Response

```
Missing required parameter: 'page'
```

---

# 🚨 Global System Error Handler

Catch-all handler.

```java
@ExceptionHandler(Exception.class)
```

This handles unexpected system failures.

---

## Behavior

* logs full stack trace
* includes traceId
* returns safe message
* prevents server crash exposure

---

### Log Example

```
[A8F23KD91LQ2][INTERNAL_ERROR]
Unhandled exception at path /api/posts
NullPointerException
```

---

### Response

```json
{
  "errorCode": "SYS_001",
  "message": "An unexpected server error occurred. TraceId: A8F23KD91LQ2"
}
```

---

# 🧠 Internal Helper Methods

---

## buildAndLogResponse()

Responsible for:

* logging warning
* formatting error
* attaching traceId
* attaching validation errors
* building response

---

## buildResponse()

Creates:

```
BaseErrorResponse
```

and returns:

```
ResponseEntity
```

---

## getTraceId()

Extracts trace ID from MDC.

```java
MDC.get("traceId")
```

Returns:

```
traceId
```

or

```
SYSTEM
```

if unavailable.

---

# 🔄 Exception Flow

```
Controller / Service
        ↓
Exception Thrown
        ↓
GlobalExceptionHandler
        ↓
ErrorCode
        ↓
BaseErrorResponse
        ↓
JSON Response
```

---

# ✅ Benefits

### Consistent Error Format

All errors follow same structure.

---

### Centralized Error Handling

No try/catch in controllers.

---

### Better Debugging

traceId links logs and responses.

---

### Secure Responses

Internal errors hidden from users.

---

### Clean Architecture

Services throw exceptions
Handlers manage responses

---

# 🎯 Goal

The exception package ensures:

* consistent API errors
* centralized error management
* secure error responses
* traceable failures
* clean service layer
* reliable debugging
