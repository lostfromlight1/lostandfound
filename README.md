## 🚀 Quick Start for Developers

You do not need to install Java or PostgreSQL locally. Everything is containerized. 

**Prerequisites:**
- [Docker Desktop](https://www.docker.com/products/docker-desktop/) installed and running.

**Setup Steps:**
1. **Clone the repository:**
   ```
   git clone https://github.com/lostfromlight1/lostandfound.git
   cd lostandfound
   ```
2. **Set up your environment variables:**
   Copy the example environment file to create your local config.
   `cp .env.example .env`

<<<<<<< Updated upstream
3. **Start the application:**
   `docker compose up -d --build`
=======
* RESTful APIs for authentication, users, posts, and comments
* JWT-based authentication (Access + Refresh tokens)
* Google OAuth2 login
* Email verification & password reset
* Image uploads via Cloudinary
* Standardized API responses (`apiId`, `traceId`)
>>>>>>> Stashed changes

**That's it for now!** - The API will be available at `http://localhost:8080`
- Swagger UI Documentation: `http://localhost:8080/swagger-ui.html`
- The database is exposed on port `5433` if you want to connect via DBeaver/DataGrip.

<<<<<<< Updated upstream
To view the real-time application logs, run:
`docker compose logs -f app`
=======
## ⚙️ Tech Stack

* Java 21
* Spring Boot
* Spring Security
* PostgreSQL
* Flyway (DB migrations)
* JWT Authentication
* Cloudinary
* Docker (multi-stage build)

---

## 🚀 Getting Started

### 1️⃣ Clone Repository

```bash
git clone https://github.com/lostfromlight1/lostandfound.git
cd lostandfound
```

---

### 2️⃣ Environment Variables

Create a `.env` file:

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

```bash
docker build -t lostandfound-backend .
docker run -p 8080:8080 --env-file .env lostandfound-backend
```

---

## 🛠️ Running Locally

```bash
./gradlew bootRun
```

---

# 🌐 API Contract

All APIs follow a **global response wrapper**.

## ✅ Success Response

```ts
interface BaseResponse<T> {
  timestamp: string;
  apiId: string;
  traceId: string;
  message: string;
  data: T | null;
}
```

## ❌ Error Response

```ts
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

### 🔥 Validation Errors Example

```json
{
  "validationErrors": {
    "email": "Invalid email format",
    "password": "Must contain uppercase letter"
  }
}
```

---

# 🔐 Authentication System

## Token Strategy

**Access Token**

* Returned in response body
* Sent via `Authorization: Bearer`
* Lifetime: 15 minutes

**Refresh Token**

* Stored in **HttpOnly Cookie**
* Lifetime: 7 days

---

## 🔄 Authentication Flow

1. Login → returns access token + cookie
2. Client sends access token in header
3. On expiry → `/auth/refresh`
4. Logout clears cookies and invalidates token

---

# 4️⃣ API Endpoints

All endpoints are prefixed with:

```text
/api/v1
```

---

## 🔐 Auth (`/auth`)

| Endpoint                  | Method | Auth | Description            |
| ------------------------- | ------ | ---- | ---------------------- |
| `/register`               | POST   | ❌    | Register new user      |
| `/login`                  | POST   | ❌    | Login user             |
| `/google`                 | POST   | ❌    | Google OAuth login     |
| `/refresh`                | POST   | ❌    | Refresh token (cookie) |
| `/logout`                 | POST   | ✅    | Logout user            |
| `/change-password`        | POST   | ✅    | Change password        |
| `/reset-password`         | POST   | ❌    | Request password reset |
| `/reset-password/confirm` | POST   | ❌    | Confirm reset          |
| `/verify-email`           | GET    | ❌    | Verify email           |
| `/resend-verification`    | POST   | ❌    | Resend verification    |

---

## 👤 Users (`/users`)

| Endpoint        | Method | Auth | Description         |
| --------------- | ------ | ---- | ------------------- |
| `/me`           | GET    | ✅    | Get current user    |
| `/me`           | PUT    | ✅    | Update profile      |
| `/{id}/profile` | GET    | ❌    | Public user profile |

---

## 📝 Posts (`/posts`)

| Endpoint  | Method | Auth | Description              |
| --------- | ------ | ---- | ------------------------ |
| `/create` | POST   | ✅    | Create new post          |
| `/{id}`   | PUT    | ✅    | Update post (owner only) |
| `/`       | GET    | ❌    | Get posts (paginated)    |
| `/{id}`   | DELETE | ✅    | Delete post              |

---

## 💬 Comments (`/comments`)

| Endpoint         | Method | Auth | Description       |
| ---------------- | ------ | ---- | ----------------- |
| `/`              | POST   | ✅    | Create comment    |
| `/post/{postId}` | GET    | ❌    | Get post comments |
| `/{id}`          | PUT    | ✅    | Update comment    |
| `/{id}`          | DELETE | ✅    | Delete comment    |

---

## 🖼️ Images (`/images`)

| Endpoint  | Method | Auth | Description  |
| --------- | ------ | ---- | ------------ |
| `/upload` | POST   | ✅    | Upload image |

---

## 🛡️ Admin (ROLE_ADMIN)

| Endpoint            | Method | Description     |
| ------------------- | ------ | --------------- |
| `/users`            | GET    | Get all users   |
| `/users/search`     | GET    | Search users    |
| `/users/{id}/ban`   | PUT    | Ban user        |
| `/users/{id}/unban` | PUT    | Unban user      |
| `/categories`       | POST   | Create category |
| `/categories/{id}`  | PUT    | Update category |
| `/categories/{id}`  | DELETE | Delete category |

---

## 🔒 Security

* JWT Authentication
* API Key via `X-API-KEY`
* Role-based access control
* Secure HttpOnly cookies

---

## 📤 File Uploads

* Endpoint: `/api/v1/images/upload`
* Type: `multipart/form-data`
* Storage: Cloudinary

---

## 🗄️ Database

* PostgreSQL
* Flyway migrations:

```text
src/main/resources/db/migration
```

---

## 🧪 API Documentation

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

---

## 📊 Monitoring

* `/actuator/health`
* `/actuator/metrics`

---

## 🧱 Deployment Notes

* Multi-stage Docker build
* Runs as non-root user
* Logs: `/app/logs`
* Uploads: `/app/uploads`

---

## ⚠️ Important Notes

* JWT secret must be **Base64 encoded (≥32 chars)**
* Never commit `.env`
* Configure `COOKIE_DOMAIN` in production

---

## 👨‍💻 Author

Lost & Found Backend System
>>>>>>> Stashed changes
