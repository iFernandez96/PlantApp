# PlantApp

Android-first container-garden intelligence app for small spaces — condos, balconies, upstairs patios. Combines a deterministic care-rules engine (watering, feeding, seasonal tasks) with local climate awareness and AI-assisted photo diagnosis and space optimization. Designed to scale from one user's real garden to many plant species without code changes.

## Status

**Planning / BDD / schema foundation only.** No production code has been written.

| Area | State |
|---|---|
| Product, architecture, domain, SDLC, privacy, AI, data-source docs | ✅ in `docs/` |
| Architecture Decision Records | ✅ in `docs/adr/` (all Slice 1 pins accepted 2026-05-26) |
| Gherkin feature files (BDD source of truth) | ✅ in `features/`, tagged by slice |
| Shared JSON Schemas (cross-boundary contracts) | ✅ in `shared-schemas/` |
| Versioned AI system prompts | ✅ in `prompts/` |
| AI evaluation suite scaffolds | ✅ in `evals/` |
| Slice 1 implementation plan | ✅ in `docs/slice-01-implementation-plan.md` |
| Slice 1 decision log (D-01 … D-12) | ✅ in `docs/slice-01-decision-log.md` — **all twelve accepted 2026-05-26** |
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

**Pre-Slice 1 scaffolding.** All twelve Slice 1 decisions (D-01 … D-12) were accepted on 2026-05-26. The accepted pins are reflected in ADR-0002, ADR-0003, and `docs/slice-01-implementation-plan.md`. The next step is initializing the empty Android and backend module skeletons (build files and empty modules only — no production behavior yet).

## Next step

Initialize Slice 1 scaffolding: backend Node.js/TypeScript skeleton, Supabase migrations folder, Android Gradle multi-module skeleton, and root task runner. **Build files and empty modules only — no business logic.**

Slice 1 implementation begins only after the scaffolding is reviewed, per `docs/slice-01-implementation-plan.md`.

## ⚠️ Do not implement production behavior yet

Decisions are locked but the code is not. The current repo remains a contract; scaffolding (build files + empty modules) lands next, followed by Slice 1 implementation only after the scaffolding is reviewed.

See `docs/repo-hygiene.md` for branch strategy, commit style, and the PR checklist.
