## 🎮 Lost and Found: Controller Development Guide

Controllers are the **entry points of our application**.
They receive HTTP requests, validate incoming data, apply security rules, and delegate business logic to the **Service layer**.

Controllers should always be **thin, secure, and well-documented**.

---

# 🏗️ Controller Structure

Every controller in this project follows a clean and consistent structure.

### Required Annotations

```java
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "2. User Management", description = "Endpoints for user profiles and admin actions")
public class UserController {

    private final UserService userService;
}
```

### Annotation Explanation

| Annotation                 | Purpose                 |
| -------------------------- | ----------------------- |
| `@RestController`          | Marks class as REST API |
| `@RequestMapping`          | Defines base API path   |
| `@RequiredArgsConstructor` | Injects dependencies    |
| `@Slf4j`                   | Enables logging         |
| `@Tag`                     | Swagger grouping        |

---

# 👤 Current User Endpoints

These endpoints work with the **authenticated user**.

We use `@CurrentUser` to inject the logged-in user automatically.

---

## Get My Profile

```java
@GetMapping("/me")
@Operation(summary = "Get My Profile")
public ResponseEntity<BaseResponse<UserResponse>> getMe(
        @Parameter(hidden = true) @CurrentUser User currentUser) {

    log.info("REST request to get profile for current user ID: {}", currentUser.getId());

    UserResponse response = userService.getMe(currentUser);

    return BaseResponse.success("Profile fetched successfully", response);
}
```

### Flow

```
Request → Controller → Service → DTO → BaseResponse → Client
```

### Key Points

* `@CurrentUser` injects authenticated user
* `@Parameter(hidden = true)` hides user object in Swagger
* No business logic in controller
* Response wrapped in `BaseResponse`

---

## Update My Profile

```java
@PutMapping("/me")
@Operation(summary = "Update My Profile")
public ResponseEntity<BaseResponse<UserResponse>> updateProfile(
        @Parameter(hidden = true) @CurrentUser User currentUser,
        @Valid @RequestBody UpdateProfileRequest request) {

    log.info("REST request to update profile for user ID: {}", currentUser.getId());

    UserResponse response = userService.updateProfile(currentUser, request);

    return BaseResponse.success("Profile updated successfully", response);
}
```

### Key Concepts

| Feature        | Purpose                 |
| -------------- | ----------------------- |
| `@PutMapping`  | Update request          |
| `@Valid`       | Triggers DTO validation |
| `@RequestBody` | Accepts JSON            |
| DTO            | Structured input        |
| BaseResponse   | Standard output         |

---

# 🌍 Public Endpoints

Public endpoints are accessible without authentication.

We use:

```java
@CheckSecurity.Public.canRead
```

---

## Get Public Profile

```java
@GetMapping("/{id}/profile")
@CheckSecurity.Public.canRead
@Operation(summary = "Get Public Profile")
public ResponseEntity<BaseResponse<UserResponse>> getPublicProfile(
        @PathVariable Long id) {

    log.info("REST request to get public profile for user ID: {}", id);

    UserResponse response = userService.getPublicProfile(id);

    return BaseResponse.success("Public profile fetched successfully", response);
}
```

### Key Points

* Public access
* PathVariable for ID
* Returns safe user data

---

# 🛡️ Admin Endpoints

Admin endpoints require special permission.

We use:

```java
@CheckSecurity.Admin.isRequired
```

---

## Get All Users

```java
@GetMapping
@CheckSecurity.Admin.isRequired
@Operation(summary = "Get All Users (Admin)")
public ResponseEntity<BaseResponse<Page<UserResponse>>> getAllUsers(
        @PageableDefault(size = 20) Pageable pageable) {

    log.info("REST request to get all users. Page: {}", pageable.getPageNumber());

    Page<UserResponse> response = userService.getAllUsers(pageable);

    return BaseResponse.success("Users fetched successfully", response);
}
```

### Pagination Example

```
/api/v1/users?page=0&size=20&sort=id,desc
```

---

## Search Users

```java
@GetMapping("/search")
@CheckSecurity.Admin.isRequired
public ResponseEntity<BaseResponse<Page<UserResponse>>> searchUsers(
        @RequestParam String query,
        @PageableDefault(size = 20) Pageable pageable) {

    log.info("REST request to search users with query: {}", query);

    Page<UserResponse> response = userService.searchUsers(query, pageable);

    return BaseResponse.success("User search completed", response);
}
```

---

## Ban User

```java
@PutMapping("/{id}/ban")
@CheckSecurity.Admin.isRequired
public ResponseEntity<BaseResponse<Void>> banUser(
        @PathVariable Long id) {

    log.info("REST request to ban user ID: {}", id);

    userService.banUser(id);

    return BaseResponse.success("User has been banned successfully");
}
```

---

# 🔐 Security Overview

| Security Type | Annotation                        |
| ------------- | --------------------------------- |
| Public        | `@CheckSecurity.Public.canRead`   |
| Admin         | `@CheckSecurity.Admin.isRequired` |
| Authenticated | `@CurrentUser`                    |

---

# 📦 Standard Response Format

### Success

```java
BaseResponse.success("Message", data);
```

or

```java
BaseResponse.created("Created", data);
```

---

### Error

Handled by:

```
GlobalExceptionHandler
```

Example:

```json
{
  "timestamp": "...",
  "traceId": "...",
  "errorCode": "...",
  "message": "...",
  "validationErrors": {}
}
```

---

# 🧠 Controller Best Practices

### Keep Controllers Thin

❌ Bad

```java
if(user.getRole() != ADMIN){
    throw new RuntimeException();
}
```

✅ Good

```java
@CheckSecurity.Admin.isRequired
```

---

### Always Use DTOs

❌

```java
public User getUser()
```

✅

```java
public UserResponse getUser()
```

---

### Always Log Requests

```java
log.info("REST request to update profile for user ID: {}", currentUser.getId());
```

---

### Never Put Business Logic in Controllers

Controller:

```
Receive request
Validate
Check security
Call service
Return response
```

Service:

```
Business logic
Database operations
Ownership checks
Mapping
```

---

# 🏁 Final Flow

```
Client
   ↓
Controller
   ↓
Security
   ↓
Validation
   ↓
Service
   ↓
Repository
   ↓
DTO
   ↓
BaseResponse
   ↓
Client
```

---

# Tips

* Controllers = Gatekeepers
* Services = Brains
* DTOs = Data carriers
* Security annotations protect routes
* BaseResponse keeps API consistent
* Always log important actions
* Never return Entities
