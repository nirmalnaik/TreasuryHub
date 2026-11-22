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

Infrastructure (PostgreSQL, Kafka, Kafka UI, Swagger UI, and the observability stack) runs in Docker Compose. Spring Boot apps run from IntelliJ or Maven.

**Why:** containers give reproducible databases and brokers; IDE/Maven runs keep breakpoints, hot reload, and fast iteration without rebuilding images for every code change.

## Kafka (present, unused)

Kafka is provisioned now so payment-service can later publish payment lifecycle events without reinventing infrastructure. No topics, producers, consumers, or listeners are implemented yet. Serializer defaults exist only so the payment service can start cleanly when the broker is up.

## Observability

All services use Micrometer with the OpenTelemetry bridge. HTTP requests, selected business methods, and JDBC operations create spans; OTLP traces flow from the Windows host to Grafana Alloy and then Tempo. Prometheus scrapes each host application's local Actuator endpoint through `host.docker.internal`. Tempo generates service-graph and span metrics back into Prometheus.

Applications also write ECS JSON logs with trace and span identifiers to a gitignored host directory. Alloy tails that Docker-mounted directory and sends records to Loki. Grafana provisions Prometheus, Tempo, and Loki automatically and links traces to logs in both directions.

The service graph represents observed traffic only. Current relationships are external caller → service and service → its own PostgreSQL database. There are no Customer → Account → Payment edges because no cross-service workflow exists. See [observability.md](./observability.md) for operation and troubleshooting.

## Intentionally postponed

These are **out of scope** for the foundation and will be added only when the need is clear:

- API Gateway
- Service discovery (Eureka, Consul, etc.)
- Centralized Config Server
- Kubernetes
- Full authentication and authorization (OAuth/OIDC)
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
- Security, gateway routing, and deployment hardening

## Technology baseline

- Java 17, Maven, Spring Boot 3.x
- Spring Web, Validation, Data JPA, Actuator
- PostgreSQL + Flyway (`ddl-auto: validate`)
- Spring Kafka (payment-service dependency only for now)
- Micrometer, OpenTelemetry, Prometheus, Alloy, Tempo, Loki, and Grafana
- JUnit 5, Mockito, Testcontainers
- Docker Compose for local infrastructure
