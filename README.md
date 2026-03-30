# Contact Manager API

A RESTful API for managing contacts built with Spring Boot and Spring Security.
Includes JWT authentication, role-based access control, pagination, search, and full test coverage.

Built by [gui000-maker](https://github.com/gui000-maker)

---

## Tech Stack

- Java 17
- Spring Boot 3
- Spring Security + JWT (jjwt)
- Spring Data JPA + Hibernate
- PostgreSQL (production) / H2 (development + tests)
- Docker + Docker Compose
- Swagger / OpenAPI (springdoc)
- JUnit 5 + Mockito + Testcontainers
- Bucket4j (rate limiting)

---

## Features

- JWT authentication — register, login, refresh and logout
- Refresh token rotation — tokens are rotated on every use
- Role-based access control — `ROLE_USER` and `ROLE_ADMIN`
- Contact scoping — users can only access their own contacts
- Contact CRUD with pagination and name search
- User management restricted to admins
- Rate limiting on login — 5 attempts per minute per IP
- Input validation with structured JSON error responses
- Centralized exception handling
- API documentation via Swagger UI
- Unit tests, integration tests, and security tests
- Spring profiles — `dev` (H2), `prod` (PostgreSQL), test (H2)
- Docker Compose for containerized deployment

---

## Getting Started

### Option 1 — Docker (recommended)

Requires Docker installed and running. Uses PostgreSQL via Docker Compose.
No local database setup needed.

```bash
git clone https://github.com/gui000-maker/contactmanager.git
cd contactmanager
docker compose up --build
```

| | URL |
|---|---|
| API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui/index.html |

### Option 2 — Local Development (H2)

No database installation required. Uses H2 in-memory database.
Data is lost when the application stops.

```bash
git clone https://github.com/gui000-maker/contactmanager.git
cd contactmanager
./gradlew bootRun --args='--spring.profiles.active=dev'
```

| | URL |
|---|---|
| API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui/index.html |
| H2 Console | http://localhost:8080/h2-console |

### Option 3 — Local Development (PostgreSQL)

Requires PostgreSQL installed and running locally.

1. Create a database named `contactmanager`
2. Configure credentials in `src/main/resources/application.properties`
3. Run with the prod profile:

```bash
./gradlew bootRun --args='--spring.profiles.active=prod'
```

---

## Spring Profiles

| Profile | Database | How to activate | Use for |
|---------|----------|-----------------|---------|
| `dev` | H2 in-memory | `--spring.profiles.active=dev` | Local development, quick testing |
| `prod` | PostgreSQL | Set via `SPRING_PROFILES_ACTIVE=prod` | Docker, production deployment |
| *(none)* | H2 in-memory | `./gradlew test` | Running tests |

**Property files:**

```
src/main/resources/
├── application.properties          — shared config (JWT, logging)
├── application-dev.properties      — H2 datasource, H2 console enabled
└── application-prod.properties     — PostgreSQL datasource via environment variables

src/test/resources/
└── application.properties          — overrides main config during tests (H2, test JWT secret)
```

---

## API Endpoints

### Auth — public

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/auth/register | Register a new user |
| POST | /api/auth/login | Login and receive JWT token |

### Contacts — requires authentication

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /contacts | Get all contacts (paginated) |
| POST | /contacts | Create a new contact |
| GET | /contacts/{id} | Get contact by ID |
| PUT | /contacts/{id} | Update contact by ID |
| DELETE | /contacts/{id} | Delete contact by ID |
| GET | /contacts/search?name= | Search contacts by name |

### Users — requires ROLE_ADMIN

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /users | Get all users (paginated) |
| POST | /users | Create a new user |
| GET | /users/{id} | Get user by ID |
| DELETE | /users/{id} | Delete user by ID |

---

## Authentication

Register and login to receive a JWT token, then include it in all protected requests:

```bash
# Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "john", "password": "password123"}'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "john", "password": "password123"}'

# Use the token
curl http://localhost:8080/contacts \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

---

## Error Responses

All errors return a consistent JSON structure:

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Contact not found with id: 10",
  "path": "/contacts/10",
  "timestamp": "2026-03-22T15:00:00"
}
```

| Status | Meaning |
|--------|---------|
| 400 | Validation failed |
| 401 | Missing or invalid JWT token |
| 403 | Authenticated but insufficient role |
| 404 | Resource not found |
| 409 | Conflict (e.g. duplicate username) |
| 429 | Too many requests (rate limit exceeded) |
| 500 | Unexpected server error |

---

## Running Tests

No Docker required — tests use H2 in-memory database automatically.

```bash
./gradlew test
```

### Test coverage includes

- Service unit tests (happy path + exception cases)
- Controller unit tests (MockMvc with standaloneSetup)
- Repository tests (custom queries with @DataJpaTest)
- JWT service unit tests (token generation and validation)
- Security integration tests (401, 403, public endpoints)

---

## Environment Variables

Used when running with Docker or the `prod` profile.

| Variable | Description | Example |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `prod` |
| `SPRING_DATASOURCE_URL` | PostgreSQL JDBC URL | `jdbc:postgresql://postgres:5432/contactmanager` |
| `SPRING_DATASOURCE_USERNAME` | Database username | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `postgres` |
| `JWT_SECRET` | JWT signing secret (min 32 characters) | `your-secret-key-here` |
| `JWT_EXPIRATION` | Token expiry in milliseconds | `86400000` |

---

## Project Structure

```
src/main/java/com/example/contactmanager/
├── auth/           — login and register controllers
├── config/         — OpenAPI config
├── controller/     — REST controllers
├── service/        — business logic
├── repository/     — data access layer
├── entity/         — JPA entities
├── dto/            — request and response objects
├── exception/      — global exception handler and custom exceptions
├── security/       — JWT filter, JWT service, security config
└── swagger/        — shared API documentation annotations

src/main/resources/
├── application.properties          — shared base config
├── application-dev.properties      — H2 for local development
└── application-prod.properties     — PostgreSQL for production

src/test/
├── java/                           — unit and integration tests
└── resources/application.properties — H2 config for test runs
```

---

## License

MIT

