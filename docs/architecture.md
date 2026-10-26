# TreasuryHub architecture

> **Diagrams (living):** keep [architecture-diagram.md](./architecture-diagram.md) in sync whenever topology, ports, services, or infrastructure change.

## Why a monorepo?

TreasuryHub keeps all services in one Maven reactor so local tooling stays simple: one clone, one `mvn clean verify`, shared BOM/parent versions, and coordinated learning steps. Each module remains an independently runnable and deployable Spring Boot application. There are **no** Java dependencies between services.

## Database-per-service

Every service owns its schema and PostgreSQL database:

| Service | Database |
| --- | --- |
| customer-service | `customer_db` |
| account-service | `account_db` |
| payment-service | `payment_db` |

**Why:** ownership boundaries stay clear, schemas can evolve independently, and operational failures in one database do not take down every domain.

**Rule:** a service must never read or write another service’s database. Cross-domain needs go through APIs or (later) events — never shared tables.

## Local runtime split

Infrastructure (PostgreSQL, Kafka, Kafka UI) runs in Docker Compose. Spring Boot apps initially run from IntelliJ or Maven.

**Why:** containers give reproducible databases and brokers; IDE/Maven runs keep breakpoints, hot reload, and fast iteration without rebuilding images for every code change.

## Kafka (present, unused)

Kafka is provisioned now so payment-service can later publish payment lifecycle events without reinventing infrastructure. No topics, producers, consumers, or listeners are implemented yet. Serializer defaults exist only so the payment service can start cleanly when the broker is up.

## Intentionally postponed

These are **out of scope** for the foundation and will be added only when the need is clear:

- API Gateway
- Service discovery (Eureka, Consul, etc.)
- Centralized Config Server
- Kubernetes
- Spring Security / OAuth
- Distributed tracing
- Resilience libraries (circuit breakers, bulkheads)
- Redis
- Shared common libraries

Starting without them keeps the learning path focused on service boundaries, data ownership, Flyway, and async messaging readiness.

## Current local topology

Direct client-to-service HTTP calls are **temporary** for local development. A gateway will likely sit in front later.

Canonical diagrams (context, ports, Compose, current vs planned communication) live in [architecture-diagram.md](./architecture-diagram.md).

## Planned future capabilities

- Company and user registration / verification (customer-service)
- Corporate accounts, balances, reservations, debit/credit (account-service)
- Beneficiaries, payment creation, approval workflows, status tracking (payment-service)
- Inter-service calls (e.g. payment-service → account-service)
- Asynchronous payment events on Kafka
- Notifications, retries, idempotency, and partial-failure handling
- Reporting / transaction history views
- Security, gateway routing, observability, and deployment hardening

## Technology baseline

- Java 17, Maven, Spring Boot 3.x
- Spring Web, Validation, Data JPA, Actuator
- PostgreSQL + Flyway (`ddl-auto: validate`)
- Spring Kafka (payment-service dependency only for now)
- JUnit 5, Mockito, Testcontainers
- Docker Compose for local infrastructure
