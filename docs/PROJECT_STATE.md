# TreasuryHub — Project State

Updated: 2026-11-21

## Goal

Learn practical Java/Spring Boot banking microservices through focused tickets, tests, failure analysis, and review. Favor explicit service ownership and depth over feature count.

## Current architecture

- Maven multimodule monorepo; Java 17 target; Spring Boot 3.5.4.
- Customer Service (`8081`) owns company and corporate-user identity in `customer_db` (`5433`).
- Account Service (`8082`) owns bank accounts in `account_db` (`5434`).
- Payment Service (`8083`) owns payment workflow data in `payment_db` (`5435`); no business HTTP routes yet.
- Kafka (`9092`) and Kafka UI (`8090`) are provisioned, but no business producers or consumers exist.
- Centralized browse-only Swagger UI runs on `8091`.
- Grafana (`3000`), Tempo (`3200`), Prometheus (`9090`), Loki (`3100`), and Alloy (`4317`, `4318`, `12345`) provide local observability.
- Applications run on the Windows host from IntelliJ/Maven; infrastructure runs through Docker Compose.

## Implemented business behavior

- Company registration is implemented with validation, Flyway-managed schema, duplicate handling, and tests.
- Corporate-user registration code exists, but its HTTP route remains deliberately blocked with `403` until corporate ADMIN authorization is implemented.
- Basic account creation and lookup are implemented with validation and error handling.
- Payment persistence exists, but payment workflow endpoints and Kafka workflows are not implemented.
- No service reads or writes another service's database, and no cross-service API calls exist.

## TH-OBS-02 observability — pending developer review

Branch: `feature/TH-OBS-02-observability`

### Phase A — application instrumentation

- Micrometer Prometheus registry, OpenTelemetry tracing bridge, OTLP exporter, and Spring AOP enabled in all services.
- Distinct service resources, configurable sampling, W3C propagation, HTTP tracing, selected `@Observed` business spans, and compatible JDBC tracing.
- Local Prometheus endpoints and HTTP latency histograms enabled.
- ECS JSON files include trace/span identifiers and use bounded rotation.
- Test profile disables OTLP export; observability configuration tests cover service identity and Prometheus exposure.
- JDBC query parameters are not captured. Experimental per-query JDBC metrics are disabled because the compatible library can emit inconsistent Prometheus label sets; standard Hikari pool metrics remain.

### Phase B — Grafana infrastructure

- Pinned Alloy, Tempo, Prometheus, and Grafana images in Compose.
- Tempo service-graph/span-metric generation writes to Prometheus.
- Prometheus scrapes host applications through `host.docker.internal`.
- Grafana provisions Prometheus, Tempo, and the TreasuryHub Overview dashboard automatically.

### Phase C — centralized logging

- Pinned Loki image with seven-day local retention.
- Alloy tails gitignored host ECS JSON files through a read-only Windows-to-Docker bind mount.
- Grafana provisions Loki, Tempo-to-logs navigation, and Loki log-to-trace derived fields.

## Verification on 2026-11-21

- `mvnw.cmd clean test`: **34 tests passed**, zero failures/errors/skips.
- `docker compose config --quiet`: passed.
- All existing and new observability containers started; health checks passed.
- All three applications started against their existing PostgreSQL databases.
- All three `/actuator/prometheus` endpoints returned `200`; Prometheus reported all three targets `up=1`.
- Centralized Swagger returned `200`; application OpenAPI tests passed.
- Corporate-user POST remained blocked with `403`.
- A sample company and account were created successfully without cross-service calls.
- Tempo returned the sample HTTP trace with its business and sanitized JDBC spans.
- Tempo generated real service/database and external-caller service-graph series; no fake service-to-service edges were introduced.
- Loki returned the application log stream for the same trace ID.
- Grafana reported healthy, provisioned all three data sources, and loaded the TreasuryHub Overview dashboard.

## Known limitations

- No cross-service HTTP or Kafka business traffic exists, so the Service Graph cannot show Customer → Account → Payment edges.
- Local observability is single-node and intended for learning, not high availability.
- Local trace sampling defaults to 100%; production-like loads should configure a lower value.
- Grafana's default local credentials must not be reused outside local development.
- The interface/implementation consistency refactor requested separately is preserved in a Git stash and is not mixed into TH-OBS-02.

## Next work

1. Developer review of TH-OBS-02; do not commit, push, or merge until approved.
2. Add real trace-propagating HTTP/Kafka interactions only when their business tickets require them.
3. Implement corporate ADMIN authentication/authorization before enabling corporate-user registration.
4. Continue account/ledger and payment workflow tickets with transaction, idempotency, concurrency, and ledger-invariant review.
