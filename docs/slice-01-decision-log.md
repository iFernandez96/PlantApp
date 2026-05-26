# Slice 1 — Decision Log (Proposed)

Status: **Accepted (all entries)** — 2026-05-26
Date opened: 2026-05-26
Date accepted: 2026-05-26 (D-01 through D-12 accepted by owner)

This log captures the architecture decisions that were unpinned in ADR-0001…0004 at the start of Slice 1. Each entry lists the accepted choice, the reason, and a recorded fallback. ADR-0002 and ADR-0003 have been updated to reflect the pins; this log remains the canonical record of when and why each pin was made.

---

## D-01 — API runtime (ADR-0003)

**Accepted pin (2026-05-26):** Node.js + TypeScript.

**Why:** Fastest MVP velocity, large ecosystem for Ajv/JSON-Schema validation, mature Supabase SDK, easy deploy on Vercel/Fly/Render. JSON schemas in `shared-schemas/` neutralize most language lock-in if we migrate later.

**Fallback:** Kotlin/Ktor backend (lets the care-engine be a single shared module). Reconsider when the engine grows or when we measure latency.

---

## D-02 — Android HTTP client (ADR-0002)

**Accepted pin (2026-05-26):** **Retrofit + OkHttp + kotlinx.serialization converter.**

**Why:** Highest-stability ergonomics on Android, very well documented, simpler interceptors for auth-token refresh and structured logging filters. Ktor client is fine but Retrofit's tooling story (logging, testing, mock web server) is more battle-tested for a single-platform app. We are not currently planning a Kotlin Multiplatform client.

**Fallback:** Ktor client. Re-evaluate if/when KMP enters the picture.

---

## D-03 — Database migrations (ADR-0003)

**Accepted pin (2026-05-26):** **Supabase migrations CLI** (`supabase/migrations/*.sql`).

**Why:** Native to the chosen DB layer; one tool to learn; reproducible across dev/prod; works with Supabase branching.

**Fallback:** Drizzle migrations if we want type-safe schemas in TS code paths.

---

## D-04 — Background-job runner (ADR-0003)

**Accepted pin (2026-05-26):** **Defer.** Slice 1 produces tasks synchronously on `POST /plants` and reads them on `GET /plants/:id/tasks`. No queue, no scheduler, no cron needed until Slice 3.

**Why:** YAGNI. Pinning this in Slice 1 invites premature infrastructure.

**Fallback (later):** `pg_cron` + `pg_listen`/`pg_notify` on Supabase; consider a dedicated queue only when message volume justifies it.

---

## D-05 — Auth flow (ADR-0003)

**Accepted pin (2026-05-26):** **Supabase Auth, email magic link only** for Slice 1.

**Why:** Minimal UI surface, no password storage, no third-party OAuth setup required. Sufficient for single-user MVP.

**Fallback:** Add Google OAuth in Slice 2 if magic-link UX is annoying on a daily-use device.

---

## D-06 — JSON Schema validators (ADR-0002 / ADR-0003)

**Accepted pin (2026-05-26):**
- Backend: **Ajv** (TS), 2020-12 mode.
- Android: **hand-written DTOs** with **kotlinx.serialization** + a small assertion layer; full JSON-Schema runtime validation only in tests, where we use `networknt/json-schema-validator` to validate round-trip.

**Why:** Avoids bundling a full JSON-Schema validator into the Android APK; relies on compile-time DTO types in production code paths.

**Fallback:** If a slice introduces dynamic schemas client-side, add the runtime validator.

---

## D-07 — Crash reporting (ADR-0002)

**Accepted pin (2026-05-26):** **Defer to Slice 3.** Slice 1 ships in-app only on a single device; manual issue capture is acceptable.

**Why:** Avoid integrating Firebase Crashlytics for a single-user pre-release.

**Fallback:** Add Crashlytics or Sentry when notifications begin (Slice 3).

---

## D-08 — API hosting (ADR-0003)

**Accepted pin (2026-05-26):** **Defer until first deploy is needed.** Slice 1 development is local. The hosting decision (Vercel Functions vs Fly.io vs Render) does not need to be made until we want to install the app on the device with a real backend reachable from outside dev.

**Why:** Doesn't affect Slice 1 code structure. Adds reversal risk if chosen too early.

**Fallback:** When needed, start with Vercel Functions for TS API (lowest setup) and revisit if cold-starts hurt UX.

---

## D-09 — Care-engine location (single source vs duplicated)

**Accepted pin (2026-05-26):** **Backend-only for Slice 1.** Engine lives in `backend/care-engine/` (TS). Android does not compute schedules locally — it reads tasks from the backend.

