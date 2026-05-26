# CLAUDE.md — PlantApp

Guidance for Claude Code working in this repository.

## Project purpose

PlantApp is an Android-first container-garden intelligence app for small spaces (condos, balconies, upstairs patios). It helps a gardener care for many container plants — starting with passion fruit, tomatoes, tomatillos, strawberries — by combining:

- A deterministic care-rules engine for watering, feeding, and seasonal tasks.
- Local climate, hardiness-zone, and weather awareness.
- A container/space-aware data model (no in-ground assumptions).
- AI-assisted photo diagnosis and space-optimization layered on top of (never replacing) the rules engine.
- Horizontal **and** vertical layout planning.

The app must scale data-driven to many plant species over time. The first MVP is scoped to the owner's real garden.

## Architecture principles

- **Domain-first.** Plant care concepts (PlantInstance, Container, GardenSpace, CareTask) drive the schema and code, not the UI.
- **Deterministic core, AI periphery.** Watering, feeding, and seasonal scheduling are computed by a pure, testable rules engine. AI explains, diagnoses, and recommends — it never silently mutates schedules.
- **Vertical slices.** Ship one end-to-end feature at a time (data model → engine → UI → tests) before starting the next.
- **Backend-only AI.** OpenAI / multimodal calls go through a backend gateway. The Android app never holds provider keys and never calls OpenAI directly.
- **Privacy by default.** Photos, location, and garden-space images are sensitive. Minimize collection, scope retention, and never log raw image bytes.
- **Structured outputs.** AI responses are constrained to JSON schemas under `shared-schemas/`. Free-form text only inside a typed field.
- **Offline-tolerant.** Reminders and the care engine must work without a network (local Room + WorkManager). AI features are explicitly online.
- **Data-driven plant catalog.** Care behavior derives from `PlantProfile` records, not hard-coded `when (species)` branches.

## BDD-first rule

No feature work begins without a Gherkin scenario in `features/`. Order of operations:

1. Write or update `features/<feature>.feature` with concrete examples.
2. Translate scenarios into failing tests (unit, engine, UI, or backend) before implementation.
3. Implement the thinnest slice that makes the scenarios pass.
4. Refactor with tests green.

## Test-first rule

- Care-engine logic: backend TypeScript unit tests with table-driven cases for Slice 1; a Kotlin port is added later only if offline scheduling requires it.
- Android UI: Compose UI tests with semantics + screenshot tests where stable.
- Backend: contract tests against the shared JSON schemas + integration tests.
- AI: deterministic evaluation harness under `evals/` — golden inputs, scored outputs, regression on prompt or model change.
- A change is not done until the relevant test layer is green and added to CI.

## Documentation-first rule

Before code changes that affect architecture, schema, or AI behavior, update the relevant document:

- Product or scope change → `docs/prd.md`.
- New module, boundary, or dependency → `docs/architecture.md` and an ADR under `docs/adr/`.
- Domain change → `docs/domain-model.md` and the affected schema under `shared-schemas/`.
- AI prompt or contract change → `prompts/` + `docs/ai-architecture.md` + bump prompt version.

ADRs are append-only. Supersede, don't rewrite.

## AI safety and privacy rules

- Backend gateway is the **only** caller of OpenAI or other LLM providers. The Android app calls our backend; the backend calls the model.
- All AI outputs are validated against a JSON schema in `shared-schemas/`. Reject and retry on schema violation.
- Photo and location data are treated as sensitive PII. Strip EXIF GPS before upload unless the user explicitly opts in for location-tagged diagnosis.
- Photos in object storage use signed URLs with short TTLs. No public buckets.
- Prompts are versioned. Production rollouts of a new prompt require passing the evaluation suite in `evals/`.
- AI may **recommend** care actions; the deterministic engine remains the source of truth for scheduled tasks. AI recommendations are surfaced as suggestions the user accepts, which then create or modify tasks through the engine's normal API.
- No PII or raw image bytes in logs. Log identifiers and decisions, not content.

## Deterministic care scheduling rule

Watering, feeding, and seasonal task generation must be:

- Pure functions of `(PlantInstance, PlantProfile, Container, GardenSpace, weather window, user feedback log, clock)`.
- Reproducible: same inputs → same outputs, regardless of model availability.
- Fully unit-tested with table-driven cases covering hot/cold weather, small/large containers, recent rain, flowering stage, missed waterings, and user "soil still wet" feedback.
- Versioned: rule-engine version is stamped on each generated `CareTask` for forensic traceability.

## Framework/API documentation rule

Before writing code that uses a framework, SDK, API, or platform feature, **check current official documentation** for that version. This applies to (non-exhaustive):

- Jetpack Compose, Material 3, Room, DataStore, WorkManager, CameraX, Hilt, Navigation.
- Firebase Cloud Messaging on Android 13+ notification permission semantics.
- Retrofit + OkHttp client APIs (per D-02). Ktor is fallback-only if Kotlin Multiplatform is later adopted.
- Supabase (Postgres, Auth, Storage, RLS).
- OpenAI Responses API + structured outputs.
- USDA Plant Hardiness Zone API, National Weather Service API, Open-Meteo.

