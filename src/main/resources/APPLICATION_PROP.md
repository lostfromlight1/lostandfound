## ⚙️ Lost and Found: Environment & Application Configuration Guide

The configuration of this project is divided into two main parts:

* **Environment Variables (`.env`)** → stores sensitive and environment-specific values
* **Application Configuration (`application.yaml`)** → defines how Spring Boot behaves

This separation ensures **security, flexibility, and maintainability** across different environments such as local development, staging, and production.

---

# 🔑 1. Environment File (`.env`)

The `.env` file acts as the **secure configuration vault** of the application.

It contains sensitive values and environment-specific settings that should never be hardcoded inside the project.

---

## Purpose

* store secrets and credentials
* define runtime configuration
* allow environment-specific setup
* prevent sensitive data from being committed to Git
* make deployment flexible

---

## Example `.env`

```env
SERVER_PORT=8080

DB_NAME=lostandfound_db
DB_USER=lostandfound_user
DB_PASSWORD=securepassword
DB_PORT=5432
DB_URL=jdbc:postgresql://localhost:5432/lostandfound_db

JWT_SECRET=very_secure_secret_key
JWT_EXPIRATION=86400000

API_KEY=master_gateway_key

FRONTEND_URL=http://localhost:3000
```

---

## Key Sections

### Server Configuration

```env
SERVER_PORT=8080
```

Defines the port where Spring Boot runs.

---

### Database Configuration

```env
DB_NAME=lostandfound_db
DB_USER=lostandfound_user
DB_PASSWORD=securepassword
DB_PORT=5432
DB_URL=jdbc:postgresql://localhost:5432/lostandfound_db
```

Used by:

* Spring Boot to connect to database
* Docker Compose to create database
* application.yaml placeholders

---

### Security Configuration

```env
JWT_SECRET=very_secure_secret_key
API_KEY=master_gateway_key
```

Used for:

* JWT token signing
* API gateway protection
* authentication security

---

### Frontend Configuration

```env
FRONTEND_URL=http://localhost:3000
```

Used for:

* CORS configuration
* allowed origins
* API communication

---

## Best Practices

* never commit `.env` to Git
* use `.env.example` as template
* use strong JWT secrets
* separate dev and production values
* keep API keys secure
* rotate secrets periodically

---

# 🗺️ 2. Application Configuration (`application.yaml`)

The `application.yaml` file is the **central configuration blueprint** for Spring Boot.

It controls:

* database connection
* security behavior
* logging
* Flyway migrations
* API settings
* CORS rules

The file reads values from `.env` using placeholders.

---

## Example

```yaml
server:
  port: ${SERVER_PORT:8080}

spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USER}
    password: ${DB_PASSWORD}

  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        format_sql: true

  flyway:
    enabled: true
    locations: classpath:db/migration

app:
  security:
    api-key-header: X-API-KEY
    allowed-origins:
      - ${FRONTEND_URL}

logging:
  level:
    root: INFO
    com.lostandfound: INFO
```

---

# 🧩 Key Configuration Sections

## Server

```yaml
server:
  port: ${SERVER_PORT:8080}
```

Controls:

* API port
* server startup
* fallback value

---

## Spring Datasource

```yaml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USER}
    password: ${DB_PASSWORD}
```

Handles:

* database connection
* connection pooling
* credentials

Uses **HikariCP** for performance and stability.

---

## JPA & Hibernate

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
```

Controls:

* entity mapping
* schema validation
* SQL generation

### Naming Strategy

```yaml
camelCase → snake_case
displayName → display_name
```

Ensures clean database structure.

---

## Flyway

```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
```

Handles:

* database version control
* schema migrations
* SQL scripts

Example:

```id="flyway-example"
V1__create_users_table.sql
V2__add_roles.sql
V3__add_indexes.sql
```

---

## App Security Configuration

```yaml
app:
  security:
    api-key-header: X-API-KEY
    allowed-origins:
      - ${FRONTEND_URL}
```

Controls:

* API key header
* public routes
* CORS
* security filters

Used by:

* ApiKeyFilter
* SecurityConfig
* Gateway protection

---

## Logging Configuration

```yaml
logging:
  level:
    root: INFO
    com.lostandfound: INFO
```

Controls:

* console output
* debug level
* application logs

### Levels

| Level | Purpose           |
| ----- | ----------------- |
| ERROR | critical failures |
| WARN  | warnings          |
| INFO  | normal operations |
| DEBUG | detailed logs     |
| TRACE | full tracing      |

---

# 🐳 Docker & Environment Interaction

## Running Outside Docker

```id="docker-outside"
Spring Boot → localhost:5432
```

Uses:

```
DB_URL=jdbc:postgresql://localhost:5432/lostandfound_db
```

---

## Running Inside Docker

```id="docker-inside"
Spring Boot → db:5432
```

Uses:

```
DB_URL=jdbc:postgresql://db:5432/lostandfound_db
```

Because Docker uses **service names as hostnames**.

---

# 🧪 Common Configuration Issues

## Environment Variables Not Updating

Solution:

```id="docker-restart"
docker compose down
docker compose up --build
```

Restart containers to reload `.env`.

---

## Database Connection Refused

Check:

* DB_PORT
* DB_URL
* database running
* docker container status
* network configuration

---

## Logs Too Verbose

Change:

```yaml
logging:
  level:
    root: WARN
```

Keep:

```yaml
com.lostandfound: INFO
```

This keeps application logs visible while reducing noise.

---

# 🔐 Configuration Best Practices

* keep secrets in `.env`
* never hardcode credentials
* use placeholders in `application.yaml`
* separate dev and production configs
* secure JWT secret
* protect API keys
* use Flyway for database changes
* keep logging structured
* restart Docker after env changes

---

# 🏁 Configuration Flow

```id="config-flow"
.env
   ↓
application.yaml
   ↓
Spring Boot Configuration
   ↓
Security / Database / Logging
   ↓
Application Runtime
```

This structure ensures:

* security
* flexibility
* scalability
* maintainability
