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

3. **Start the application:**
   `docker compose up -d --build`

**That's it for now!** - The API will be available at `http://localhost:8080`
- Swagger UI Documentation: `http://localhost:8080/swagger-ui.html`
- The database is exposed on port `5433` if you want to connect via DBeaver/DataGrip.

To view the real-time application logs, run:
`docker compose logs -f app`
