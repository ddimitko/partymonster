# Practice - Party Events API

A Spring Boot 4 prototype for OTP-based authentication and party event ticketing.
This repository also serves as a DevOps practice project with Docker and Kubernetes
artifacts alongside the application code.

## What it does
- Email OTP login with bearer token authentication
- Profile completion required before hosting or buying tickets
- Create, list, and fetch events; purchase tickets with capacity checks
- H2 in-memory database by default, PostgreSQL supported via env vars
- Actuator health endpoint and OpenTelemetry export hooks

## Tech stack
- Java 25, Spring Boot 4, Spring Security
- Spring Data JPA, H2, PostgreSQL
- Docker, Docker Compose, Kubernetes manifests (kind)

## Quick start (local)
1) Run the app with the embedded H2 database:
```bash
./gradlew bootRun
```

2) Confirm health:
```bash
curl http://localhost:8080/actuator/health
```

## Local development with Docker Compose
Compose brings up Postgres, Grafana LGTM (OTel), and Redis (not used yet).

```bash
docker build -t partymonster:dev .
docker compose up
```

Set database and mail env vars in `.env` or your shell. See the Environment section
for defaults.

## Authentication flow
1) Request an OTP (sent via SMTP):
```bash
curl -X POST http://localhost:8080/auth/request-otp \
  -H "Content-Type: application/json" \
  -d '{"email":"you@example.com"}'
```

2) Verify the OTP to get a bearer token:
```bash
curl -X POST http://localhost:8080/auth/verify-otp \
  -H "Content-Type: application/json" \
  -d '{"email":"you@example.com","code":"123456"}'
```

3) Complete profile (required before hosting or buying tickets):
```bash
curl -X POST http://localhost:8080/auth/complete-profile \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Ada Lovelace","displayName":"Ada","phone":"+1-555-0100"}'
```

## API endpoints
All `/events` endpoints require `Authorization: Bearer <token>`.

- `POST /auth/request-otp` request an OTP
- `POST /auth/verify-otp` verify OTP and receive token
- `POST /auth/complete-profile` complete profile for the logged-in user
- `GET /auth/me` current user profile
- `GET /events` list events
- `GET /events/{id}` get event details
- `POST /events` create event
- `POST /events/{id}/tickets` buy tickets
- `GET /actuator/health` health check (no auth)

### Create event example
```bash
curl -X POST http://localhost:8080/events \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title":"Rooftop Party",
    "description":"Live DJ and snacks",
    "location":"123 Main St",
    "startTime":"2025-01-01T20:00:00Z",
    "price":25.00,
    "capacity":100
  }'
```

### Purchase tickets example
```bash
curl -X POST http://localhost:8080/events/1/tickets \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"quantity":2}'
```

## Environment
Defaults are in `src/main/resources/application.properties`. Override as needed:

- `SPRING_DATASOURCE_URL` (default H2 in-memory)
- `SPRING_DATASOURCE_DRIVER`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `SPRING_MAIL_HOST` (default `localhost`)
- `SPRING_MAIL_PORT` (default `1025`)
- `SPRING_MAIL_USERNAME`
- `SPRING_MAIL_PASSWORD`
- `SPRING_MAIL_SMTP_AUTH`
- `SPRING_MAIL_SMTP_STARTTLS`
- `SPRING_MAIL_SSL_ENABLED`
- `APP_MAIL_FROM`
- `MANAGEMENT_OTLP_METRICS_EXPORT_ENABLED`
- `MANAGEMENT_OTLP_METRICS_EXPORT_URL`

## Tests
```bash
./gradlew test
```

## Kubernetes notes
- `deployment.yaml` is a minimal deployment stub.
- `kind-config.yaml` provides a local kind cluster with port mappings for 8080/8443.
