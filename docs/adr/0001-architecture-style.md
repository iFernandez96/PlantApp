# ADR-0001 — Architecture Style

Date: 2026-05-26
Status: Proposed

## Context

PlantApp needs to support deterministic care scheduling, AI-assisted diagnosis, offline reminders, photo storage, and growth from a single-user MVP to many users without re-architecting.

## Decision

Adopt a **modular monolith on the backend + clean-architecture Android client**, with a strict separation between a **pure care-engine** and all I/O.

- **Backend:** one deployable service initially, organized into well-bounded modules (`api`, `care-engine`, `ai-gateway`, `weather`, `notifications`, `storage`). Modules communicate via in-process interfaces today; can be split into services later if scale demands.
- **Care engine:** a pure module with no I/O, no network, no clock dependency injected by reference. Deterministic and exhaustively unit-tested. Available to backend (primary) and optionally to Android as a Kotlin library if we choose to compute reminders locally; for MVP, server-authoritative.
- **Android:** clean architecture — `domain` (use cases + models) ← `data` (repos, Room, network) ← `feature-*` UI modules. Hilt for DI. Compose for UI.
- **Shared contracts:** JSON Schemas in `shared-schemas/` are the cross-boundary types. Codegen on both sides.

## Alternatives considered

- **Microservices from day one.** Rejected: premature; adds ops cost with no scale need.
- **AI-driven scheduling (let the LLM decide watering).** Rejected: violates the deterministic-engine rule and is hard to test or explain.
- **Pure Firebase BaaS, no custom backend.** Rejected: AI gateway must hide provider keys and validate structured outputs; that responsibility doesn't fit cleanly in client SDKs.

## Consequences

- We commit to discipline around module boundaries inside the monolith.
- Care engine is portable: backend today, optionally Android tomorrow.
- Adding new bounded contexts (sensors, community) is a module-add, not a re-architecture.
