# Transaction Monitoring Platform

An event-driven, microservices-based fraud and AML (Anti-Money Laundering) monitoring platform built in Java and Spring Boot. The system ingests financial transactions, evaluates them against configurable business rules, and routes suspicious activity to compliance analysts for investigation — with a fully immutable audit trail across the pipeline.

This is **not** a banking system or payment gateway. Transactions are assumed to originate externally (simulated via REST for development purposes); the platform's responsibility begins after a transaction has occurred.

---

## Architecture

The platform is composed of independent microservices, each owning its own database, communicating exclusively through **Apache Kafka**. There are no synchronous inter-service calls — every cross-service interaction is an asynchronous, versioned event.

```
┌─────────────────────┐        ┌──────────────────────┐        ┌────────────────────────┐
│  Transaction Service │──────▶ │  Rule Engine Service  │──────▶ │ Case Management Service │
│                      │ Kafka  │                       │ Kafka  │                         │
│  own DB: transaction │        │  own DB: rule_engine  │        │  own DB: case_mgmt      │
└─────────────────────┘        └──────────────────────┘        └────────────────────────┘
           │                              │                               │
           │                              │                               │
           ▼                              ▼                               ▼
                          ┌───────────────────────────────┐
                          │         Audit Service          │
                          │   own DB: audit (append-only)  │
                          └───────────────────────────────┘
```

### Services

| Service | Responsibility | Publishes | Consumes |
|---|---|---|---|
| **Transaction Service** | Receives, validates, and persists incoming transactions | `transactions.created.v1` | — |
| **Rule Engine Service** | Evaluates transactions against fraud/AML rules via the Strategy pattern; computes a risk score | `transactions.flagged.v1` | `transactions.created.v1` |
| **Case Management Service** | Creates investigation cases from flagged transactions; analysts approve/dismiss/escalate | `cases.created.v1`, `cases.updated.v1` | `transactions.flagged.v1` |
| **Audit Service** | Maintains an immutable, append-only log of every event across the platform | — | all events |

### Design Principles

- **Database-per-service** — each service owns its schema exclusively; no service reaches into another's database.
- **Event-driven only** — Kafka is the sole integration point; no REST calls between services.
- **Strategy pattern for rule logic** — fraud/AML rules (Threshold, Velocity, Structuring) are pluggable strategies, not `if/else` chains, so new rules can be added without touching the executor.
- **Versioned event contracts** — every event is named and shaped explicitly (`transactions.created.v1`), allowing schemas to evolve without breaking consumers.
- **Rules evolve from hardcoded to data-driven** — initial rule thresholds are hardcoded; a later milestone migrates them to database-backed configuration without changing the executor's logic.
- **Production-grade reliability patterns** — idempotent consumers, dead-letter queues, and the Outbox pattern are introduced deliberately as the platform matures, rather than bolted on from day one.

---

## Tech Stack

- **Language / Framework:** Java 21, Spring Boot
- **Messaging:** Apache Kafka
- **Persistence:** Spring Data JPA / Hibernate, MySQL
- **Migrations:** Flyway
- **Security:** Spring Security — JWT authentication, role-based access control (`ANALYST`, `ADMIN`, `SYSTEM`)
- **Build:** Maven
- **Infrastructure:** Docker Compose (local development)

---

## Repository Structure

This is a monorepo — each service is a self-contained Maven module living as a sibling folder, sharing a single Docker Compose file for local infrastructure.

```
transactions-monitoring-platform/
├── transaction-service/
│   └── transaction-service/
│       ├── src/main/java/...
│       ├── src/main/resources/
│       │   ├── application.yaml
│       │   └── db/migration/
│       └── pom.xml
├── rule-engine-service/
│   ├── src/main/java/...
│   ├── src/main/resources/
│   │   ├── application.yaml
│   │   └── db/migration/
│   └── pom.xml
├── case-management-service/       (planned)
├── audit-service/                 (planned)
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

This brings up MySQL instances for each service and a single shared Kafka broker (KRaft mode, no Zookeeper).

### 2. Run database migrations

Each service manages its own schema independently via Flyway:

```bash
cd transaction-service/transaction-service
mvn flyway:migrate

cd ../../rule-engine-service
mvn flyway:migrate
```

### 3. Run a service

```bash
mvn spring-boot:run
```

Each service runs on its own port to avoid collisions locally (e.g. Transaction Service on `8080`, Rule Engine Service on `8082`).

---

## Event Flow (current)

1. A transaction is submitted to **Transaction Service** via REST (`POST /api/transactions/create`).
2. Transaction Service validates, persists it, and publishes `transactions.created.v1` to Kafka.
3. **Rule Engine Service** consumes the event, records it in its local transaction history, and evaluates it against all registered rule strategies.
4. Each strategy produces a risk score and verdict; the executor aggregates these into an overall decision and persists the full evaluation (including which rules triggered and why).
5. If the aggregate risk crosses the configured threshold, Rule Engine Service publishes `transactions.flagged.v1`.
6. *(Planned)* Case Management Service consumes flagged events and opens investigation cases for analysts.
7. *(Planned)* Audit Service consumes every event across the platform to build an immutable compliance trail.

---

## Roadmap

The platform is being built incrementally across eight milestones, each independently demoable:

- [x] **Milestone 1** — Transaction Service (standalone, REST + persistence)
- [x] **Milestone 2** — Kafka integration (event publishing from Transaction Service)
- [ ] **Milestone 3** — Rule Engine Service (Kafka consumer, Strategy-pattern rule evaluation) — *in progress*
- [ ] **Milestone 4** — Case Management Service
- [ ] **Milestone 5** — Audit Service
- [ ] **Milestone 6** — Security (JWT authentication, RBAC)
- [ ] **Milestone 7** — Production hardening (idempotency, DLQ, Outbox pattern)
- [ ] **Milestone 8** — Full containerization of all services

---

## License

This is a personal learning and portfolio project. No license has been applied yet.
