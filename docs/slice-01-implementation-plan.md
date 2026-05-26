# Slice 1 — Implementation Plan: "Add a plant in a container in a garden space, generate one deterministic care task"

Status: Proposed v0.2 (post-review) — NOT YET APPROVED FOR IMPLEMENTATION
Owner: Israel Fernandez
Date: 2026-05-26

## Goal

Deliver the thinnest end-to-end vertical slice that proves the architecture:

1. The user creates a `GardenSpace`, a `Container`, and a `PlantInstance` tied to both.
2. The deterministic care-engine generates **one** initial `CareTask` of kind `water` for the new plant.
3. The task is visible in the Android UI with `rationale`, `engineVersion`, `inputsHash`, and `sourceInputs`.

This slice excludes weather, feedback, advisories, feeding, AI, notifications, photos, and precise location.

## In-scope BDD scenarios

From `features/plant-inventory.feature` — tagged `@slice-1`:
- Add a passion fruit plant in a 5-gallon barrel **with a last-watered date** (happy path).
- Adding a plant generates one initial deterministic water task with `dueAt` anchored on the supplied `lastWateredAt` (happy path).
- Baseline fallback: when `lastWateredAt` is omitted, `dueAt` is anchored on `createdAt`.
- Negative: missing container.
- Negative: missing garden space.
- Negative: unknown profile id.
- Negative: non-positive container volume.
- Negative: unknown container material.
- Negative: empty name / missing kind for garden space.
- Authorization: a user cannot read another user's plants.
- Determinism: re-adding produces a different inputsHash because `clockUtc` (and possibly the new `lastWateredAt`) differs, with consistent shape.

From `features/watering.feature` — tagged `@slice-1`:
- The engine output is purely a function of inputs.

### Slice 1 add-plant form

