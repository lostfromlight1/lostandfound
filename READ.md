# Lost & Found – Backend (Spring Boot)

## 📌 Overview

This is the **backend service** for the Lost & Found platform, built with **Spring Boot**.

It provides:

* RESTful APIs for authentication, users, posts, and comments
* JWT-based authentication (Access + Refresh tokens)
* Google OAuth2 login
* Email verification & password reset
* Image uploads via Cloudinary
* Secure, scalable production-ready configuration

---

## ⚙️ Tech Stack

* **Java 21**
* **Spring Boot**
* **Spring Security**
* **PostgreSQL**
* **Flyway (DB migrations)**
* **JWT Authentication**
* **Cloudinary (image storage)**
* **Docker (multi-stage build)**

---

## 🚀 Getting Started

### 1️⃣ Clone the Repository

```bash
git clone <your-repo-url>
cd backend
```

---

### 2️⃣ Environment Variables

Create a `.env` file in the root:

```env
# ==========================================
# Application Server
# ==========================================
SERVER_PORT=8080
PUBLIC_FRONTEND_URL=http://localhost:3000

# ==========================================
# Database Configuration
# ==========================================
DB_NAME=lostandfound_db
DB_PORT=5434
DB_USERNAME=your_database_user
DB_PASSWORD=your_database_password
DB_URL=jdbc:postgresql://localhost:5434/lostandfound_db

# ==========================================
# Security & Authentication
# ==========================================
JWT_SECRET=your_base64_encoded_jwt_secret_key_here
API_KEY=your_api_key_here

# ==========================================
# Email Configuration
# ==========================================
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_16_char_google_app_password

# ==========================================
# OAuth2 Google Credentials
# ==========================================
GOOGLE_CLIENT_ID=your_google_client_id.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=your_google_client_secret

# ==========================================
# Cloudinary
# ==========================================
CLOUDINARY_CLOUD_NAME=your_cloud_name_here
CLOUDINARY_API_KEY=your_api_key_here
CLOUDINARY_API_SECRET=your_api_secret_here
```

---

## 🐳 Running with Docker

### Build & Run

```bash
docker build -t lostandfound-backend .
docker run -p 8080:8080 --env-file .env lostandfound-backend
```

---

## 🛠️ Running Locally (Without Docker)

### Requirements

* Java 21
* PostgreSQL running on configured port

### Run

```bash
./gradlew bootRun
```

---

## 📂 Project Structure

```text
src/main/java/com/lostandfound
├── controller
├── service
├── serviceimpl
├── repository
├── model
├── dto
├── security
├── config
└── exception
```

---

## 🔐 Authentication System

### Token Strategy

* **Access Token**

    * Short-lived (15 minutes)
    * Sent via `Authorization: Bearer`

* **Refresh Token**

    * Stored in **HttpOnly Cookie**
    * Valid for 7 days

### Flow

1. User logs in → receives tokens
2. Access token used for API calls
3. On expiry → `/auth/refresh` issues new token

---

## 🌐 API Base Path

```text
/api/v1
```

### Example

```text
http://localhost:8080/api/v1/auth/login
```

---

## 🔓 Public Routes

The following routes do **NOT require authentication**:

* `/api/v1/auth/**`
* `/api/v1/users/*/profile`
* `/api/v1/posts`
* `/api/v1/posts/*`
* `/api/v1/images/upload`
* `/swagger-ui/**`
* `/v3/api-docs/**`
* `/oauth2/**`

---

## 🔒 Security Features

* JWT Authentication
* API Key protection (`X-API-KEY`)
* CORS configuration
* Role-based access control (`USER`, `ADMIN`)
* Secure cookies (HttpOnly, SameSite)

---

## 📤 File Uploads

* Uses **Cloudinary**
* Endpoint: `/api/v1/images/upload`
* Accepts `multipart/form-data`

---

## 🧪 API Documentation

Swagger UI available at:

```text
http://localhost:8080/swagger-ui.html
```

---

## 🗄️ Database

* PostgreSQL
* Managed via **Flyway migrations**

```text
src/main/resources/db/migration
```

---

## ⚡ Configuration Highlights

### JWT

* Access Token: 15 minutes
* Refresh Token: 7 days

### File Upload Limits

* Max file size: 20MB

### Connection Pool

* HikariCP (optimized)

---

## 📊 Monitoring & Logging

* Health check: `/actuator/health`
* Metrics: `/actuator/metrics`
* Logs include `traceId` for request tracking

---

## 🧱 Deployment Notes

* Uses **multi-stage Docker build**
* Runs as **non-root user**
* Logs stored in `/app/logs`
* Uploads stored in `/app/uploads`

---

## ⚠️ Important Notes

* JWT secret must be **Base64 encoded**
* Never commit `.env` file
* Use strong credentials in production
* Configure `COOKIE_DOMAIN` for deployment

---

## 📬 Future Improvements

* Rate limiting
* Redis caching
* WebSocket notifications
* Full-text search

---

## 👨‍💻 Author

Lost & Found Backend System
