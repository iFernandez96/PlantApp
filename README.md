# PlantApp

Android-first container-garden intelligence app for small spaces — condos, balconies, upstairs patios. Combines a deterministic care-rules engine (watering, feeding, seasonal tasks) with local climate awareness and AI-assisted photo diagnosis and space optimization. Designed to scale from one user's real garden to many plant species without code changes.

## Status

**Planning / BDD / schema foundation only.** No production code has been written.

| Area | State |
|---|---|
| Product, architecture, domain, SDLC, privacy, AI, data-source docs | ✅ in `docs/` |
| Architecture Decision Records | ✅ in `docs/adr/` (some pins still awaiting owner approval) |
| Gherkin feature files (BDD source of truth) | ✅ in `features/`, tagged by slice |
| Shared JSON Schemas (cross-boundary contracts) | ✅ in `shared-schemas/` |
| Versioned AI system prompts | ✅ in `prompts/` |
| AI evaluation suite scaffolds | ✅ in `evals/` |
| Slice 1 implementation plan | ✅ in `docs/slice-01-implementation-plan.md` |
| Slice 1 decision log (12 pins) | ✅ in `docs/slice-01-decision-log.md` — **awaiting owner approval** |
| Android module skeleton | ❌ not initialized |
| Backend module skeleton | ❌ not initialized |
| CI | ❌ not configured |

## Repository layout

```
CLAUDE.md                Working contract for Claude Code in this repo.
docs/                    Product, architecture, domain, SDLC, privacy, AI,
                         data-source docs, the roadmap, and the Slice 1
                         implementation plan + decision log.
docs/adr/                Append-only Architecture Decision Records.
features/                Gherkin .feature files — BDD source of truth.
                         Scenarios are tagged @slice-1 .. @slice-9.
shared-schemas/          JSON Schemas (2020-12) shared by Android, backend,
                         and AI structured outputs.
prompts/                 Versioned system prompts for AI flows.
evals/                   AI evaluation harness scaffolds (per flow).
```

`android/` and `backend/` are intentionally absent — they appear when Slice 1 implementation begins.

## Default branch

`master`.

## Current phase

**Pre-Slice 1 implementation.** Awaiting owner approval of the 12 proposed decisions (D-01 … D-12) in `docs/slice-01-decision-log.md` before any Android or backend code is written.

## Next step

1. Read `docs/slice-01-implementation-plan.md` and `docs/slice-01-decision-log.md`.
2. Approve or modify each of D-01 through D-12.
3. Once approved, those decisions will be reflected in ADR-0002 and ADR-0003 and Slice 1 implementation may begin.

## ⚠️ Do not implement production code yet

No Android or backend production code is to be written until the 12 Slice 1 decisions are explicitly approved. The current repo is a contract — code follows after the contract is locked.

See `docs/repo-hygiene.md` for branch strategy, commit style, and the PR checklist.
