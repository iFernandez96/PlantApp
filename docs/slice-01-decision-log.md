# Slice 1 — Decision Log (Proposed)

Status: Proposed — awaiting owner approval
Date: 2026-05-26

This log captures the architecture decisions that are still **unpinned** in ADR-0001…0004 and must be pinned before Slice 1 begins. Each entry lists the choice, the reason, and a fallback. No code is written based on these proposals until the owner approves them.

---

## D-01 — API runtime (ADR-0003)

**Proposed pin:** Node.js + TypeScript.

**Why:** Fastest MVP velocity, large ecosystem for Ajv/JSON-Schema validation, mature Supabase SDK, easy deploy on Vercel/Fly/Render. JSON schemas in `shared-schemas/` neutralize most language lock-in if we migrate later.

**Fallback:** Kotlin/Ktor backend (lets the care-engine be a single shared module). Reconsider when the engine grows or when we measure latency.

---

## D-02 — Android HTTP client (ADR-0002)

**Proposed pin:** **Retrofit + OkHttp + kotlinx.serialization converter.**

**Why:** Highest-stability ergonomics on Android, very well documented, simpler interceptors for auth-token refresh and structured logging filters. Ktor client is fine but Retrofit's tooling story (logging, testing, mock web server) is more battle-tested for a single-platform app. We are not currently planning a Kotlin Multiplatform client.

**Fallback:** Ktor client. Re-evaluate if/when KMP enters the picture.

---

## D-03 — Database migrations (ADR-0003)

**Proposed pin:** **Supabase migrations CLI** (`supabase/migrations/*.sql`).

**Why:** Native to the chosen DB layer; one tool to learn; reproducible across dev/prod; works with Supabase branching.

**Fallback:** Drizzle migrations if we want type-safe schemas in TS code paths.

---

## D-04 — Background-job runner (ADR-0003)

**Proposed pin:** **Defer.** Slice 1 produces tasks synchronously on `POST /plants` and reads them on `GET /plants/:id/tasks`. No queue, no scheduler, no cron needed until Slice 3.

**Why:** YAGNI. Pinning this in Slice 1 invites premature infrastructure.

**Fallback (later):** `pg_cron` + `pg_listen`/`pg_notify` on Supabase; consider a dedicated queue only when message volume justifies it.

---

## D-05 — Auth flow (ADR-0003)

**Proposed pin:** **Supabase Auth, email magic link only** for Slice 1.

**Why:** Minimal UI surface, no password storage, no third-party OAuth setup required. Sufficient for single-user MVP.

**Fallback:** Add Google OAuth in Slice 2 if magic-link UX is annoying on a daily-use device.

---

## D-06 — JSON Schema validators (ADR-0002 / ADR-0003)

**Proposed pin:**
- Backend: **Ajv** (TS), 2020-12 mode.
- Android: **hand-written DTOs** with **kotlinx.serialization** + a small assertion layer; full JSON-Schema runtime validation only in tests, where we use `networknt/json-schema-validator` to validate round-trip.

**Why:** Avoids bundling a full JSON-Schema validator into the Android APK; relies on compile-time DTO types in production code paths.

**Fallback:** If a slice introduces dynamic schemas client-side, add the runtime validator.

---

## D-07 — Crash reporting (ADR-0002)

**Proposed pin:** **Defer to Slice 3.** Slice 1 ships in-app only on a single device; manual issue capture is acceptable.

**Why:** Avoid integrating Firebase Crashlytics for a single-user pre-release.

**Fallback:** Add Crashlytics or Sentry when notifications begin (Slice 3).

---

## D-08 — API hosting (ADR-0003)

**Proposed pin:** **Defer until first deploy is needed.** Slice 1 development is local. The hosting decision (Vercel Functions vs Fly.io vs Render) does not need to be made until we want to install the app on the device with a real backend reachable from outside dev.

**Why:** Doesn't affect Slice 1 code structure. Adds reversal risk if chosen too early.

**Fallback:** When needed, start with Vercel Functions for TS API (lowest setup) and revisit if cold-starts hurt UX.

---

## D-09 — Care-engine location (single source vs duplicated)

**Proposed pin:** **Backend-only for Slice 1.** Engine lives in `backend/care-engine/` (TS). Android does not compute schedules locally — it reads tasks from the backend.

**Why:** Single source of truth for Slice 1; avoids cross-language drift before there's a real need for offline scheduling.

**Fallback:** When offline scheduling is required (likely Slice 3 or 4), reintroduce a Kotlin port and pair both implementations with a shared JSON test-vector suite.

---

## D-10 — Care-engine v0.1.0 watering formula

**Proposed pin:** The formula documented in `docs/slice-01-implementation-plan.md` §"Care-engine v0.1.0 rule":
- `dueAt = (plant.plantedAt ?? plant.createdAt) + profile.wateringProfile.baseIntervalDays × containerFactor`
- `containerFactor = clamp(container.volumeLiters / profile.containerProfile.recommendedMinLiters, 0.5, 1.5)`
- `priority = "normal"`
- `engineVersion = "0.1.0"`

**Why:** Minimal but plausible and fully testable. Slices 3 and 4 will extend it.

**Fallback:** If owner review thinks the default interval should also be capped (e.g. never < 6h), add a floor in the same patch and bump to `0.1.1`.

---

## D-11 — Slice 1 photo handling

**Proposed pin:** **No photos at all in Slice 1.** Schemas allow a `photos` array on `PlantInstance` and `GardenSpace`, but the Slice 1 UI does not capture or upload images. CameraX is not added as a dependency yet.

**Why:** Photos are sensitive data; their pipeline (EXIF stripping, signed URLs, storage TTL) is a real piece of work and belongs to Slice 7 alongside AI diagnosis.

**Fallback:** None. This is locked.

---

## D-12 — Slice 1 location handling

**Proposed pin:** **Postal code only.** `GardenSpace.postalCode` is optional in Slice 1 (the engine doesn't use it yet). No GPS, no background location, no location permissions in the Android manifest.

**Why:** Privacy minimization. Hardiness-zone resolution is a Slice 6 concern.

**Fallback:** None. This is locked.

---

## Approval

When the owner approves, mark each "Proposed pin" as **Accepted** and:
- Update ADR-0002 with D-02, D-06, D-07.
- Update ADR-0003 with D-01, D-03, D-04, D-05, D-06, D-08, D-09.
- Capture D-10, D-11, D-12 in `docs/slice-01-implementation-plan.md`.
- Open the Slice 1 work itinerary.
