# Slice 2 — Implementation Plan: Advisories

Status: In progress (S2.0) — 2026-06-02
Owner: Israel Fernandez

## Goal

Surface **deterministic, profile-driven advisories** for a `PlantInstance` — computed on
read, never persisted as schedule state, and **never auto-creating a `CareTask`**. Source
of truth for behavior: `features/container-health.feature` (`@slice-2`, including the
`@invariant` scenario) and `docs/roadmap.md` Slice 2.

Advisories are advice, not tasks: the deterministic care-engine (Slice 1) remains the only
producer of `CareTask`s. A user may later *accept* an advisory's recommendation, which would
create a task through the engine's normal API — but that acceptance flow is out of Slice 2
scope.

## The three advisory rules (deterministic, profile-driven)

1. **container-size** (`severity: high`) — when
   `container.volumeLiters < profile.containerProfile.recommendedMinLiters`.
   The message cites the recommended minimum and, when present, the ideal range
   (`idealMinLiters`–`idealMaxLiters`). It suggests **target sizes, not brands**.
2. **support** — when `profile.requiresSupport === true && plantInstance.supportRecorded
   !== true`.
3. **pollination** — when `profile.selfFruitful === false` and the count of the user's
   **active instances of that profile** `< profile.pollinationPartnersRequired`. Clears
   automatically once a partner is added (the count reaches the required number).

## Invariant

Advisories **never auto-schedule or auto-create tasks** (`@slice-2 @invariant`). They are
surfaced for display only. Acceptance → task creation is a later concern (not Slice 2).

## Contract

`shared-schemas/advisory.schema.json` (2020-12, `additionalProperties:false`):
`kind` (container-size|support|pollination), `severity` (low|medium|high),
`plantInstanceId` (uuid), `profileId`, `title`, `message`, optional `details` (free-form
object, e.g. recommendedMinLiters / idealMin/Max / currentVolume / instanceCount /
requiredPartners), optional `createdAt`. camelCase, consistent with the other
shared-schemas (D-06).

## Decomposition

- **S2.0 (this step):** Slice 2 plan doc + `advisory.schema.json` + red→green schema test.
- **S2.1:** deterministic `computeAdvisories(...)` engine (backend TS, pure) implementing
  the three rules + the no-auto-task invariant. Red-first unit tests covering the BDD rule
  cases and validating output against `advisory.schema.json`. Pollination needs the count
  of the user's active instances of the profile (engine input).
- **S2.2:** `GET /plants/:id/advisories` API (RLS-scoped, response conforms to
  `advisory.schema.json`) + integration tests.
- **S2.3:** Android display of advisories on the plant detail screen (+ UI test).

## Seed-data gap (addressed in S2.2)

The Slice 1 seed profiles (`backend/care-engine/seed-profiles.ts` and the
`plant_profiles` migration) carried `containerProfile.recommendedMinLiters` but **not**
`idealMinLiters` / `idealMaxLiters`. The container-size advisory cites an ideal range when
present, so **S2.2** enriched the seed + DB where appropriate (passion fruit ideal 95–190 L;
migration `0004_slice1_profile_ideal_range.sql` jsonb-merges ideal ranges into the seeded
`plant_profiles`, and `seed-profiles.ts` mirrors them) — keeping `plant-profile.schema.json`
(which already allows `idealMinLiters`/`idealMaxLiters`) and the seed in sync.

## In scope (Slice 2)

- The `Advisory` contract, the deterministic engine, the read API, and the Android display.

## Out of scope (Slice 2)

- Accept-advisory → create-task flow. Notifications. Weather/seasonal advisories (later
  slices). Persisting advisories (they are computed on read).

## Definition of done

The five `@slice-2` scenarios in `features/container-health.feature` are green at the
appropriate layers:
1. Passion fruit in a 5-gallon (19 L) container → container-size advisory (severity high,
   cites recommended min + ideal range).
2. Vining species without support recorded → support advisory.
3. Single tomatillo (non-self-fruitful) → pollination warning.
4. Adding the required partner → warning clears.
5. Invariant: advisories never auto-schedule corrective tasks.

Plus: advisory engine unit tests green; `GET /plants/:id/advisories` integration tests
green (incl. RLS); Android advisory display + UI test green; `npm run validate-schemas`
compiles `advisory.schema.json`.
