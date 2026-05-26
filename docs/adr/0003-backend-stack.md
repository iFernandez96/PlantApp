# ADR-0003 — Backend Stack

Date: 2026-05-26
Status: Accepted — 2026-05-26 (pins D-01, D-03, D-04, D-05, D-06, D-08, D-09 from `docs/slice-01-decision-log.md`)

## Context

We need a backend that:
- Owns auth, inventory storage, and signed-URL photo storage.
- Hosts the deterministic care-engine module.
- Hosts the AI gateway as the **only** caller of LLM providers.
- Dispatches notifications.
- Is cheap and operable for a single-user MVP, but not a dead end at growth.

## Decision

- **Database, auth, storage:** Supabase.
  - Postgres for relational data with **row-level security per `userId` on every table**.
  - Supabase Storage for photos, private buckets, signed URLs with short TTL (consumed from Slice 7 onward).
  - **Supabase Auth as the JWT issuer; Slice 1 enables email magic link only** (decision log D-05, accepted 2026-05-26). Additional providers may be added in later slices.
- **API runtime:** **Node.js + TypeScript** (decision log D-01, accepted 2026-05-26). Chosen for MVP velocity and ecosystem maturity around Ajv and the Supabase SDK; JSON Schemas in `shared-schemas/` keep us portable.
- **Hosting:** **Deferred until the first device-reachable deployment is needed** (decision log D-08, accepted 2026-05-26). Vercel Functions, Fly.io, and Render remain candidates; Vercel Functions is the leading default when the decision is forced.
- **Care-engine:** TypeScript module within the API repo. Pure functions, exhaustive unit tests, no DB access in the engine itself. **For Slice 1 this is the only implementation of the engine** (decision log D-09, accepted 2026-05-26); Android reads tasks from the backend rather than computing them locally. A Kotlin port for Android offline scheduling is deferred to a later slice (likely Slice 3 or 4) and will share a JSON test-vector suite with the TypeScript engine to prevent drift.
- **AI gateway:** TypeScript service module (not built in Slice 1). Owns OpenAI SDK use, schema validation, retry, telemetry, quota.
- **Notifications:** worker process that consumes a `due_tasks` queue/table and dispatches via FCM (not built in Slice 1).
- **Weather + USDA PHZM adapters:** thin TS modules with per-zip caching tables (not built in Slice 1).
- **Migrations:** **Supabase migrations CLI** (`supabase/migrations/*.sql`) (decision log D-03, accepted 2026-05-26).
- **Background-job runner:** **Deferred** (decision log D-04, accepted 2026-05-26). Slice 1 generates tasks synchronously inside the request handlers for `POST /plants` and `DELETE /plants`. A queue or `pg_cron`/`pg_notify` arrives no earlier than Slice 3.
- **Schema validation:** **Ajv (2020-12 mode)** on the backend; on Android the production code paths use kotlinx.serialization DTOs and `networknt/json-schema-validator` is used only in tests (decision log D-06, accepted 2026-05-26).

## Alternatives considered

- **Firebase end-to-end.** Rejected: weak SQL story for the relational plant/care model; AI gateway still needs custom code somewhere.
- **Kotlin/Ktor server.** Strong candidate — gives true care-engine code reuse between Android and backend. Deferred to revisit because TypeScript yields faster MVP velocity and the JSON-Schema-defined contracts neutralize most language-lock-in.
- **Python (FastAPI).** Rejected for primary API; reconsidered if heavy ML/CV processing moves server-side beyond LLM gateway.

## Consequences

- Care-engine implemented twice if Android computes locally later. JSON Schemas + a shared test-vector suite mitigate drift.
- We rely on Supabase RLS as a defense-in-depth on top of application-layer auth checks.
- Migrating off Supabase later is feasible (it's Postgres) but storage and Auth would require lift.

## Still open (post-acceptance)

- Hosting choice for the API runtime (Vercel Functions, Fly, Render) — deferred per D-08; revisit when the first device-reachable deployment is needed.
- Background-job runner concrete shape (`pg_cron`/listen-notify vs a dedicated queue) — deferred per D-04; revisit no earlier than Slice 3.
