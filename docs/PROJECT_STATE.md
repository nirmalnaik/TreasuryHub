# TreasuryHub — Project State
Updated: 2026-10-26

## Goal
Learn practical Java/Spring Boot banking microservices via short GitHub issues, Codex-assisted implementation, deliberate failure scenarios, tests, and interview reviews. Favor depth over feature count.

## Current architecture
- Maven monorepo; Java 17 target, Spring Boot 3.5.4.
- customer-service (8081; customer_db / PostgreSQL 5433)
- account-service (8082; account_db / PostgreSQL 5434)
- payment-service (8083; payment_db / PostgreSQL 5435)
- Docker Compose: three PostgreSQL instances, Kafka (9092), Kafka UI (8090).
- Flyway migrations per service. No implemented business APIs, JPA domain entities, inter-service clients, Kafka producers/consumers or security at the 2026-10-26 audit.

## Verified milestones
- Local Git initialized; initial baseline commit `9c9d35f`; branch `main` was clean.
- GitHub remote connected, per developer confirmation.
- Docker infrastructure: 3 PostgreSQL containers and Kafka healthy; Kafka UI running (`docker ps`).
- Customer-service Docker integration test: user reports one passing test.
- Earlier audit: Maven test/verify passed; integration test could not run at audit time because Docker was inaccessible then.

## Next work
1. Confirm whether minimal CI exists; add it only if missing.
2. TH-002: corporate customer onboarding vertical slice (Flyway, entity, repository, service, controller, validation, errors, PostgreSQL tests).
3. TH-003: account opening and lifecycle.

## Engineering principles
- Customer Service owns company identity; Account Service owns accounts/ledger; Payment Service owns payment requests and approval/execution workflow.
- Each service owns its database. Cross-service access through contracts, not another service's tables.
- Financial transfers require ledger invariants, atomic posting, idempotency, and concurrency safeguards before claims of correctness.
- Keep 15 core tickets and 3 stretch tickets as a tentative roadmap, not a rigid schedule.
- Do not add gateway, Kubernetes, Redis, service mesh or extra services without demonstrated need.

## Handoff at end of each ticket
Update this file with ticket ID, branch/PR, changes, commands/tests and outcomes, known failures, decisions, and next ticket. Reference issue/PR links rather than duplicating code or lengthy discussions.
