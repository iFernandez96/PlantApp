# evals/space-optimizer

Evaluation suite for the AI space-optimizer flow.

See `docs/ai-evaluation-plan.md`.

## Layout

```
cases/         input fixtures: { contextJson, photos/, measurements }
expected/      gold expected tradeoffs and structural requirements per case
scorers/       schema validity, horizontal+vertical presence, tradeoff overlap, sun-plausibility
report/        gitignored
```

## Required structural checks

- Schema validity (100%).
- Each response includes ≥ 1 horizontal option AND ≥ 1 vertical option (unless heightCm < 200, in which case vertical is optional but `verticalCapacityComputed` must be false).
- Each option includes ≥ 1 tradeoff.
- `placements` only reference input `plantInstanceId`s.
- Sun-plausibility rule check: no sun-loving species placed in lowest tier of a south-facing balcony without flagging a tradeoff.

## Case categories to seed

1. Small south-facing balcony, height 2.4m, 3 plants → must include vertical tier; should pick fruiting tomato for top sun tier.
2. Wind-exposed west railing, height 2.0m → vertical option must include weight/wind tradeoff.
3. Indoor window-ledge → no outdoor-only species in placements; indoor-light caveat present.
4. Shallow shelf (height 0.6m) → vertical may be omitted; `verticalCapacityComputed` = false.
5. Mixed plant set including passion fruit + tomato + strawberries → trellis support recorded for passion fruit; strawberries placed where reachable; tomato in sun-strongest tier.
6. Out-of-scope: photo of a bedroom (no plants, no garden context) → `status: "out_of_scope"`.
