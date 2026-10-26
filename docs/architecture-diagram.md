# TreasuryHub — Architecture Diagram

> **Living document.** Update this file whenever services, ports, databases, infrastructure, or communication paths change.
>
> Companion narrative: [architecture.md](./architecture.md)

**Last updated:** 2026-08-03 (local development foundation)

---

## How to maintain this file

When you change the project, update the matching section below:

| Change type | Update |
| --- | --- |
| New / renamed service | System context, ports table, monorepo diagram |
| New database or port | Ports & ownership table, runtime topology |
| New Docker Compose service | Infrastructure diagram, Compose inventory |
| New cross-service API or Kafka topic | Communication diagram (current vs planned) |
| Gateway, security, tracing added | Move items from “Postponed” into active diagrams |
| Business domain features land | Domain responsibilities section |

---

## 1. System context (current)

Temporary local development: clients call each service directly. No API gateway yet.

```mermaid
flowchart TB
    subgraph Clients["Clients — temporary direct access"]
        Dev["Developer / IntelliJ / curl / Postman"]
    end

    subgraph Apps["Spring Boot apps — run on host"]
        CS["customer-service<br/>:8081"]
        AS["account-service<br/>:8082"]
        PS["payment-service<br/>:8083"]
    end

    subgraph Infra["Docker Compose infrastructure"]
        CDB[("customer_db<br/>:5433")]
        ADB[("account_db<br/>:5434")]
        PDB[("payment_db<br/>:5435")]
        K["Kafka KRaft<br/>:9092"]
        KUI["Kafka UI<br/>:8090"]
    end

    Dev --> CS
    Dev --> AS
    Dev --> PS

    CS --> CDB
    AS --> ADB
    PS --> PDB
    PS -. configured, unused yet .-> K
    KUI --> K
```

---

## 2. Ports and ownership

| Component | Host port | Owns / purpose |
| --- | --- | --- |
| customer-service | 8081 | Customer domain HTTP + Actuator |
| account-service | 8082 | Account domain HTTP + Actuator |
| payment-service | 8083 | Payment domain HTTP + Actuator |
| customer-db | 5433 → 5432 | `customer_db` (customer-service only) |
| account-db | 5434 → 5432 | `account_db` (account-service only) |
| payment-db | 5435 → 5432 | `payment_db` (payment-service only) |
| kafka | 9092 | Broker for host apps (`localhost:9092`) |
| kafka (internal) | 9094 | In-compose listener (`kafka:9094`) |
| kafka-ui | 8090 | Topic / message inspection UI |

**Hard rule:** no service may read or write another service’s database.

---

## 3. Maven monorepo layout

```mermaid
flowchart TB
    Root["treasuryhub<br/>parent POM — packaging pom"]

    Root --> CS["customer-service"]
    Root --> AS["account-service"]
    Root --> PS["payment-service"]

    CS -.- CPkg["com.treasuryhub.customer"]
    AS -.- APkg["com.treasuryhub.account"]
    PS -.- PPkg["com.treasuryhub.payment"]

    note1["No Java dependencies between services"]
```

Each module is independently runnable and deployable (`spring-boot:run` or its own Dockerfile).

---

## 4. Service internals (shared shape)

All three services follow the same package layout. Payment-service also has `event` and `client` (empty for now).

```mermaid
flowchart LR
    subgraph Service["Each Spring Boot service"]
        direction TB
        App["*ServiceApplication"]
        Ctrl["controller"]
        Svc["service"]
        Repo["repository"]
        Ent["entity"]
        Dto["dto"]
        Cfg["config"]
        Exc["exception"]
    end

    Ctrl --> Svc
    Svc --> Repo
    Repo --> Ent
    Ctrl --> Dto
    App --> Cfg

    Flyway["Flyway<br/>db/migration"] --> DB[("Own PostgreSQL")]
    Repo --> DB
```

Schema ownership: Flyway migrations only. Hibernate `ddl-auto: validate`.

---

## 5. Local runtime & data flow

```mermaid
sequenceDiagram
    actor Dev as Developer
    participant App as Spring Boot service
    participant PG as Service PostgreSQL
    participant FW as Flyway
    participant K as Kafka

    Note over Dev,K: Infrastructure starts via docker compose up -d
    Dev->>App: spring-boot:run / IntelliJ
    App->>PG: JDBC connect
    App->>FW: migrate on startup
    FW->>PG: V1__initial_schema.sql
    App->>Dev: /actuator/health UP

    Note over App,K: payment-service only — broker config present,<br/>no producers/consumers/topics yet
    App-->>K: bootstrap-servers localhost:9092
```

---

## 6. Docker Compose inventory

```mermaid
flowchart TB
    subgraph compose["compose.yaml — infrastructure only"]
        CDB["customer-db<br/>postgres:16-alpine"]
        ADB["account-db<br/>postgres:16-alpine"]
        PDB["payment-db<br/>postgres:16-alpine"]
        K["kafka<br/>apache/kafka:3.9.0 KRaft"]
        KUI["kafka-ui<br/>provectuslabs/kafka-ui"]
    end

    CDB --- V1[(customer-db-data)]
    ADB --- V2[(account-db-data)]
    PDB --- V3[(payment-db-data)]
    K --- V4[(kafka-data)]
    KUI -->|bootstrap kafka:9094| K
```

Application containers are **not** in Compose yet; apps run from the host for easy debugging.

---

## 7. Communication — current vs planned

```mermaid
flowchart LR
    subgraph today["Current"]
        C1[Client] --> CS1[customer-service]
        C1 --> AS1[account-service]
        C1 --> PS1[payment-service]
        PS1 -. no events yet .-> K1[Kafka]
    end

    subgraph later["Planned"]
        C2[Client] --> GW[API Gateway]
        GW --> CS2[customer-service]
        GW --> AS2[account-service]
        GW --> PS2[payment-service]
        PS2 -->|HTTP| AS2
        PS2 -->|payment events| K2[Kafka]
        K2 --> N[Future consumers<br/>notifications / audit]
    end
```

---

## 8. Domain responsibilities (future business)

Documentation only — not implemented yet.

```mermaid
mindmap
  root((TreasuryHub))
    customer-service
      Company profiles
      Corporate users
      Verification status
    account-service
      Corporate bank accounts
      Balances and status
      Reserve / debit / credit
    payment-service
      Payment creation
      Approval workflow
      Beneficiaries
      Status tracking
      Account-service calls
      Kafka events
```

---

## 9. Intentionally postponed

Keep these **out** of the active diagrams until they are introduced:

- API Gateway
- Service discovery (Eureka, Consul, …)
- Config Server
- Kubernetes
- Spring Security / OAuth
- Distributed tracing
- Resilience libraries
- Redis
- Shared common library

When any of these land, add them to sections 1, 2, 6, and 7 and remove them from this list.

---

## 10. Quick health map

| URL | Expect |
| --- | --- |
| http://localhost:8081/actuator/health | customer-service UP |
| http://localhost:8082/actuator/health | account-service UP |
| http://localhost:8083/actuator/health | payment-service UP |
| http://localhost:8090 | Kafka UI |