The add-plant form in `:feature-inventory` collects:
- profile (selector against the catalog),
- nickname (optional),
- container (selector or create-new),
- garden space (selector or create-new),
- placement,
- growth stage,
- **last watered** (optional date-time; populates `PlantInstance.lastWateredAt` and the engine's baseline; default is "unknown — use creation time").

## Out of scope (explicitly)

- Hardiness zone, weather, freeze warnings (Slice 6).
- Feedback log and overdue escalation (Slice 4).
- Feeding tasks (Slice 5).
- Container-size, support, pollination advisories (Slice 2).
- Photo capture, EXIF handling, CameraX (Slice 7).
- AI gateway, prompts, evals as runtime code (build files only — Slice 7).
- FCM / WorkManager reminders (Slice 3).

## Required documentation checks before coding (per CLAUDE.md)

- Jetpack Compose + Material 3 — current stable navigation, list, form components.
- Hilt setup for Compose-only apps.
- Room with KSP.
- Ktor client (engine choice) OR Retrofit (per ADR-0002 pin).
- kotlinx.serialization JSON.
- Supabase: Postgres schema design, Auth flows, RLS policy syntax.
- JSON Schema 2020-12 validator: Ajv (TS) or networknt (Kotlin).

If web access is unavailable: pause and report which docs are needed.

## Trimmed Android module set for Slice 1

Start with the **minimum** that respects clean architecture, defer the rest until a slice actually needs them:

```
:app                composition root + theme + navigation
:design-system      basic Material 3 tokens + a small set of components
:domain             use cases, models (pure Kotlin)
:data               Room + repository impls + DataStore
:network            HTTP client + DTOs + schema validation
:feature-inventory  the three Slice 1 screens
```

**No `:care-engine` Android module in Slice 1.** Per decision log D-09 the care-engine lives only in the backend (TypeScript) for Slice 1. Android reads tasks from the backend via `:network` and treats them as opaque domain values. A Kotlin port is deferred until offline scheduling/reminders are required (likely Slice 3 or 4); the port will be paired with a shared JSON test-vector suite to prevent cross-language drift.

Defer to later slices: `:care-engine` (Kotlin port — Slice 3 or 4 when offline scheduling is needed), `:notifications` (Slice 3), `:camera` (Slice 7), `:feature-care`, `:feature-diagnosis`, `:feature-space`.

## Backend scope for Slice 1

- Supabase Postgres with tables: `users` (via Auth), `garden_spaces`, `containers`, `plant_profiles`, `plant_instances`, `care_tasks`.
- RLS on every user-owned table.
- `plant_profiles` is read-only to clients; seeded by migration.
- Endpoints:
  - `POST /garden-spaces`, `GET /garden-spaces`
  - `POST /containers`, `GET /containers`
  - `POST /plants`, `GET /plants`, `GET /plants/:id`, `DELETE /plants/:id`
  - `GET /plants/:id/tasks` (returns the current task set; Slice 1: at most one `water` task per plant)
- The care-engine module is a pure TypeScript module for Slice 1, called from `POST /plants` and `DELETE /plants` flows.

## Care-engine v0.1.0 rule (Slice 1 only)

```
wateringBaselineAt = plant.lastWateredAt ?? plant.createdAt
containerFactor    = clamp(container.volumeLiters
                           / profile.containerProfile.recommendedMinLiters,
                           0.5, 1.5)
dueAt              = wateringBaselineAt
                     + profile.wateringProfile.baseIntervalDays × containerFactor
priority           = "normal"
rationale          = "<species common name>: base interval {baseIntervalDays}d
                      adjusted by container factor {containerFactor};
                      baseline {wateringBaselineAt}"
engineVersion      = "0.1.0"
sourceInputs       = { plantInstanceId, profileId, profileVersion,
                       containerId, gardenSpaceId, clockUtc,
                       wateringBaselineAt,
                       weatherWindowRef: null, feedbackWindowRef: null }
inputsHash         = sha256(canonical-json(sourceInputs))
```

No weather, no feedback, no advisories. Deliberately simple so it's auditable.

**On `lastWateredAt`:** the Slice 1 add-plant form accepts an optional "last watered" date-time so the user can onboard existing plants honestly. The field is an onboarding baseline only — Slice 4 introduces `CareLogEvent` as the long-term source of truth and the engine derives the baseline from log history from then on.

## First failing tests (red-first order)

These tests should be written **before** their respective implementations. Each is in its own layer; none requires the others to run.

### Schema-validation tests (run first — depends on nothing else)
1. `plant-profile.schema.json` accepts each seed profile (passion fruit, tomato, tomatillo, strawberry, basil).
2. `plant-instance.schema.json` accepts a valid instance and rejects missing `containerId` / `gardenSpaceId`.
3. `container.schema.json` rejects `volumeLiters <= 0` and unknown `material`.
4. `garden-space.schema.json` rejects empty `name` and missing `kind`.
5. `care-task.schema.json` rejects records without `sourceInputs`, `engineVersion`, or `inputsHash`.
6. Round-trip encode/decode equality for each schema's DTO on both backend and Android.

### Domain / care-engine unit tests (pure, no I/O)
7. `computeInitialWaterTask(...)` returns one `CareTask` with kind = "water".
8. The task carries the exact `engineVersion`, `inputsHash`, `sourceInputs` (including `wateringBaselineAt`), `rationale`, `dueAt`, `priority`.
9. Determinism: equal inputs (including `clockUtc` and `wateringBaselineAt`) → byte-equal output and identical `inputsHash`.
10. Container factor clamps: ratios below 0.5 and above 1.5 produce factors 0.5 and 1.5 respectively.
11. Recomputation with a later `clockUtc` (and unchanged baseline) produces a different `inputsHash` but the **same** `dueAt`, because `dueAt` is anchored on `wateringBaselineAt`.
12. Baseline supplied: when `plant.lastWateredAt` is set, `sourceInputs.wateringBaselineAt === plant.lastWateredAt` and `dueAt = lastWateredAt + interval × containerFactor`.
13. Baseline fallback: when `plant.lastWateredAt` is absent, `sourceInputs.wateringBaselineAt === plant.createdAt` and `dueAt` is anchored there.
14. Baseline-induced hash difference: two otherwise-identical plants with different `lastWateredAt` values produce different `inputsHash` values and correspondingly different `dueAt` values.

### Repository / API integration tests (against a real Postgres)
15. `POST /plants` with valid body creates the plant and emits one initial `CareTask`.
16. Missing `containerId` → 400 with field-level error.
17. Missing `gardenSpaceId` → 400.
18. Unknown `profileId` → 400.
19. RLS: user A cannot `GET /plants` belonging to user B; query returns empty / 403.
20. `DELETE /plants/:id` removes the plant and its `CareTask` rows.

### Android UI tests (Compose semantics)
21. Empty state renders when no plants exist.
22. Add-plant flow: filling all fields and submitting navigates to the detail screen showing the new plant.
23. Detail screen shows the next task with kind "water", a rationale string, the `engineVersion` badge, and a `dueAt` formatted timestamp.
24. Validation: submitting without container shows a field-level error and does not navigate.

## Architecture decisions to pin (proposed; awaiting approval)

See `docs/slice-01-decision-log.md`.

## Definition of done for Slice 1

- All `@slice-1` scenarios pass at appropriate layers.
- Engine table tests green; engineVersion = "0.1.0" tagged on every produced CareTask.
- All Slice 1 schemas validated round-trip.
- ADR pins in `docs/slice-01-decision-log.md` confirmed by owner and reflected in updated ADR files.
- Owner can add their 5 real plants on a physical device and sees one initial water task per plant with rationale + engineVersion + sourceInputs.
- `docs/architecture.md`, `docs/domain-model.md`, and this file updated if anything diverged during implementation.
- One-page slice-1 retro added to `docs/`.

## Do NOT implement until the owner approves this plan.
