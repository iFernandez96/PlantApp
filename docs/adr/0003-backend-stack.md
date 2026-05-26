# ADR-0003 — Backend Stack

Date: 2026-05-26
Status: Proposed

## Context

We need a backend that:
- Owns auth, inventory storage, and signed-URL photo storage.
- Hosts the deterministic care-engine module.
- Hosts the AI gateway as the **only** caller of LLM providers.
- Dispatches notifications.
- Is cheap and operable for a single-user MVP, but not a dead end at growth.

## Decision (initial — to be confirmed before slice 3)

- **Database, auth, storage:** Supabase.
  - Postgres for relational data with **row-level security per `userId` on every table**.
  - Supabase Storage for photos, private buckets, signed URLs with short TTL.
  - Supabase Auth as the JWT issuer (OAuth + email magic link to start).
- **API runtime:** **Node.js + TypeScript** API service deployable to a host that supports background jobs and outbound calls (e.g. Vercel Functions / Fly.io / Render — to be selected when first deploy lands). Kotlin/Ktor backend remains an option if we want Kotlin/JVM symmetry with the care-engine; deferred until we measure JS engine performance on care-engine table evaluations.
- **Care-engine:** TypeScript module within the API repo. Pure functions, exhaustive unit tests, no DB access in the engine itself. **For Slice 1 this is the only implementation of the engine** (decision log D-09); Android reads tasks from the backend rather than computing them locally. A Kotlin port for Android offline scheduling is deferred to a later slice (likely Slice 3 or 4) and will share a JSON test-vector suite with the TypeScript engine to prevent drift.
- **AI gateway:** TypeScript service module. Owns OpenAI SDK use, schema validation, retry, telemetry, quota.
- **Notifications:** worker process that consumes a `due_tasks` queue/table and dispatches via FCM.
- **Weather + USDA PHZM adapters:** thin TS modules with per-zip caching tables.
- **Migrations:** Supabase migrations or Prisma/Drizzle — picked at slice-1 implementation; pinned then in this ADR.

## Alternatives considered

- **Firebase end-to-end.** Rejected: weak SQL story for the relational plant/care model; AI gateway still needs custom code somewhere.
- **Kotlin/Ktor server.** Strong candidate — gives true care-engine code reuse between Android and backend. Deferred to revisit because TypeScript yields faster MVP velocity and the JSON-Schema-defined contracts neutralize most language-lock-in.
- **Python (FastAPI).** Rejected for primary API; reconsidered if heavy ML/CV processing moves server-side beyond LLM gateway.

## Consequences

- Care-engine implemented twice if Android computes locally later. JSON Schemas + a shared test-vector suite mitigate drift.
- We rely on Supabase RLS as a defense-in-depth on top of application-layer auth checks.
- Migrating off Supabase later is feasible (it's Postgres) but storage and Auth would require lift.

## Open

- Hosting choice for the API runtime (Vercel Functions, Fly, Render). To be selected when slice 3 (deterministic watering reminders) lands.
- Migration tool (Supabase migrations CLI vs Prisma vs Drizzle).
- Background-job runner (DB-as-queue with `pg_cron`/listen-notify vs a dedicated queue).
