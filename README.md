# Payment Processing System — Spring Boot

High-throughput payment processor: idempotency, distributed sagas, Outbox pattern, Resilience4j, Kafka.

---

## Quickest start (zero dependencies — no Redis, no Kafka)

### Requirements

- **Java 21+** → https://adoptium.net
- **Maven 3.9+** → https://maven.apache.org/download.cgi

### Run

```bash
# From inside the payment-system folder:
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

App starts on http://localhost:8080

H2 console at http://localhost:8080/h2-console

**JDBC URL:** `jdbc:h2:mem:paymentsdb`  
**User:** `sa`  
**Password:** blank

---

## Run in IntelliJ IDEA

1. **File → Open** → select the `payment-system` folder
2. Wait for Maven to import (bottom status bar)
3. Open **PaymentSystemApplication.java**
4. Click **Edit Configurations** → **Edit Configurations**
5. Under **Active profiles** enter: `local`
6. Click ▶ **Run**

---

## API Endpoints

### Create a payment

```http
POST http://localhost:8080/api/v1/payments
Content-Type: application/json

{
  "idempotencyKey": "order-001",
  "amount": 250.00,
  "currency": "USD",
  "payerId": "payer-001",
  "payeeId": "payee-001",
  "description": "Order #001"
}
```

### Get a payment

```http
GET http://localhost:8080/api/v1/payments/{id}
```

### Payment history for a payer

```http
GET http://localhost:8080/api/v1/payments/payer/payer-001?page=0&size=10
```

### Live stats

```http
GET http://localhost:8080/api/v1/payments/stats
```

---

## Test accounts (pre-seeded)

| **Account** | **Balance** |
|-------------|-------------:|
| payer-001   | $10,000.00   |
| payer-002   | $5,000.00    |
| payee-001   | $0.00        |
| payee-002   | $0.00        |

---

## Run tests

```bash
mvn test -Dspring.profiles.active=local
```

---

## Full stack (Kafka + Redis + PostgreSQL)

```bash
# Start everything
docker-compose up -d

# Watch application logs
docker-compose logs -f app

# Tear everything down
docker-compose down -v
```

---

## Production Deployment — Railway

The application is deployed as a production Spring Boot service on **Railway** with **PostgreSQL**.

### Production architecture

```text
Client
  │
  ▼
Railway
  │
  ├── Spring Boot Application
  │       │
  │       └── PostgreSQL
  │
  └── HTTPS Public URL
```

### Production profile

Railway runs the application using the `prod` Spring profile.

The production configuration uses Railway's PostgreSQL connection through the `DATABASE_URL` environment variable:

```yaml
spring:
  datasource:
    url: ${DATABASE_URL}
```

Kafka-dependent components are disabled in the production profile so the application can run on Railway without requiring a separate Kafka deployment.

### Live application

**Production URL:**

https://payment-processing-system-production-d41a.up.railway.app

### Live API endpoints

#### Live payment statistics

```http
GET https://payment-processing-system-production-d41a.up.railway.app/api/v1/payments/stats
```

#### Create a live payment

```http
POST https://payment-processing-system-production-d41a.up.railway.app/api/v1/payments
Content-Type: application/json

{
  "idempotencyKey": "live-001",
  "amount": 100.00,
  "currency": "USD",
  "payerId": "payer-001",
  "payeeId": "payee-001",
  "description": "Live recruiter test"
}
```

### Railway services

| **Service** | **Purpose** |
|-------------|-------------|
| Spring Boot Application | Runs the payment processing API |
| PostgreSQL | Production relational database |

### Environment variables

The application uses Railway service-reference variables rather than hard-coded database credentials.

```text
DATABASE_URL=${{Postgres.DATABASE_URL}}
SPRING_PROFILES_ACTIVE=prod
```

> Database credentials are managed by Railway and are not stored in the GitHub repository.

---

## Key design patterns

| **Concern** | **Solution** |
|-------------|--------------|
| Duplicate requests | In-memory / Redis idempotency (SET NX) |
| Dual-write safety | Transactional Outbox pattern |
| Distributed failures | Saga orchestration + compensation |
| Concurrent writes | `@Version` optimistic locking |
| Downstream failures | Resilience4j circuit breaker + fallback |
| Message redelivery | Consumer-side dedup table |

---

## Technology Stack

| **Category** | **Technology** |
|--------------|----------------|
| Language | Java 21 |
| Framework | Spring Boot |
| Database | H2 / PostgreSQL |
| Messaging | Apache Kafka |
| Cache | Redis |
| Resilience | Resilience4j |
| ORM | Spring Data JPA / Hibernate |
| Build Tool | Maven |
| Containerization | Docker |
| Deployment | Railway |

---

## Project Highlights

- Idempotent payment creation
- Transactional Outbox pattern for reliable event publishing
- Saga-based distributed transaction handling
- Optimistic locking for concurrent account updates
- Resilience4j circuit breaker and fallback handling
- Consumer-side event deduplication
- Local development without Kafka or Redis
- PostgreSQL production deployment
- Docker-based full-stack development
- RESTful payment APIs