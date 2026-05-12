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
H2 console at http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:mem:paymentsdb`, user: `sa`, password: blank)

---

## Run in IntelliJ IDEA

1. **File → Open** → select the `payment-system` folder
2. Wait for Maven to import (bottom status bar)
3. Open **PaymentSystemApplication.java**
4. Click **Edit Configurations** (top-right dropdown → Edit Configurations)
5. Under **Active profiles** enter: `local`
6. Click ▶ Run

---

## API Endpoints

### Create a payment
```
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
```
GET http://localhost:8080/api/v1/payments/{id}
```

### Payment history for a payer
```
GET http://localhost:8080/api/v1/payments/payer/payer-001?page=0&size=10
```

### Live stats
```
GET http://localhost:8080/api/v1/payments/stats
```

---

## Test accounts (pre-seeded)

| Account     | Balance     |
|-------------|-------------|
| payer-001   | $10,000.00  |
| payer-002   | $5,000.00   |
| payee-001   | $0.00       |
| payee-002   | $0.00       |

---

## Run tests
```bash
mvn test -Dspring.profiles.active=local
```

---

## Full stack (Kafka + Redis + PostgreSQL)
```bash
docker-compose up -d          # start everything
docker-compose logs -f app    # watch logs
docker-compose down -v        # tear down
```

---

## Key design patterns

| Concern | Solution |
|---|---|
| Duplicate requests | In-memory / Redis idempotency (SET NX) |
| Dual-write safety | Transactional Outbox pattern |
| Distributed failures | Saga orchestration + compensation |
| Concurrent writes | @Version optimistic locking |
| Downstream failures | Resilience4j circuit breaker + fallback |
| Message redelivery | Consumer-side dedup table |
