# Transaction Monitoring Platform

[![CI](https://github.com/emran-youssef/transactions_monitoring/actions/workflows/ci.yml/badge.svg)](https://github.com/emran-youssef/transactions_monitoring/actions/workflows/ci.yml)


An event-driven, microservices-based fraud and AML (Anti-Money Laundering) monitoring platform built in Java and Spring Boot. The system ingests financial transactions, evaluates them against configurable business rules, and routes suspicious activity to compliance analysts for investigation — with a fully immutable audit trail across the pipeline.

This is **not** a banking system or payment gateway. Transactions are assumed to originate externally (simulated via REST for development purposes); the platform's responsibility begins after a transaction has occurred.

---

## Architecture

The platform is composed of independent microservices, each owning its own database, communicating exclusively through **Apache Kafka**. There are no synchronous inter-service calls between services — every cross-service interaction is an asynchronous, versioned event.

All external client traffic enters through a single **API Gateway**, which centralizes JWT authentication before routing to the two REST-facing services.


```
                                                                        ┌──────────┐
                                                                        │  Client  │
                                                                        └────┬─────┘
                                                                             │ REST (HTTPS + JWT)
                                                                             ▼
                                                                   ┌───────────────────┐
                                                                   │    API Gateway      │
                                                                   │  (JWT validation)   │
                                                                   └─────────┬──┬────────┘
                                                                 REST        │  │        REST
                                                              ┌──────────────┘  └──────────────┐
                                                              ▼                                 ▼
                                                ┌──────────────────────┐          ┌───────────────────────────┐
                                                │  Transaction Service   │          │  Case Management Service   │
                                                │  DB: transaction_db    │          │  DB: case_mgmt_db          │
                                                └───────────┬─────────────┘          └─────────────▲───────────────┘
                                                            │ publishes                              │ consumes
                                                            │ transactions.created.v1                │ transactions.flagged.v1
                                                            ▼                                         │
                                                ╔═══════════════════════════════════════════════════════╗
                                                ║               Apache Kafka (KRaft mode)                 ║
                                                ╚═══════════════════════════════════════════════════════╝
                                                            │                                         ▲
                                                            │ consumes                                │ publishes
                                                            │ transactions.created.v1                 │ transactions.flagged.v1
                                                            ▼                                         │
                                                ┌──────────────────────────┐
                                                │    Rule Engine Service     │──────────────────────────┘
                                                │    DB: rule_engine_db      │
                                                └──────────────────────────┘
                                          
                                                Audit Service consumes ALL four topics from Kafka:
                                                transactions.created.v1 · transactions.flagged.v1
                                                cases.created.v1 · cases.updated.v1
                                          
                                                              ┌──────────────────────────────┐
                                                              │         Audit Service           │
                                                              │   DB: audit_db (append-only)    │
                                                              └──────────────────────────────┘

```

### Services

| Service | Responsibility | Publishes | Consumes | Exposes REST |
|---|---|---|---|---|
| **API Gateway** | Validates JWTs at the edge; routes authenticated requests; injects trusted identity headers | — | — | Yes (entry point) |
| **Transaction Service** | Receives, validates, and persists incoming transactions | `transactions.created.v1` | — | Yes (behind gateway) |
| **Rule Engine Service** | Evaluates transactions against fraud/AML rules via the Strategy pattern; computes a risk score | `transactions.flagged.v1` | `transactions.created.v1` | No |
| **Case Management Service** | Creates investigation cases from flagged transactions; analysts approve/dismiss/escalate | `cases.created.v1`, `cases.updated.v1` | `transactions.flagged.v1` | Yes (behind gateway) |
| **Audit Service** | Maintains an immutable, append-only log of every event across the platform | — | all events | No |

### Design Principles

- **Database-per-service** — each service owns its schema exclusively; no service reaches into another's database.
- **Event-driven only** — Kafka is the sole integration point between backend services; no REST calls between services.
- **Strategy pattern for rule logic** — fraud/AML rules (Threshold, Velocity, Structuring) are pluggable strategies, not `if/else` chains, so new rules can be added without touching the executor.
- **Versioned event contracts** — every event is named and shaped explicitly (`transactions.created.v1`), allowing schemas to evolve without breaking consumers.
- **Centralized authentication, distributed authorization** — the API Gateway validates JWTs once at the edge and injects trusted identity headers (`X-User-Id`, `X-User-Roles`); each service still enforces its own fine-grained, per-action role checks (`@PreAuthorize`) and business-context authorization (e.g. case ownership), since those decisions require domain data the gateway doesn't have.
- **Production-grade reliability patterns** — idempotent consumers, dead-letter queues, and the Outbox pattern are used across all publishing services to guarantee at-least-once delivery without dual-write inconsistency.

---

## Security

All client traffic goes through the **API Gateway** (port `8888`), which is the only externally-facing entry point:

1. The gateway validates the JWT's signature, issuer, and expiry using a shared HMAC-SHA256 secret.
2. Requests with a missing, invalid, or expired token are rejected immediately with a `401` and a JSON error body — they never reach a downstream service.
3. On success, the gateway strips the original `Authorization` header and injects `X-User-Id` and `X-User-Roles` headers before forwarding the request.
4. Downstream services (Transaction Service, Case Management Service) no longer parse or validate JWTs themselves — they trust the gateway-injected headers to populate the Spring Security context.
5. Each service still enforces its own **per-action role checks** (`@PreAuthorize`) and **ownership-based authorization** (e.g. verifying a case belongs to the requesting analyst), since these require business context the gateway doesn't have.

`/auth/login` (served by Transaction Service, routed through the gateway) is the only unauthenticated endpoint.

> Note: in local development, services are still individually reachable on their own ports — network isolation forcing all traffic through the gateway is not yet enforced. This is a known gap, planned for when the platform is deployed rather than run locally.

---

## Tech Stack

- **Language / Framework:** Java 21, Spring Boot 4
- **API Gateway:** Spring Cloud Gateway (WebFlux)
- **Messaging:** Apache Kafka (KRaft mode)
- **Persistence:** Spring Data JPA / Hibernate, MySQL
- **Migrations:** Flyway
- **Security:** Spring Security — JWT authentication (jjwt), role-based access control (`ANALYST`, `ADMIN`, `SYSTEM`)
- **Build:** Maven
- **Infrastructure:** Docker Compose (local development — infrastructure only; services run locally via IDE)

---

## Testing

Unit test coverage currently spans two services:

- **Rule Engine Service** — pure JUnit 5 tests for all three rule strategies (`ThresholdRule`, `VelocityRule`, `StructuringRule`), plus Mockito-based tests for `RuleEvaluationService`
- **Case Management Service** — Mockito-based tests for `CaseCreationService` and `CaseWorkflowService`

Tests run automatically on every push and pull request via GitHub Actions (see badge above). Coverage does not yet extend to Transaction Service, Audit Service, or API Gateway, and integration-level testing (e.g. Testcontainers against real Kafka/MySQL) is a planned next step, not yet implemented.

---


## Repository Structure

This is a monorepo — each service is a self-contained Maven module living as a sibling folder, sharing a single Docker Compose file for local infrastructure.

```
transactions-monitoring-platform/
├── api-gateway/
│   ├── src/main/java/.../security/
│   │   ├── JwtValidator.java
│   │   └── JwtAuthenticationGlobalFilter.java
│   ├── src/main/resources/
│   │   └── application.yaml
│   └── pom.xml
├── transaction-service/
│   ├── src/main/java/...
│   ├── src/main/resources/
│   │   ├── application.yaml
│   │   └── db/migration/
│   └── pom.xml
├── rule-engine-service/
│   ├── src/main/java/...
│   ├── src/main/resources/
│   │   ├── application.yaml
│   │   └── db/migration/
│   └── pom.xml
├── case-management-service/
├── audit-service/
├── docker-compose.yml
└── README.md
```

---

## Getting Started

### Prerequisites

- Java 21 (JDK)
- Maven
- Docker & Docker Compose

### 1. Start local infrastructure

```bash
docker compose up -d
```

This brings up MySQL instances for each service and a single shared Kafka broker (KRaft mode, no Zookeeper). Services themselves run locally via your IDE, not in Docker.

### 2. Run database migrations

Each service manages its own schema independently via Flyway:

```bash
cd transaction-service
mvn flyway:migrate

cd ../rule-engine-service
mvn flyway:migrate

cd ../case-management-service
mvn flyway:migrate

cd ../audit-service
mvn flyway:migrate
```

### 3. Run the services

Each service runs on its own port to avoid collisions locally:

| Service | Port |
|---|---|
| API Gateway | `8888` |
| Transaction Service | `8080` |
| Rule Engine Service | `8082` |
| Case Management Service | `8083` |
| Audit Service | `8085` |

```bash
mvn spring-boot:run
```

Run each service, then the API Gateway last. **All client requests should go through the gateway (`http://localhost:8888`), not directly to individual service ports.**

---

## Event Flow (current)

1. A client authenticates via `POST http://localhost:8888/auth/login` and receives a JWT.
2. A transaction is submitted via `POST http://localhost:8888/api/transactions/create` with `Authorization: Bearer <token>`.
3. The **API Gateway** validates the token, strips it, and forwards the request to Transaction Service with `X-User-Id`/`X-User-Roles` headers.
4. **Transaction Service** validates, persists it, and publishes `transactions.created.v1` to Kafka (via the Outbox pattern).
5. **Rule Engine Service** consumes the event, records it in its local transaction history, and evaluates it against all registered rule strategies.
6. Each strategy produces a risk score and verdict; the executor aggregates these into an overall decision and persists the full evaluation (including which rules triggered and why).
7. If the aggregate risk crosses the configured threshold, Rule Engine Service publishes `transactions.flagged.v1`.
8. **Case Management Service** consumes flagged events and opens investigation cases for analysts. Analyst actions (assign/approve/escalate) are submitted via `http://localhost:8888/api/cases/**`, authenticated the same way as transactions.
9. **Audit Service** consumes all four topics (`transactions.created.v1`, `transactions.flagged.v1`, `cases.created.v1`, `cases.updated.v1`) and persists each as an immutable, append-only row — storing the raw event JSON verbatim, indexed by `entity_id` and `event_type` for traceability.

---

## Roadmap

The platform was originally planned across eight milestones; all are complete, and development has continued beyond the original scope:

- [x] **Milestone 1** — Transaction Service (standalone, REST + persistence)
- [x] **Milestone 2** — Kafka integration (event publishing from Transaction Service)
- [x] **Milestone 3** — Rule Engine Service (Kafka consumer, Strategy-pattern rule evaluation)
- [x] **Milestone 4** — Case Management Service
- [x] **Milestone 5** — Audit Service
- [x] **Milestone 6** — Security (JWT authentication, RBAC)
- [x] **Milestone 7** — Production hardening (idempotency, dead-letter queues, Outbox pattern across all publishing services)
- [x] **Milestone 8** — Full containerization of all services

### Beyond the original roadmap

- [x] **API Gateway** — centralized JWT validation at the edge; trusted-header identity propagation (`X-User-Id`, `X-User-Roles`) to downstream services


---

## License

This is a personal learning and portfolio project. No license has been applied yet.
