# evals/care-engine

Regression suite for the deterministic care-engine AND the natural-language `care-explanation` flow.

See `docs/ai-evaluation-plan.md`.

## Layout

```
cases/         engine inputs (PlantInstance + Profile + Container + GardenSpace + weather + feedback + clock)
expected/      golden CareTask outputs (engine) + expected explanation rubric (LLM flow)
scorers/       deep-equality scorer for engine; rubric scorer for explanation flow
report/        gitignored
```

## Engine metrics

- Deep-equality match against golden CareTask records (must be 100%).
- `engineVersion` present and matches recorded value.
- `inputsHash` reproducible across runs.

## Explanation flow metrics

- Schema validity (100%).
- Fact alignment with engine rationale (rubric scorer): mentions same drivers as the engine's rationale (e.g. "hot weather", "container size", "post-harvest").
- No contradiction with the engine's `dueAt` or `kind` (rubric scorer + assertion).
- No fabricated facts (rubric scorer).

## Slice 1 minimal golden cases (lock these first)

These are the engine-only goldens that must exist and pass before any Slice 1 implementation merges. They do **not** involve weather, feedback, or the explanation LLM flow.

S1.1 Passion fruit in a 19L barrel — initial water task generated.
S1.2 Tomato in a 19L container — initial water task generated.
S1.3 Tomatillo in a 19L container — initial water task generated.
S1.4 Strawberry in a 4L container (small) — containerFactor clamps at 0.5; dueAt sooner.
S1.5 Basil in a 50L container (oversized) — containerFactor clamps at 1.5; dueAt later.
S1.6 Determinism: identical sourceInputs (including `clockUtc`) produce equal CareTask output and equal `inputsHash`.
S1.7 Recompute with a later clock: produces a different `inputsHash` and later `dueAt`; same shape of `sourceInputs`.

## Case categories to seed (slices 3+)

1. Container tomato in 7L pot, 3-day hot forecast, last watered yesterday → water due within 24h.
2. Container tomato, 19L pot, rainy forecast → water due ≥ 48h out.
3. Passion fruit, 19L pot, mid-summer → container-size advisory + water cadence respects watering profile.
4. Tomatillo, 1 instance → pollination warning surfaced (not a CareTask).
5. Tomatillo, 2 instances → warning cleared.
6. Strawberry post-harvest event → feed CareTask in post-harvest window.
7. "Soil-still-wet" feedback → next water pushed ≥ 24h.
8. "Fertilizer-too-strong" feedback → next feed delayed by ≥ one interval.
9. Overdue water (24h past) → task escalated to "high", no new task created.
10. Dormant plant → no feed task.
11. Freeze warning tonight → seasonal-prep task created; non-critical tasks deferred.
12. Weather provider down → tasks produced with `rationaleMetadata.degraded = true`.
