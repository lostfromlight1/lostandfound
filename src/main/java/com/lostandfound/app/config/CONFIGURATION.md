## ⚙️ Lost and Found: Configuration Documentation

The `config` package is the **core configuration layer** of the application.
It defines how Spring Boot handles **database connections, security, documentation, and background processes**.

This package ensures the system remains **secure, structured, and easy to manage**.

---

# 📄 application.yaml (Master Configuration)

This is the **central configuration file** of the application.

It controls database connections, security settings, JWT configuration, and environment-based variables.

### Key Sections

### 🗃️ Datasource

Configures PostgreSQL database connection.

```yaml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

Environment variables are injected at runtime.

---

### 🛠️ Flyway

Flyway manages **database versioning and migrations**.

```yaml
spring:
  flyway:
    enabled: true
```

This ensures:

* database schema is version controlled
* migrations run automatically on startup
* database stays consistent across environments

---

### 🛡️ Security

Defines:

* public routes
* CORS configuration
* API security rules

Example:

```yaml
security:
  public-routes:
    - /api/auth/**
    - /swagger-ui/**
```

---

### 🔑 JWT

Defines token configuration.

```yaml
jwt:
  secret: ${JWT_SECRET}
  expiration: 86400000
```

This controls:

* token signing
* token expiration
* security integrity

---

# 🛡️ SecurityConfig.java

This is the **main security configuration** of the application.

It defines:

* filter chain
* authentication rules
* CORS
* JWT integration
* API Key validation
* exception handling

---

## Responsibilities

### Disable CSRF

```java
http.csrf(AbstractHttpConfigurer::disable);
```

CSRF is disabled because the API is **stateless** and uses JWT authentication.

---

### Enable CORS

```java
.cors(cors -> cors.configurationSource(corsConfigurationSource()))
```

This allows the frontend to communicate with the backend.

CORS configuration is loaded from `SecurityProperties`.

---

### Stateless Session

```java
.sessionManagement(
    session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
)
```

This ensures:

* no session is stored on server
* JWT handles authentication
* API remains scalable

---

### Exception Handling

```java
.exceptionHandling(
    ex -> {
        ex.authenticationEntryPoint(authEntryPoint);
        ex.accessDeniedHandler(accessDeniedHandler);
    }
)
```

Handles:

* unauthorized access
* forbidden access
* standardized API error responses

---

### Public Routes

```java
.requestMatchers(publicRoutes).permitAll()
.anyRequest().authenticated()
```

This means:

* public routes are accessible
* all other routes require authentication

Public routes are read from `SecurityProperties`.

---

# 🔄 Filter Chain Order

The filter chain is configured manually to ensure correct execution order.

### Order

```
TraceIdFilter
      ↓
ApiKeyFilter
      ↓
JwtAuthenticationFilter
      ↓
Controller
```

---

### TraceIdFilter

```java
http.addFilterBefore(traceIdFilter, UsernamePasswordAuthenticationFilter.class);
```

Adds request trace ID for logging and debugging.

---

### ApiKeyFilter

```java
http.addFilterAfter(apiKeyFilter, TraceIdFilter.class);
```

Validates `X-API-KEY` header.

Blocks unauthorized requests early.

---

### JwtAuthenticationFilter

```java
http.addFilterAfter(jwtAuthFilter, ApiKeyFilter.class);
```

Validates JWT token and loads user into Security Context.

---

# 🔐 Password Encoder

```java
@Bean
PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

Used for:

* hashing passwords
* verifying login credentials

BCrypt ensures strong password security.

---

# 🔑 AuthenticationManager

```java
@Bean
AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
    return config.getAuthenticationManager();
}
```

Used by:

* authentication service
* login process
* Spring Security authentication flow

---

# 🌐 CORS Configuration

Configured using `SecurityProperties`.

```java
config.setAllowedOrigins(corsProps.allowedOrigins());
config.setAllowedMethods(corsProps.allowedMethods());
config.setAllowedHeaders(corsProps.allowedHeaders());
config.setExposedHeaders(corsProps.exposedHeaders());
```

This allows:

* frontend applications
* Swagger UI
* external tools

to communicate safely with the API.

---

# 🛂 AuthorizationConfig.java

This component handles **resource ownership validation**.

It ensures users can only modify their own data.

---

## Purpose

Checks whether the logged-in user owns:

* a post
* a comment

Used by `@CheckSecurity` annotations.

---

## isPostOwner

```java
public boolean isPostOwner(Long postId)
```

Steps:

1. Get current user ID
2. Fetch post from database
3. Compare post owner with current user
4. Return true or false

Currently stubbed until repository is implemented.

---

## isCommentOwner

```java
public boolean isCommentOwner(Long commentId)
```

Steps:

1. Get current user ID
2. Fetch comment from database
3. Compare comment owner with current user
4. Return result

Currently stubbed.

---

## getCurrentUserId

```java
Authentication auth = SecurityContextHolder.getContext().getAuthentication();
```

Extracts authenticated user from security context.

Returns:

```
User ID
```

or

```
null
```

if user is not authenticated.

---

# 🧠 SecurityProperties.java

Provides **type-safe configuration binding**.

Instead of using multiple `@Value` annotations, settings are grouped into a structured object.

---

## Example

```java
securityProperties.publicRoutes()
securityProperties.cors()
```

This improves:

* readability
* maintainability
* configuration safety

---

# 📚 SwaggerConfig.java

Configures API documentation.

Swagger requires:

```
X-API-KEY
Bearer JWT
```

This allows testing endpoints directly from browser.

Access:

```
/swagger-ui.html
```

or

```
/swagger-ui/index.html
```

---

# 🕒 SchedulerConfig.java

Enables scheduled tasks.

```java
@EnableScheduling
```

Allows:

```java
@Scheduled(cron = "0 0 * * * *")
```

Use cases:

* cleanup tasks
* notifications
* background jobs
* system monitoring

---

# 🗃️ JpaConfig.java

Enables JPA auditing.

```java
@EnableJpaAuditing
```

Automatically manages:

```
created_at
updated_at
created_by
updated_by
```

No manual handling required.

---

# ✅ Configuration Flow

```
application.yaml
        ↓
SecurityProperties
        ↓
SecurityConfig
        ↓
AuthorizationConfig
        ↓
SwaggerConfig
        ↓
JpaConfig
        ↓
SchedulerConfig
```

---

# 🎯 Goal

The config package ensures:

* secure API access
* structured configuration
* scalable architecture
* controlled authentication
* clean database management
* reliable background processing
