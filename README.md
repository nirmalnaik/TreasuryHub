# TreasuryHub — corporate banking and cash-management microservices (learning platform)

TreasuryHub is a production-style Java microservices foundation for corporate banking and cash management. This repository currently provides the local development stack only: three independently runnable Spring Boot services, dedicated PostgreSQL databases, Kafka (KRaft), and Flyway — without business features yet.

## Architecture overview

Maven monorepo with three independently deployable services. Each service owns its database. Infrastructure (PostgreSQL × 3, Kafka, Kafka UI) runs in Docker Compose; applications run from Maven or IntelliJ for easy debugging.

See [docs/architecture.md](docs/architecture.md) for design rationale and [docs/architecture-diagram.md](docs/architecture-diagram.md) for the living architecture diagrams (update that file whenever topology changes).

## Services

| Service | Port | Database | Host DB port | Responsibility (future) |
| --- | --- | --- | --- | --- |
| `customer-service` | 8081 | `customer_db` | 5433 | Companies, users, verification |
| `account-service` | 8082 | `account_db` | 5434 | Accounts, balances, debit/credit |
| `payment-service` | 8083 | `payment_db` | 5435 | Payments, approvals, Kafka events |

## Prerequisites

- JDK 17+ (project targets Java 17; JDK 21 also works)
- Maven 3.9+ **or** the included Maven Wrapper (`mvnw` / `mvnw.cmd`)
- Docker Desktop (or Docker Engine + Compose v2)
- IntelliJ IDEA (optional, for IDE runs)

## Start Docker infrastructure

From the repository root:

```bash
docker compose up -d
```

PowerShell:

```powershell
docker compose up -d
```

## Check container health

```bash
docker compose ps
docker compose logs -f
```

PowerShell (same commands). Expect `customer-db`, `account-db`, `payment-db`, `kafka`, and `kafka-ui` to be running; databases should report healthy.

## Run each service from Maven

With infrastructure up:

```bash
./mvnw -pl customer-service spring-boot:run
./mvnw -pl account-service spring-boot:run
./mvnw -pl payment-service spring-boot:run
```

Windows PowerShell:

```powershell
.\mvnw.cmd -pl customer-service spring-boot:run
.\mvnw.cmd -pl account-service spring-boot:run
.\mvnw.cmd -pl payment-service spring-boot:run
```

Default profile is `local` (see each service’s `application.yml`).

## Run each service from IntelliJ

1. Open the repository root as a Maven project (import the root `pom.xml`).
2. Wait for Maven import to finish.
3. Run the main class for the service you need:
   - `com.treasuryhub.customer.CustomerServiceApplication`
   - `com.treasuryhub.account.AccountServiceApplication`
   - `com.treasuryhub.payment.PaymentServiceApplication`
4. Ensure Docker infrastructure is running first.
5. Active profile defaults to `local`; override with `-Dspring-boot.run.profiles=local` or a Run Configuration VM option / env `SPRING_PROFILES_ACTIVE=local` if needed.

## Health endpoints

- http://localhost:8081/actuator/health
- http://localhost:8082/actuator/health
- http://localhost:8083/actuator/health

Info endpoints: `/actuator/info` on the same ports.

## Kafka UI

- http://localhost:8090

Kafka for local apps: `localhost:9092` (configured in `payment-service` via `KAFKA_BOOTSTRAP_SERVERS`).

## Stop containers without deleting data

```bash
docker compose down
```

Named volumes are kept; database and Kafka data remain.

## Reset all local database and Kafka data

```bash
docker compose down -v
```

**Warning:** `-v` permanently removes the Compose named volumes (`customer-db-data`, `account-db-data`, `payment-db-data`, `kafka-data`). All local DB and Kafka data is deleted.

## Build the whole project

```bash
./mvnw clean verify
```

PowerShell:

```powershell
.\mvnw.cmd clean verify
```

## Run normal tests

Default Surefire runs exclude the `docker` JUnit group (Testcontainers tests):

```bash
./mvnw test
```

PowerShell:

```powershell
.\mvnw.cmd test
```

Context-load tests use the `test` profile and do **not** require local PostgreSQL or Kafka.

## Run the Docker-dependent Testcontainers integration test

Requires Docker. From the repository root:

```bash
./mvnw -pl customer-service -Pdocker-integration test
```

PowerShell:

```powershell
.\mvnw.cmd -pl customer-service -Pdocker-integration test
```

This runs `CustomerDatabaseIntegrationTest`, which starts a temporary PostgreSQL container, applies Flyway, and asserts `service_metadata` for `customer-service`.

## Docker image builds (optional)

Build from the **repository root** (monorepo context):

```bash
docker build -f customer-service/Dockerfile -t treasuryhub/customer-service .
docker build -f account-service/Dockerfile -t treasuryhub/account-service .
docker build -f payment-service/Dockerfile -t treasuryhub/payment-service .
```

Application containers are **not** included in `compose.yaml` yet.

## Common troubleshooting

```bash
# Status and health
docker compose ps
docker compose logs -f customer-db
docker compose logs -f kafka

# Restart infrastructure
docker compose restart

# Recreate a single service
docker compose up -d --force-recreate customer-db

# Inspect DB metadata after a service has started once
docker exec -it treasuryhub-customer-db psql -U treasury -d customer_db -c "SELECT * FROM service_metadata;"
docker exec -it treasuryhub-account-db psql -U treasury -d account_db -c "SELECT * FROM service_metadata;"
docker exec -it treasuryhub-payment-db psql -U treasury -d payment_db -c "SELECT * FROM service_metadata;"

# Port already in use
netstat -ano | findstr "8081 8082 8083 5433 5434 5435 9092 8090"
```

PowerShell port check:

```powershell
Get-NetTCPConnection -LocalPort 8081,8082,8083,5433,5434,5435,9092,8090 -ErrorAction SilentlyContinue
```

If Flyway fails on startup, confirm the matching DB container is healthy and credentials match `application-local.yml` (`treasury` / `treasury`).

If Kafka UI cannot list brokers, wait until `kafka` is healthy, then refresh http://localhost:8090.
