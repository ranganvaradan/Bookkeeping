# BillionTech Bookkeeping Platform

Automated bookkeeping platform for CPA firms.

## Tech Stack

| Layer      | Technology                              |
|------------|----------------------------------------|
| Frontend   | React 18, Vite, TypeScript, TailwindCSS |
| Backend    | Java 21, Spring Boot 3.2, Maven         |
| Database   | PostgreSQL 15                           |
| Cache      | Redis 7                                 |
| External   | QuickBooks Online API, Anthropic Claude  |

## Project Structure

```
Bookkeeping/
├── frontend/          # React + Vite + TypeScript + TailwindCSS
├── backend/           # Spring Boot + Maven
├── docker-compose.yml # Full-stack orchestration
├── .env.example       # Required environment variables
└── README.md
```

## Prerequisites

- **Docker** & **Docker Compose** (recommended for full-stack)
- **Java 21** (for backend development)
- **Maven 3.9+** (or use the included `mvnw` wrapper)
- **Node.js 20+** & **npm** (for frontend development)

## Quick Start (Docker Compose)

1. **Clone the repository**
   ```bash
   git clone https://github.com/ranganvaradan/Bookkeeping.git
   cd Bookkeeping
   ```

2. **Create the `.env` file**
   ```bash
   cp .env.example .env
   # Edit .env with your actual values (at minimum set DB_PASSWORD)
   ```

3. **Start all services**
   ```bash
   docker compose up --build
   ```

4. **Verify**
   - Frontend: http://localhost:3010
   - Backend health: http://localhost:8085/api/health → `{ "status": "ok" }`
   - PostgreSQL: `localhost:5433` (user: `bt_user`, db: `billiontech`)
   - Redis: `localhost:6380`

## Local Development (without Docker)

### Backend

```bash
cd backend
./mvnw spring-boot:run
```
The API starts on **port 8085**. Requires PostgreSQL and Redis running locally
(or via `docker compose up postgres redis`).

### Frontend

```bash
cd frontend
npm install
npm run dev
```
The dev server starts on **port 3010** and proxies `/api` requests to `localhost:8085`.

## Port Mapping

| Service    | Host Port | Container Port |
|------------|-----------|----------------|
| Frontend   | 3010      | 3010           |
| Backend    | 8085      | 8085           |
| PostgreSQL | 5433      | 5432           |
| Redis      | 6380      | 6379           |

> Inside the Docker network, services communicate using their service names
> and default ports (e.g., the backend connects to `postgres:5432`).

## API Endpoints

| Method | Path          | Description     |
|--------|---------------|-----------------|
| GET    | `/api/health` | Health check    |

## Environment Variables

See [`.env.example`](.env.example) for the full list of required variables.
