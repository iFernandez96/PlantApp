# PlantApp — AI Evaluation Plan

Status: Draft v0.1 — 2026-05-26

## 1. Goals

- Catch regressions when prompts or models change.
- Set quality floors per AI flow before production rollout.
- Generate auditable scores for every prompt version.

## 2. Evaluation suites

Each suite lives under `evals/<flow>/` and contains:

- `cases/` — input fixtures (synthetic, sanitized images, mock plant context).
- `expected/` — gold labels (allowed findings, required fields, forbidden claims).
- `scorers/` — Python or Node scripts implementing per-case scoring.
- `report/` — generated, gitignored.

### 2.1 `evals/plant-diagnosis/`

Cases include:
- Healthy tomato leaf (negative-case: must not invent disease).
- Early blight (positive-case: must include "fungal" + recommend removal).
- Nutrient deficiency vs. underwatering ambiguity (must report multiple suspected causes with confidences).
- Out-of-scope photo (e.g. cat) — must return `out_of_scope`.
- Prompt-injection embedded in OCR-able text — must ignore.

Metrics:
- Schema validity (must be 100%).
- Label precision/recall against gold.
- Confidence calibration (Brier score) on labeled cases.
- Out-of-scope detection rate.
- Prompt-injection compliance rate (must be 100%).

### 2.2 `evals/care-engine/`

The care engine is deterministic; "AI evaluation" here means **golden-case regression** on the rules engine and on the natural-language `care-explanation` flow.

Cases include:
- Container tomato in 5-gallon, hot week, last watered 2d ago.
- Passion fruit in 5-gallon, mid-summer, profile says min-recommended 25L.
- Tomatillo, single-plant household, must surface pollination warning.
- Strawberry post-harvest feeding interval.
- User feedback "soil still wet" — must push next watering out, not advance it.

Metrics:
- Engine output equality with golden CareTask records.
- `engineVersion` stamped on every output.
- Explanation flow: schema validity + factual alignment with engine rationale (LLM-as-judge scorer with a fixed rubric, plus spot-checks).

### 2.3 `evals/space-optimizer/`

Cases include:
- 1.2m × 0.8m balcony, south-facing, 6 sun hours, 3 plants — must include vertical option.
- Wind-exposed railing with hanging weight risk — must flag wind tradeoff.
- Window ledge indoor — must not propose outdoor-only plants for the zone.

Metrics:
- Schema validity (100%).
- Includes both horizontal and vertical proposals (binary).
- Captures top-3 expected tradeoffs (overlap score).
- Sun-mapping plausibility (rule-based check against declared `direction`/`sunHoursEstimate`).

## 3. Scoring philosophy

- Prefer rule-based + structured scorers over LLM-as-judge.
- Where LLM-as-judge is used, the rubric is fixed and versioned alongside the suite.
- Each metric has a numeric threshold required for production rollout of a prompt or model change.

## 4. Workflow

- Local: `npm run evals` (or chosen runner) executes all suites against the current prompt versions and produces a `report/`.
- CI: runs on any change to `prompts/`, `shared-schemas/`, or `ai-gateway/`.
- Pre-production gate: report must meet or exceed each suite's threshold or the prompt deploy is blocked.

## 5. Data hygiene

- All eval inputs are synthetic or sanitized owner-provided. No third-party images without permission.
- No production user data ever lands in `evals/`.
- Photo fixtures stored with stripped EXIF and in a dedicated `cases/photos/` subfolder.

## 6. Open items

- Choice of eval runner (OpenAI Evals, Promptfoo, Inspect, custom thin runner) — TBD ADR after MVP slice 7.
- Image-fixture licensing process.
- Calibration data volume target before first AI flow ships.
