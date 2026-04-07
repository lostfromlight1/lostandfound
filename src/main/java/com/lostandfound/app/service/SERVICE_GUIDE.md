## 🛠️ Lost and Found: Service Layer Development Guide

The `service` package contains the **core business logic of the application**.
It sits between the **Controller layer** (HTTP requests) and the **Repository layer** (database access).

The Service layer is responsible for:

* enforcing business rules
* handling transactions
* managing data transformations
* coordinating repositories
* mapping entities to DTOs
* logging system operations with trace IDs

Controllers should remain thin, while Services contain the application logic.

---

# 🏗️ Service Architecture

The project follows the **Interface–Implementation pattern**.

### Structure

```
service/
    BaseService.java
    UserService.java
    AuthService.java

serviceimpl/
    UserServiceImpl.java
    AuthServiceImpl.java
    CustomUserDetailsService.java
```

### Architecture Flow

```
Controller → Service Interface → Service Implementation → Repository → Database
```

### Why This Pattern

| Component      | Responsibility              |
| -------------- | --------------------------- |
| Interface      | Defines service contract    |
| Implementation | Contains business logic     |
| BaseService    | Provides shared utilities   |
| Repository     | Handles database operations |

This structure improves **testability, scalability, and maintainability**.

---

# 🧱 Core Service Components

## BaseService

### Purpose

Provides shared utilities used across all service implementations.

### Key Feature

```
getTraceId()
```

### Example

```java
log.info("[{}] Updating profile for user ID: {}", getTraceId(), userId);
```

### Benefits

* consistent logging
* easier debugging
* request tracing
* centralized utility access

All service implementations should extend `BaseService`.

---

# 👤 UserService Implementation

This service manages:

* user profiles
* public profiles
* admin user management
* search
* moderation (ban users)

---

## getMe()

Fetches the authenticated user's profile.

```java
@Override
public UserResponse getMe(User currentUser) {

    log.info("[{}] Fetching profile for current user ID: {}", getTraceId(), currentUser.getId());

    return mapToResponse(fetchUserById(currentUser.getId()));
}
```

### Responsibilities

* fetch latest user from database
* ensure fresh data
* map entity to response DTO
* log operation

---

## updateProfile()

Updates display name and contact information.

```java
@Override
@Transactional
public UserResponse updateProfile(User currentUser, UpdateProfileRequest request) {

    log.info("[{}] Updating profile for user ID: {}", getTraceId(), currentUser.getId());

    User user = fetchUserById(currentUser.getId());

    user.setDisplayName(request.displayName());
    user.setContactInfo(request.contactInfo());

    User updated = userRepository.save(user);

    log.info("[{}] Profile updated successfully for user ID: {}", getTraceId(), updated.getId());

    return mapToResponse(updated);
}
```

### Key Concepts

| Feature           | Purpose                      |
| ----------------- | ---------------------------- |
| `@Transactional`  | ensures database consistency |
| `fetchUserById()` | centralizes user lookup      |
| `save()`          | persists updates             |
| `mapToResponse()` | converts entity to DTO       |
| logging           | tracks operation             |

---

# 🌍 Public Profile

## getPublicProfile()

Fetches a user's public profile.

```java
@Override
public UserResponse getPublicProfile(Long userId) {

    log.info("[{}] Fetching public profile for user ID: {}", getTraceId(), userId);

    return mapToResponse(fetchUserById(userId));
}
```

### Responsibilities

* fetch user
* return safe data
* log operation

---

# 🛡️ Admin Operations

## getAllUsers()

Returns paginated user list.

```java
@Override
public Page<UserResponse> getAllUsers(Pageable pageable) {

    log.info("[{}] Fetching all users, page={}", getTraceId(), pageable.getPageNumber());

    return userRepository.findAll(pageable)
            .map(this::mapToResponse);
}
```

### Features

* pagination
* repository mapping
* DTO conversion
* trace logging

---

## searchUsers()

Search users by email or display name.

```java
@Override
public Page<UserResponse> searchUsers(String query, Pageable pageable) {

    log.info("[{}] Searching users with query='{}', page={}",
            getTraceId(),
            query,
            pageable.getPageNumber());

    Page<User> users = userRepository.searchUsers(query, pageable);

    log.info("[{}] Search returned {} users",
            getTraceId(),
            users.getTotalElements());

    return users.map(this::mapToResponse);
}
```

### Responsibilities

* execute repository search
* log results
* map entity to DTO

---

## banUser()

Locks a user account.

```java
@Override
@Transactional
public void banUser(Long userId) {

    log.info("[{}] Attempting to ban user ID: {}", getTraceId(), userId);

    User user = fetchUserById(userId);

    if (Boolean.TRUE.equals(user.getIsLocked())) {
        log.info("[{}] User ID: {} already banned", getTraceId(), userId);
        return;
    }

    user.setIsLocked(true);

    userRepository.save(user);

    log.warn("[{}] User ID: {} has been locked/banned", getTraceId(), userId);
}
```

### Business Logic

* fetch user
* check lock status
* update account
* save to database
* log warning

### JWT Note

JWT tokens remain valid until expiration unless a blacklist is implemented.
Locked users will be prevented from logging in again.

---

# 🧰 Helper Methods

## fetchUserById()

Centralized user lookup.

```java
private User fetchUserById(Long userId) {

    return userRepository.findById(userId)
            .orElseThrow(() -> {

                log.warn("[{}] User not found: {}", getTraceId(), userId);

                return new AppException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        "User profile not found"
                );
            });
}
```

### Benefits

* avoids duplicate lookup logic
* consistent error handling
* centralized logging

---

## mapToResponse()

Converts entity to DTO.

```java
private UserResponse mapToResponse(User user) {

    return UserResponse.builder()
            .id(user.getId())
            .email(user.getEmail())
            .displayName(user.getDisplayName())
            .contactInfo(user.getContactInfo())
            .role(user.getRole())
            .build();
}
```

### Purpose

* hide entity structure
* return safe API response
* standardize output

---

# 🔐 Service Layer Best Practices

## Use @Transactional

Required for:

* updates
* deletes
* inserts
* multi-step operations

Ensures rollback on failure.

---

## Log All Important Actions

```
log.info("[{}] Action message", getTraceId());
log.warn("[{}] Warning message", getTraceId());
log.error("[{}] Error message", getTraceId());
```

Provides:

* request tracking
* debugging
* monitoring
* audit logs

---

## Use Repository for Database Access

Services should not contain SQL.

Correct approach:

```
userRepository.findById()
userRepository.save()
userRepository.searchUsers()
```

---

## Always Map Entities to DTOs

Never return entities directly.

```
User → UserResponse
```

Ensures:

* data privacy
* consistent API structure
* controlled output

---

# 🏁 Service Layer Flow

```
Controller
     ↓
Service Interface
     ↓
Service Implementation
     ↓
Repository
     ↓
Database
     ↓
Entity
     ↓
DTO
     ↓
Controller
     ↓
Client
```

---

# 📌 Key Principles

* services contain business logic
* controllers delegate work
* repositories handle database
* DTOs control output
* BaseService provides utilities
* transactions ensure consistency
* logging enables traceability
