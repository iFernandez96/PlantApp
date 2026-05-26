# PlantApp — Data Sources

Status: Draft v0.1 — 2026-05-26

External data the app depends on, and the rules for using each. Verify each source's current docs before integration (per the CLAUDE.md framework-docs rule).

## 1. Weather

### National Weather Service (US)
- Use: forecast + alerts for US zip codes.
- Strengths: free, official, no key required, includes severe-weather alerts.
- Limitations: US only.
- Required: identify your application via `User-Agent`. Respect rate guidance.
- Caching: per-zip, per-hour server-side.

### Open-Meteo (global)
- Use: forecast and historical worldwide. Fallback / global support.
- Strengths: no key for many endpoints, global coverage, historical reanalysis.
- Limitations: no severe-weather alerts equivalent to NWS.
- Caching: per-zip, per-hour server-side.

Strategy: NWS primary in US; Open-Meteo primary outside US and fallback when NWS is degraded. Detect by user location.

## 2. Hardiness zone (US)

### USDA Plant Hardiness Zone Map
- Use: derive a `hardinessZone` for each `GardenSpace` from zip/postal code.
- Caching: per-zip indefinite; refresh when USDA publishes a new dataset.
- Outside US: use a per-country equivalent (RHS for UK, AHS heat zones, Köppen as a fallback) — TBD when international scope is added.

## 3. Plant knowledge (PlantProfile catalog)

Seed the catalog from authoritative, citable sources. Each `PlantProfile` field carries a `source` reference.

Examples (verify current URLs at integration time):
- University extension services (e.g. UC ANR, Iowa State Extension, Utah State Extension, NC State Extension).
- USDA NRCS PLANTS Database (taxonomy, native ranges).
- Royal Horticultural Society (UK).
- Curated, owner-reviewed entries for cultivars when no extension source covers them.

No species data is sourced from LLM hallucination. AI may suggest fields; an editor confirms with a citation before merge.

## 4. AI provider

### OpenAI Responses API
- Use: multimodal photo diagnosis + structured outputs for space optimizer + care explanation.
- Required: backend-only key, structured-output schema mode, zero-retention configuration where available.
- See `docs/ai-architecture.md`.

## 5. Notifications

### Firebase Cloud Messaging (Android)
- Use: server-pushed reminder dispatch.
- Required: FCM project, server key in backend secrets, token registration flow on Android.
- Local fallback: WorkManager scheduled reminders so offline / unregistered devices still fire.

## 6. Auth + storage

### Supabase
- Postgres: primary database. Row-level security per `userId` on every table.
- Auth: provider choice TBD ADR-0003.
- Storage: object storage for photos. Private buckets. Signed URLs with short TTL.

## 7. Sensors (future)

Not in MVP. Designed-for: a `SensorReading` table and a port for moisture/temperature/light. Likely vendors evaluated post-MVP.

## 8. Open items

- Hardiness-zone source for non-US users.
- Pollinator data — is a third-party source useful or is editorial catalog data enough?
- Pest/disease taxonomy — IPM source for tagging in `commonIssues`.
- Image-licensing for fixture / training-evaluation data sets.
