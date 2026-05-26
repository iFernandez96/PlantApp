# PlantApp — MVP Roadmap (Vertical Slices)

Status: Draft v0.2 — 2026-05-26 (revised after foundation review)

Each slice is end-to-end thin: schema → engine → API → Android UI → tests → docs. No slice begins without its `.feature` file. No slice ships without DOD per `sdlc-plan.md`.

Scenarios in `features/*.feature` are tagged with a slice tag (`@slice-1`, `@slice-2`, …) so the test runner can execute only the in-scope behavior per slice.

## Slice 1 — Plant in a container in a space + first deterministic care task

**Goal:** End-to-end thinnest path. Owner adds a `PlantInstance` inside a `Container` inside a `GardenSpace`; the deterministic care-engine generates **one** initial `CareTask` of kind `water` with full traceability (`engineVersion`, `inputsHash`, `sourceInputs`, `rationale`, `dueAt`). No weather, no feedback, no advisories, no AI, no notifications.

Scope:
- Schemas: `plant-profile`, `plant-instance`, `container`, `garden-space`, `care-task`.
- Seed `PlantProfile` records: passion fruit, tomato, tomatillo, strawberry, basil.
- Backend: inventory CRUD + initial-task generation endpoint + RLS.
- Android: minimal modules; add/list/detail screens; in-app task display only.
- BDD scenarios tagged `@slice-1`. (See `docs/slice-01-implementation-plan.md` for the exact list.)

Exit: Owner adds their 5 real plants on a device and each shows one initial water task with rationale + engineVersion + sourceInputs visible.

## Slice 2 — Advisories: container-size, support, pollination

**Goal:** Deterministic, profile-driven advisories that surface in the UI without auto-creating CareTasks.

Scope:
- Container-size advisory when `Container.volumeLiters < PlantProfile.containerProfile.recommendedMinLiters`.
- Support advisory when `PlantProfile.requiresSupport && !PlantInstance.supportRecorded`.
- Pollination warning when `PlantProfile.selfFruitful === false && active instance count < pollinationPartnersRequired`.
- BDD scenarios tagged `@slice-2` (see `features/container-health.feature` and `features/plant-inventory.feature`).

Exit: Passion fruit shows a container-size advisory; lone tomatillo shows a pollination warning; adding a second tomatillo dismisses it.

## Slice 3 — Deterministic watering reminders with notifications

**Goal:** Care-engine watering rules incorporating recurrence + basic-weather (still simplified). Reminders dispatch via FCM with a local WorkManager fallback.

Scope:
- Recurrence rule (after a CareLogEvent advances the next dueAt).
- Hot-weather and rainy-forecast adjustments (basic weather adapter — see Slice 6 for the full integration).
- FCM channel + WorkManager local fallback.
- BDD scenarios tagged `@slice-3` (most of `features/watering.feature`).

Exit: Owner gets a real reminder for their tomato that respects container size and current forecast.

## Slice 4 — Watering log and feedback loop

**Goal:** User feedback updates the next task deterministically.

Scope:
- `CareLogEvent` persistence.
- Engine consumes recent feedback (`soil-still-wet`, `plant-wilted`, `watered-early`, `fertilizer-too-strong`).
- Overdue escalation rule (raises priority, does not silently create a second task).
- BDD scenarios tagged `@slice-4` (`features/watering.feature` feedback cases).

Exit: Marking "soil-still-wet" demonstrably pushes the next reminder later.

## Slice 5 — Feeding reminders

Scope:
- Feeding rules: container leach adjustment, fruiting-stage acceleration, dormancy suppression, post-harvest follow-up.
- BDD scenarios tagged `@slice-5` (`features/feeding.feature`).

Exit: Fruiting tomato gets a higher-frequency feed reminder; "fertilizer-too-strong" pushes the next feed out.

## Slice 6 — Real weather + hardiness zone + seasonal awareness

Scope:
- Weather adapter (NWS US, Open-Meteo elsewhere).
- USDA PHZM hardiness-zone resolution by zip.
- Freeze-warning protection task + degraded fallback (`rationaleMetadata.degraded = true`).
- BDD scenarios tagged `@slice-6` (`features/seasonal-care.feature` + rainy/freeze scenarios in `features/watering.feature`).

Exit: Real forecast changes my next watering; a freeze warning produces a protection task.

## Slice 7 — AI photo diagnosis

Scope:
- AI gateway service (sole LLM caller).
- `prompts/plant-diagnosis.system.md` v0.1.0.
- CameraX capture with EXIF GPS stripping.
- Suggestion → user-accept → CareTask via the engine API.
- Eval suite gate before rollout.
- BDD scenarios tagged `@slice-7` (`features/photo-diagnosis.feature`).

Exit: Owner photographs a struggling leaf and gets structured findings + acceptable suggestions.

## Slice 8 — Space optimizer

Scope:
- `prompts/space-optimizer.system.md` v0.1.0.
- Measurement + photo capture UI.
- Proposal review UI; per-placement accept writes through inventory API.
- Eval suite gate.
- BDD scenarios tagged `@slice-8` (`features/space-optimizer.feature`).

Exit: My balcony returns a plan with horizontal and vertical options.

## Slice 9 — Catalog expansion

Scope:
- Profile import format + validator.
- Source-citation enforcement.
- Editorial flow (single-user MVP: owner is admin).
- Batch add of additional species.

Exit: New species are addable to the catalog without code changes.

## Post-MVP backlog (not committed)

Sensors (moisture/temp/light), multi-user shared gardens, community, wearables, on-device diagnosis, pollinator-overlap engine, deep IPM integration.
