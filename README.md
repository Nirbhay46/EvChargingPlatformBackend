# ⚡ EV Charging Platform

A production-grade microservices backend for managing EV charging stations, bookings, payments, and notifications — built with Spring Boot 3.5, Java 25, and Apache Kafka.

---

## 🏗️ Architecture

```
                  ┌──────────────────┐
                  │  Angular 22 SPA  │
                  └────────┬─────────┘
                           │
                  ┌────────▼─────────┐
                  │   API Gateway    │  :8080
                  │  JWT + Rate Limit│
                  └────────┬─────────┘
        ┌──────────────────┼──────────────────┐
        │                  │                  │
 ┌──────▼──────┐   ┌───────▼──────┐   ┌──────▼──────┐
 │   User      │   │   Station    │   │   Booking   │
 │  Service    │   │   Service    │◀──│   Service   │
 │  :8081      │   │  :8082       │   │  :8083      │
 └─────────────┘   └──────────────┘   └──────┬──────┘
                                              │ Kafka
                                   ┌──────────┴──────────┐
                                   │                     │
                            ┌──────▼──────┐      ┌──────▼────────┐
                            │  Payment    │      │ Notification  │
                            │  Service   │──────▶│  Service      │
                            │  :8084     │       │  :8085        │
                            └────────────┘       └───────────────┘
```

---

## 🧰 Tech Stack

| Layer | Tech |
|-------|------|
| Language | Java 25 |
| Framework | Spring Boot 3.5, Spring Cloud 2025.0 |
| Auth | JWT HS256 (jjwt 0.12), Spring Security, `@PreAuthorize` |
| Messaging | Apache Kafka |
| Database | MySQL 8.0 (DB-per-service) + Flyway migrations |
| Audit | Hibernate Envers (`*_aud` tables + `revinfo`) |
| Cache | Redis 7 (station read-path, gateway rate limiter) |
| Resilience | Resilience4j (Circuit Breaker, Retry, Bulkhead) |
| Tracing | Micrometer + Zipkin |
| Payments | Mock Stripe (no real keys) |
| Email | AWS SES via LocalStack |
| Frontend | Angular 22 |

---

## 📦 Services

| Service | Port | Responsibility |
|---------|------|----------------|
| api-gateway | 8080 | Routing, JWT validation, Redis rate limiting |
| user-service | 8081 | Register, login, JWT issuance, role management |
| station-service | 8082 | Station CRUD, Redis cache (60s TTL) |
| booking-service | 8083 | Slot reservation, double-booking prevention |
| payment-service | 8084 | Mock Stripe payment processing |
| notification-service | 8085 | Email via AWS SES (LocalStack) |

---

## 🔐 Security & Roles

JWT is validated at the gateway. Downstream services receive user info as headers:
- `X-User-Id` — user ID
- `X-User-Email` — email
- `X-User-Roles` — comma-separated roles

Each service reads these via `HeaderAuthFilter` (from `common-lib`) and builds a Spring Security context for `@PreAuthorize`.

### Roles

| Role | Access |
|------|--------|
| `ROLE_USER` | Create/view own bookings, payments, notifications |
| `ROLE_OPERATOR` | Create/update/delete stations |
| `ROLE_ADMIN` | Full access to everything |

### Public Endpoints (no token required)
- `POST /api/users/register`
- `POST /api/users/login`
- `GET  /api/stations/**`

---

## 🔄 Kafka Event Flow

```
POST /api/bookings
       │
       ▼
booking-service  ──► BOOKING_EVENTS ──► payment-service
                                              │
                              ┌───────────────┴──────────────┐
                              ▼                               ▼
                     PAYMENT_EVENTS                 NOTIFICATION_EVENTS
                              │                               │
                              ▼                               ▼
                     booking-service                notification-service
                   (CONFIRMED / FAILED)             (email via SES)
```

Topics: `booking-events`, `payment-events`, `notification-events`

---

## 🗄️ Databases

Each service has its own MySQL database created automatically on startup (`createDatabaseIfNotExist=true`):

| Service | Database |
|---------|----------|
| user-service | `userdb` |
| station-service | `stationdb` |
| booking-service | `bookingdb` |
| payment-service | `paymentdb` |
| notification-service | `notificationdb` |

Flyway runs migrations on startup:
- `V1__init.sql` — main tables
- `V2__envers_audit_tables.sql` — audit tables (`*_aud` + `revinfo`)

---

## 🚀 Running Locally

### Prerequisites
- Java 25
- Maven
- Docker Desktop

### Step 1 — Start Infrastructure

```bash
cd infra
docker compose up -d
```

This starts: MySQL (3307), Redis (6379), Kafka (9092), Zookeeper, Kafka UI (8090), Zipkin (9411), LocalStack (4566)

### Step 2 — Verify SES Identities (LocalStack)