If web access is unavailable, state which docs are needed and proceed with framework-agnostic planning only.

## Repository layout

```
docs/                  Product, architecture, domain, SDLC, privacy, AI, data-source docs
docs/adr/              Architecture decision records (append-only)
features/              Gherkin .feature files — BDD source of truth
shared-schemas/        JSON schemas shared by Android, backend, and AI outputs
prompts/               System prompts for AI flows (versioned in-file)
evals/                 AI evaluation harness, golden cases, scoring
android/               Android app — Kotlin, Compose, Hilt (scaffolded; Slice 1 modules only)
backend/               Backend API, care-engine service, AI gateway (scaffolded; no production behavior yet)
```

## Commands (placeholders until code lands)

The repo currently has no buildable code. As slices land, update this section. The stack below reflects the accepted Slice 1 decisions (D-01 … D-12).

```bash
# Android (Slice 1 — Retrofit + OkHttp + kotlinx.serialization stack per D-02)
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
./gradlew :app:connectedDebugAndroidTest
# Note: no :care-engine Android module in Slice 1 (D-09).
# A Kotlin port may appear in Slice 3 or 4 if offline scheduling is needed;
# at that point: ./gradlew :care-engine:test

# Backend (Slice 1 — Node.js + TypeScript per D-01)
# From backend/:
#   npm test                unit + care-engine tests (incl. shared JSON test vectors)
#   npm run test:int        integration tests against a local Postgres
#   npm run lint
#   npm run typecheck
#   npm run build
#   npm run validate-schemas compile every shared-schemas/*.schema.json with Ajv

# Supabase migrations (D-03)
# From repo root:
#   supabase migration list
#   supabase migration up

# BDD feature lint
# (TBD — gherkin-lint or cucumber dry-run once a runner is chosen)

# AI evaluations (Slice 7+)
# (TBD — see docs/ai-evaluation-plan.md)
```

## Slice 1 scope (locked)

Slice 1 = "Add a plant in a container in a garden space, generate one deterministic care task." Exactly that. See `docs/slice-01-implementation-plan.md` for the in-scope BDD scenarios. Decisions D-01 through D-12 in `docs/slice-01-decision-log.md` were **accepted on 2026-05-26** and are reflected in ADR-0002 and ADR-0003. Out of scope until later slices: weather, feedback loop, feeding, advisories, AI, notifications, photos, precise location.

**Care-engine lives only in the backend (TypeScript) for Slice 1** (decision log D-09). The Android app does **not** contain a `:care-engine` module in Slice 1; it reads tasks from the backend. A Kotlin port is added later when offline scheduling is required.

## Project subagents

The repository ships seven project-level Claude Code subagents under `.claude/agents/`. They are read-only reviewers — they inspect the working tree and return findings; they never edit, write, commit, or push. The main Claude session remains responsible for all edits, commits, and pushes unless the owner explicitly approves otherwise.

| Subagent | Purpose |
|---|---|
| `repo-guardian` | Slice scope, git hygiene, commit/push cadence, no accidental production behavior, no secrets/photos/location fixtures committed. |
| `docs-consistency-auditor` | README/CLAUDE/docs/ADRs/roadmap/Slice 1 plan agree on phase, scaffolding status, accepted decisions, Slice 1 scope. |
| `backend-scaffold-reviewer` | `backend/` — strict TS, script alignment, no HTTP server / Supabase client / care-engine rules / production behavior yet. |
| `android-scaffold-reviewer` | `android/` — only Slice 1 modules, accepted network stack, no Slice-1 forbidden deps (CameraX, FCM, WorkManager, AI SDKs), wrapper situation clear. |
| `schema-contract-reviewer` | Shared JSON Schemas, domain model, slice plan, and BDD agree on PlantProfile / PlantInstance / Container / GardenSpace / CareTask and the watering-baseline contract. |
| `privacy-security-reviewer` | Slice 1 privacy posture: no photos, no precise GPS, no camera/notification permissions, no LLM SDKs on Android, no secrets. |
| `bdd-qa-reviewer` | Gherkin quality, Slice 1 behavior coverage, observable Then-steps, correct `@slice-N` tagging, negative scenarios present. |

**When to run them.** After any scaffolding or implementation change that touches their scope, and always before starting the next slice. Run the relevant subagents *before* asking the owner to approve a slice transition.

Subagents use the `sonnet` model and read-only tools (`Read, Grep, Glob, Bash`). They are invoked through the Agent tool.

## What Claude must NOT do

- Do not start implementation before the user approves the slice plan.
- Do not hard-code species-specific care logic in code branches. Encode it as data in `PlantProfile`.
- Do not add OpenAI/Anthropic/Gemini SDKs to the Android module.
- Do not ship a feature without a corresponding `.feature` file and a passing test layer.
- Do not let AI write to the care schedule without going through the engine's typed API.
- Do not commit photos, secrets, `.env`, or location-tagged sample data.
