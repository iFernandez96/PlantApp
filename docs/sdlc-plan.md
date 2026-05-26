# PlantApp — SDLC Plan

Status: Draft v0.1 — 2026-05-26

## 1. Methodology

- **BDD-first.** Every feature begins as a `.feature` file under `features/`.
- **Vertical slices.** Each slice ships an end-to-end thin path: schema → engine → API → Android UI → tests → docs.
- **Test pyramid.** Heavy on unit + engine tests; targeted integration; minimal but real UI tests; AI evaluations as a separate suite.
- **Trunk-based.** Short-lived branches, small PRs, feature flags for incomplete slices.

## 2. Lifecycle of a slice

1. **Discover** — capture user need in `docs/prd.md`. Identify the smallest valuable cut.
2. **Specify** — write Gherkin scenarios in `features/<slice>.feature`.
3. **Decide** — if architectural, write an ADR under `docs/adr/`.
4. **Design schema** — add/extend JSON Schema in `shared-schemas/`.
5. **Engine first** — implement pure care-engine logic with table-driven tests. No UI yet.
6. **Backend API** — expose endpoint(s) with schema validation + contract tests.
7. **Android** — repository + ViewModel + Compose screen + UI test for the scenario.
8. **Wire telemetry** — structured logs (no PII), metrics for adherence + reminder relevance.
9. **Document** — update `architecture.md`, `domain-model.md`, slice retrospective note.
10. **Ship behind flag** — enable for the owner; observe; iterate.

## 3. Definition of ready (for a slice)

- Gherkin scenarios exist and are reviewed.
- Schemas exist or have a draft delta.
- Affected ADRs identified.
- Test layers planned (which tests in which module).
- Privacy implications reviewed against `privacy-threat-model.md`.

## 4. Definition of done (for a slice)

- All scenarios pass via automated tests at appropriate layer.
- Care-engine changes carry table-driven tests and a new `engineVersion` bump.
- Schemas validated round-trip (encode/decode/equality).
- Android UI test covers the golden path.
- Backend integration test covers the API.
- AI changes pass `evals/` suite at the recorded threshold.
- Docs and ADRs updated.
- Owner has used the feature on their real garden.

## 5. Branching and PRs

- `main` is always shippable.
- Branch per slice: `slice/<n>-<short-name>`.
- PR template references the `.feature` file and the relevant docs/ADR.
- Required checks: unit tests, engine tests, lint, schema validation, AI evals (when prompts touched).

## 6. Environments

- **Local** — Android emulator + local backend + local Supabase (or hosted dev project).
- **Dev (cloud)** — single shared Supabase dev project. Real APIs (NWS, Open-Meteo, OpenAI) with low-volume budgets.
- **Prod** — single user (owner) initially. Hardened secrets. Production prompts pinned by version.

## 7. Test strategy

| Layer | Where | What |
|---|---|---|
| Schema | `shared-schemas/` | JSON Schema lint + cross-language round-trip |
| Care engine | `:care-engine` / backend `care-engine/` | Pure functions, table-driven, property-based for date math |
| Backend API | `backend/` | Contract tests against schemas + integration with Postgres |
| Android unit | `:domain`, `:data` | Use-case and repository tests |
| Android UI | `:feature-*` | Compose semantics tests, screenshot tests where stable |
| AI evals | `evals/` | Golden inputs, scored outputs, regression on prompt/model |
| End-to-end | TBD | Owner-driven manual flow per slice DOD |

## 8. Observability

- Structured logs: event name + ids + decision metadata. No raw photo bytes, no location strings, no plant nicknames in logs.
- Metrics: reminder dispatch latency, reminder relevance %, AI latency, AI schema-fail rate, eval regression count.
- Crash reporting: Firebase Crashlytics with PII filters; or self-hosted Sentry — TBD ADR.

## 9. Release cadence

- Slice-based releases. No fixed sprint. A slice is done when DOD is met.
- Pre-release on every merge to `main` (internal track).
- Production release per slice unless multiple ship together for cohesion.

## 10. Risks

- AI cost overruns → cap requests/day, monitor eval scores, prefer smaller models where adequate.
- Privacy regression → automated lints for forbidden log fields; periodic threat-model review.
- Care-engine drift → engineVersion stamping + golden-case regression tests in CI.
- Plant catalog quality → require source citation on every `PlantProfile` field.