```bash
export AWS_ACCESS_KEY_ID=test
export AWS_SECRET_ACCESS_KEY=test

aws --endpoint-url=http://localhost:4566 ses verify-email-identity \
  --email-address no-reply@evcharge.io --region ap-southeast-2

aws --endpoint-url=http://localhost:4566 ses verify-email-identity \
  --email-address admin@evcharge.io --region ap-southeast-2
```

### Step 3 — Build

```bash
cd backend
mvn clean package -DskipTests
```

### Step 4 — Run Services

Start each in a separate terminal:

```bash
cd backend/user-service         && mvn spring-boot:run
cd backend/station-service      && mvn spring-boot:run
cd backend/booking-service      && mvn spring-boot:run
cd backend/payment-service      && mvn spring-boot:run
cd backend/notification-service && mvn spring-boot:run
cd backend/api-gateway          && mvn spring-boot:run
```

### Step 5 — Full Docker Stack (alternative)

```bash
docker compose -f infra/docker-compose.full.yml up --build
```

---

## 🔑 Default Admin Credentials

Seeded automatically by `AdminSeeder` on user-service startup:

| Field | Value |
|-------|-------|
| Email | `admin@evcharge.io` |
| Password | `Admin@12345` |
| Roles | ADMIN, OPERATOR, USER |

---

## 📝 API Quick Reference

All requests go through the gateway on port `8080`.

### Auth
```bash
# Register
POST http://localhost:8080/api/users/register
{ "fullName": "John", "email": "john@x.com", "password": "Pass@123" }

# Login
POST http://localhost:8080/api/users/login
{ "email": "john@x.com", "password": "Pass@123" }
```

### Stations
```bash
# Create (OPERATOR/ADMIN)
POST http://localhost:8080/api/stations
Authorization: Bearer <token>
{
  "name": "CBD Hub", "city": "Sydney", "address": "123 George St",
  "latitude": -33.8688, "longitude": 151.2093,
  "totalSlots": 5, "powerKw": 50, "connectorType": "CCS",
  "pricePerKwh": "0.35", "currency": "AUD", "status": "ACTIVE"
}

# List (public)
GET http://localhost:8080/api/stations
```

### Bookings
```bash
# Create (USER/ADMIN)
POST http://localhost:8080/api/bookings
Authorization: Bearer <token>
{
  "stationId": 1,
  "startTime": "2026-06-20T10:00:00",
  "endTime": "2026-06-20T11:00:00",
  "idempotencyKey": "unique-key-001"
}

# My bookings
GET http://localhost:8080/api/bookings
Authorization: Bearer <token>
```

### Payments
```bash
GET http://localhost:8080/api/payments
Authorization: Bearer <token>
```

### Notifications
```bash
GET http://localhost:8080/api/notifications
Authorization: Bearer <token>
```

---

## 📊 Observability

| Tool | URL |
|------|-----|
| Zipkin (tracing) | http://localhost:9411 |
| Kafka UI | http://localhost:8090 |
| Health check | http://localhost:808x/actuator/health |
| Metrics (Prometheus) | http://localhost:808x/actuator/prometheus |
| SES sent emails | http://localhost:4566/_aws/ses |

---

## 🛡️ Resilience Patterns

- Circuit Breaker + Retry on booking→station Feign call
- Bulkhead + Retry on mock Stripe gateway
- `@Retryable` (5 attempts, exp. backoff) on Kafka producers
- Optimistic locking (`@Version`) on Booking entity
- Idempotency key (unique constraint) on bookings
- Dead Letter Topic (`.DLT`) for poison Kafka messages
- Redis token-bucket rate limiting at the gateway

---

## 🗂️ Project Structure

```
ev-charging-platform/
├── backend/
│   ├── common-lib/           # Shared: JWT util, Kafka events, HeaderAuthFilter, converters
│   ├── api-gateway/          # Spring Cloud Gateway + JWT filter + Redis rate limit
│   ├── user-service/         # Auth, registration, JWT issuance
│   ├── station-service/      # Station CRUD + Redis cache
│   ├── booking-service/      # Reservations + Feign + Kafka
│   ├── payment-service/      # Mock Stripe + Kafka
│   └── notification-service/ # SES email + Kafka
├── frontend/                 # Angular 22
└── infra/
    ├── docker-compose.yml         # Infrastructure only
    ├── docker-compose.full.yml    # Full stack (infra + services + frontend)
    ├── init-db.sql                # Creates all MySQL databases
    └── localstack-init/           # Auto-verifies SES identities on LocalStack startup
```

---

## 🧪 Tests

```bash
cd backend && mvn test
```

- Unit tests: JUnit 5 + Mockito for all service layers
- Integration: Testcontainers (MySQL, Kafka, Redis)
- Kafka end-to-end: `BookingKafkaIntegrationTest` with Awaitility
