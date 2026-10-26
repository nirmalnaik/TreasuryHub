# TreasuryHub — Codex Working Agreement

- Act as an AI pair programmer; the developer owns decisions and must understand the code.
- Work only on the requested GitHub issue/ticket. Keep changes small and focused.
- Read relevant existing code and architecture docs before editing; do not scan unrelated files unless necessary.
- Preserve service/database boundaries. Customer owns company identity; Account owns accounts and ledger; Payment owns payment workflow.
- Use Flyway for schema changes, validations and clear API errors. Add tests for behavior, edge cases, and failures.
- Never claim financial correctness without checking transaction boundaries, idempotency, concurrency, and ledger invariants.
- Do not add dependencies, services, or infrastructure speculatively.
- Do not commit, push, merge, or rewrite Git history unless explicitly requested.
- Summarize changed files, decisions, executed tests/results, unresolved issues, and what the developer should understand.
- Keep responses and prompts concise. After completing a ticket, propose short updates to docs/PROJECT_STATE.md and docs/LEARNING_TRACKER.md.
