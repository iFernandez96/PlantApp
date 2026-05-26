# PlantApp — Product Requirements Document

Status: Draft v0.1 — 2026-05-26
Owner: Israel Fernandez

## 1. Problem

Existing plant-care apps (Planta, Blossom, PictureThis) are general-purpose. They treat plants as species in pots, not as living instances inside a constrained small-space garden (condo balconies, upstairs patios, vertical racks). They under-serve container-specific concerns: restricted root volume, faster soil drying, faster nutrient leaching, support/trellis needs for vines, vertical-stacking opportunities, and microclimate effects on a single railing.

The owner needs an app that:

- Treats each plant as a *PlantInstance* tied to a *Container* inside a *GardenSpace*.
- Generates deterministic, container-aware watering and feeding reminders.
- Uses local climate, hardiness zone, and forecast data.
- Uses AI (photo diagnosis, space optimization) as an assistant, not as the decision-maker for scheduling.
- Scales to many species via a data-driven plant catalog.

## 2. Goals

- **G1** — Reliable, container-aware care reminders that the owner actually trusts.
- **G2** — A single source of truth for "what's growing, where, in what."
- **G3** — AI-assisted diagnosis and space optimization that respects privacy and produces structured, auditable output.
- **G4** — A data model that scales from the owner's ~6 plants today to dozens of species without code changes.
- **G5** — Vertical-space planning as a first-class concern, not an afterthought.

## 3. Non-goals (for MVP)

- Social/community features.
- Plant marketplace or ecommerce.
- Multi-user shared gardens (single-user MVP).
- Wearable/smartwatch app.
- Outdoor in-ground large-garden planning.
- Sensor/IoT integration in v1 (designed-for, not built).

## 4. Target user

The owner: a hobbyist gardener growing edible container plants on a small upstairs condo space. Comfortable with technology, wants accurate guidance, dislikes generic reminders, willing to log observations and photos.

Secondary users (post-MVP): other hobbyist container gardeners in apartments, condos, balconies, small patios.

## 5. Core user stories (MVP)

1. As a gardener, I add a passion fruit plant in a 5-gallon barrel on my balcony so the app knows the species, container, and location.
2. As a gardener, I receive a watering reminder that accounts for today's forecast and the small container size.
3. As a gardener, I record that I watered (or skipped) a plant and the next reminder adjusts.
4. As a gardener, I get a feeding reminder appropriate for a container tomato during fruiting.
5. As a gardener, I am warned my passion fruit will outgrow its 5-gallon container and given target sizes.
6. As a gardener, I am warned that a single tomatillo will not reliably set fruit and need a second plant.
7. As a gardener, I take a photo of a struggling leaf and receive an AI diagnosis with structured causes and follow-up tasks.
8. As a gardener, I photograph and measure my balcony and receive a space-optimization plan that includes vertical stacking.

## 6. Functional requirements

- **FR1** Plant inventory CRUD with species lookup against a `PlantProfile` catalog.
- **FR2** Garden-space and container records with sunlight/wind/shade attributes.
- **FR3** Deterministic care-task generation for watering and feeding.
- **FR4** Watering/feeding log with user feedback (skipped, soil wet, wilted, watered early) feeding back into the engine.
- **FR5** Local weather and hardiness-zone integration.
- **FR6** Notifications via FCM + local WorkManager fallback.
- **FR7** AI photo diagnosis via backend gateway with structured JSON output.
- **FR8** AI space optimizer via backend gateway with horizontal + vertical layout plans.
- **FR9** Account/auth via Supabase (email or OAuth, TBD ADR).
- **FR10** Encrypted local storage of plant data; signed-URL photo storage.

## 7. Non-functional requirements

- **NFR1** Offline-first for inventory, schedule view, and reminders.
- **NFR2** P95 reminder dispatch within 5 minutes of scheduled time.
- **NFR3** AI diagnosis response < 15s P95 end-to-end.
- **NFR4** No third-party SDK in Android receives photos or location.
- **NFR5** All AI prompts versioned; production prompt rollouts gated on eval pass.
- **NFR6** No raw image bytes or location strings in logs.

## 8. Success metrics

- Reminder relevance: % of reminders the user marks "done" vs "skipped — wrong timing".
- Care adherence: average days between recommended and actual watering for a tracked plant.
- AI diagnosis utility: % of diagnoses the user accepts an associated follow-up task for.
- Catalog growth: number of `PlantProfile` records covered without code changes.

## 9. Open questions

- Auth provider: Supabase Auth vs Firebase Auth? (ADR-0003)
- Backend runtime: Supabase Edge Functions, a separate Node/Kotlin service, or both? (ADR-0003)
- AI provider: OpenAI Responses API as primary; secondary fallback? (ADR-0004)
- Weather provider primary: NWS (US-only) with Open-Meteo fallback, or Open-Meteo primary? (ADR-0003 / data-sources.md)
- Hardiness zone source: USDA PHZM API + ZIP geocoding accuracy. (data-sources.md)
