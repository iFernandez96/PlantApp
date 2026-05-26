# PlantApp — Architecture

Status: Draft v0.1 — 2026-05-26

## 1. High-level diagram (text)

```
┌──────────────────────────────────────────────────────────────────┐
│                       Android App (Kotlin)                       │
│  Compose UI · Hilt · Room (local cache) · DataStore · WorkManager│
│  CameraX · FCM client · Retrofit/Ktor HTTP client                │
└───────────────┬──────────────────────────────────────────────────┘
                │ HTTPS (REST + signed photo URLs)
                ▼
┌──────────────────────────────────────────────────────────────────┐
│                     Backend API (HTTP gateway)                   │
│   - Auth (JWT via Supabase)                                      │
│   - Inventory CRUD                                               │
│   - Care-engine service (deterministic)                          │
│   - Weather aggregator (NWS / Open-Meteo / USDA PHZM)            │
│   - AI gateway (OpenAI Responses API + structured outputs)       │
│   - Notification worker (FCM + scheduled jobs)                   │
└─────┬──────────────────┬──────────────────────┬─────────────────-┘
      │                  │                      │
      ▼                  ▼                      ▼
┌───────────┐    ┌──────────────────┐    ┌──────────────────┐
│ Postgres  │    │  Object Storage  │    │  External APIs   │
│ (Supabase)│    │ (Supabase / S3)  │    │ NWS, Open-Meteo, │
│ schemas:  │    │ photos, signed   │    │ USDA PHZM, OpenAI│
│ inventory │    │ URLs, short TTL  │    │                  │
│ care      │    └──────────────────┘    └──────────────────┘
│ ai_logs   │
└───────────┘
```

## 2. Modules

### 2.1 Android (`/android` — future)

Modules grow per slice. Targets at the end of MVP; Slice 1 starts with the **minimum** subset (see `docs/slice-01-implementation-plan.md` and ADR-0002).

- `:app` — Compose UI, navigation, screens.
- `:design-system` — Material 3 tokens, typography, components.
- `:data` — Room entities, DataStore prefs, repository implementations.
- `:domain` — Use cases, models, ports.
- `:care-engine` — **(Deferred; not in Slice 1.)** Per decision log D-09 the care-engine lives only in the backend (TypeScript) for Slice 1. A pure-Kotlin Android port is added later — likely Slice 3 or 4 — when offline scheduling/reminders are required. The port will be paired with a shared JSON test-vector suite to prevent cross-language drift (ADR-0001).
- `:network` — Retrofit (per decision log D-02) or Ktor clients, DTOs derived from `shared-schemas/`.
- `:notifications` — (Slice 3) FCM + WorkManager.
- `:camera` — (Slice 7) CameraX with EXIF stripping.
- `:feature-inventory` (Slice 1), `:feature-care` (Slice 3+), `:feature-diagnosis` (Slice 7), `:feature-space` (Slice 8).

### 2.2 Backend (`/backend` — future)

- `api/` — REST endpoints. JWT verification.
- `care-engine/` — pure deterministic scheduler. Inputs: `(PlantInstance, PlantProfile, Container, GardenSpace, weather window, feedback log, clock)`. Outputs: `CareTask[]`.
- `ai-gateway/` — sole caller of LLM providers. Validates outputs against `shared-schemas/`. Stores prompt version, model, latency, token counts (no raw image bytes).
- `weather/` — adapters for NWS, Open-Meteo, USDA PHZM. Cached per zip/day.
- `notifications/` — FCM dispatch worker + scheduled job runner.
- `storage/` — signed-URL minting for photo upload/download.

### 2.3 Shared

- `shared-schemas/` — JSON Schemas. Source of truth for cross-boundary types. Both Android (codegen) and backend (validation) consume them.
- `prompts/` — versioned system prompts. Backend embeds them; not shipped to Android.
- `evals/` — golden inputs, expected outputs, scoring harness.

## 3. Data flow — watering reminder (deterministic)

1. User logs a watering or adds a plant.
2. Backend care-engine recomputes the next `CareTask` for that `PlantInstance` using the rule set + cached weather window.
3. Backend stores `CareTask(next_due_at, rationale, engine_version)`.
4. Backend schedules an FCM dispatch and/or returns the task to the client.
5. Android receives push or pulls on launch; WorkManager schedules a local reminder as fallback.
6. User acts (waters / skips / feedback). Android posts the feedback event; engine re-runs.

## 4. Data flow — AI photo diagnosis

1. Android captures photo, strips EXIF GPS, compresses, requests signed upload URL.
2. Android uploads to object storage.
3. Android POSTs `/diagnose` with the storage key and `PlantInstance` id.
4. Backend `ai-gateway` builds a prompt from `prompts/plant-diagnosis.system.md` (versioned) plus context (species, container, recent care log).
5. Backend calls OpenAI Responses API with structured-output schema `diagnosis-result.schema.json`.
6. Response is validated. On schema fail: retry once, then surface a typed error.
7. Validated diagnosis is persisted, returned to client. Any suggested follow-up tasks are presented to the user as **suggestions** that, on accept, are funneled through the care-engine's normal API.

## 5. Key boundaries and invariants

- The Android app never holds an OpenAI API key.
- The care-engine is a pure function. No I/O, no clock dependency injected by reference (clock is a parameter).
- All AI outputs cross the boundary as schema-validated structured JSON.
- All photos are referenced by storage key, never embedded in DB rows or logs.
- Engine version is stamped on every generated CareTask.

## 6. Caching

- Weather: per-zip, per-hour cache server-side. Stale-while-revalidate for offline.
- Hardiness zone: per-zip, indefinite (refresh on USDA dataset version change).
- AI responses: not cached by content; metadata logged for eval and forensic use.
- Android: Room as local cache for inventory, schedule, last 90 days of logs.

## 7. Failure modes

| Failure | Behavior |
|---|---|
| Backend unreachable | Local schedule from last sync continues; reminders fire from WorkManager. |
| Weather API down | Care-engine falls back to seasonal defaults flagged as `degraded: true`. |
| AI gateway down | Diagnosis returns typed `unavailable` error; no fallback to mock advice. |
| AI returns invalid JSON | One retry with stricter instruction, then typed `schema_error`. |
| FCM token revoked | Re-register on next app launch; local WorkManager covers the gap. |

## 8. Tech-stack summary (subject to ADRs)

- Android: Kotlin, Jetpack Compose, Material 3, Room, DataStore, WorkManager, CameraX, Hilt, Retrofit or Ktor client.
- Backend: Supabase (Postgres, Auth, Storage) + a thin API layer (runtime TBD in ADR-0003).
- AI: OpenAI Responses API with structured outputs; gateway-only.
- Observability: structured logs + metrics; no PII or photo bytes.