**Why:** Single source of truth for Slice 1; avoids cross-language drift before there's a real need for offline scheduling.

**Fallback:** When offline scheduling is required (likely Slice 3 or 4), reintroduce a Kotlin port and pair both implementations with a shared JSON test-vector suite.

---

## D-10 — Care-engine v0.1.0 watering formula

**Accepted pin (2026-05-26, revised from earlier draft):**

```
wateringBaselineAt = plant.lastWateredAt ?? plant.createdAt
containerFactor    = clamp(container.volumeLiters
                           / profile.containerProfile.recommendedMinLiters,
                           0.5, 1.5)
dueAt              = wateringBaselineAt
                     + profile.wateringProfile.baseIntervalDays × containerFactor
priority           = "normal"
engineVersion      = "0.1.0"
sourceInputs       = { plantInstanceId, profileId, profileVersion,
                       containerId, gardenSpaceId, clockUtc,
                       wateringBaselineAt,
                       weatherWindowRef: null, feedbackWindowRef: null }
inputsHash         = sha256(canonical-json(sourceInputs))
rationale          = "<species common name>: base interval {baseIntervalDays}d
                      adjusted by container factor {containerFactor};
                      baseline {wateringBaselineAt}"
```

**Why:** Honest onboarding for existing plants. A user who adds a tomato they watered this morning should not get a reminder tomorrow as if it were watered the moment the row was inserted. `lastWateredAt` is the user-supplied baseline; when absent, the engine falls back to `createdAt` for new transplants or unknown history. Including `wateringBaselineAt` in `sourceInputs` keeps the input set complete for `inputsHash` reproducibility and forensic tracing.

**Note on long-term source of truth:** `lastWateredAt` is an **onboarding baseline only**. Slice 4 introduces `CareLogEvent` as the long-term source of truth; from Slice 4 onward the engine derives the baseline from log history rather than from a frozen field on the plant.

**Fallback:** If owner review wants a minimum dueAt floor (e.g. never sooner than 6h after `clockUtc`), add a clamp in the same patch and bump to `0.1.1`.

---

## D-11 — Slice 1 photo handling

**Accepted pin (2026-05-26):** **No photos at all in Slice 1.** Schemas allow a `photos` array on `PlantInstance` and `GardenSpace`, but the Slice 1 UI does not capture or upload images. CameraX is not added as a dependency yet.

**Why:** Photos are sensitive data; their pipeline (EXIF stripping, signed URLs, storage TTL) is a real piece of work and belongs to Slice 7 alongside AI diagnosis.

**Fallback:** None. This is locked.

---

## D-12 — Slice 1 location handling

**Accepted pin (2026-05-26):** **Postal code only.** `GardenSpace.postalCode` is optional in Slice 1 (the engine doesn't use it yet). No GPS, no background location, no location permissions in the Android manifest.

**Why:** Privacy minimization. Hardiness-zone resolution is a Slice 6 concern.

**Fallback:** None. This is locked.

---

## Approval — 2026-05-26

All twelve decisions accepted by the owner.

| Decision | Pin | Status |
|---|---|---|
| D-01 | Node.js + TypeScript API runtime | Accepted 2026-05-26 |
| D-02 | Retrofit + OkHttp + kotlinx.serialization on Android | Accepted 2026-05-26 |
| D-03 | Supabase migrations CLI | Accepted 2026-05-26 |
| D-04 | No background-job runner in Slice 1 (defer) | Accepted 2026-05-26 |
| D-05 | Supabase Auth, email magic link only | Accepted 2026-05-26 |
| D-06 | Ajv on backend; handwritten DTOs + kotlinx.serialization on Android (networknt only in tests) | Accepted 2026-05-26 |
| D-07 | Crash reporting deferred to Slice 3 | Accepted 2026-05-26 |
| D-08 | API hosting deferred until first deploy | Accepted 2026-05-26 |
| D-09 | Care-engine backend-only for Slice 1 | Accepted 2026-05-26 |
| D-10 | Care-engine v0.1.0 watering formula with `wateringBaselineAt` | Accepted 2026-05-26 |
| D-11 | No photos in Slice 1 | Accepted 2026-05-26 |
| D-12 | Postal code only; no precise location in Slice 1 | Accepted 2026-05-26 |

Follow-through completed in the same session:
- `docs/adr/0002-android-stack.md` updated with D-02, D-06, D-07.
- `docs/adr/0003-backend-stack.md` updated with D-01, D-03, D-04, D-05, D-06, D-08, D-09.
- D-10, D-11, D-12 are reflected in `docs/slice-01-implementation-plan.md`.
- Slice 1 work itinerary remains pending until the owner gives the explicit "begin Slice 1" green light.
