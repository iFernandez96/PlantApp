# PlantApp

Android-first container-garden intelligence app for small spaces — condos, balconies, upstairs patios. Combines a deterministic care-rules engine (watering, feeding, seasonal tasks) with local climate awareness and AI-assisted photo diagnosis and space optimization. Designed to scale from one user's real garden to many plant species without code changes.

## Status

**Foundation + scaffolding only; no production behavior.** Build files, empty modules, placeholder migrations, and the working contract are committed. No Kotlin source, no HTTP routes, no database tables beyond enabled extensions, no AI/notifications/photos/weather.

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
| Backend module skeleton | ✅ in `backend/` — Node.js + TypeScript, no production behavior |
| Supabase migration scaffold | ✅ in `supabase/` — extensions only; no tables yet |
| Android module skeleton | ✅ in `android/` — 6-module Gradle skeleton, no Kotlin source |
| Root task runner | ✅ in `justfile` — delegates to backend and Android tools |
| CI | ❌ not configured |

## Repository layout

```
CLAUDE.md                Working contract for Claude Code in this repo.
README.md                This file.
justfile                 Root task runner: lint / test / typecheck / build /
                         validate-schemas. Thin pass-through to backend and
                         Android tools.

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

backend/                 Node.js + TypeScript skeleton. ESLint 9 flat config,
                         Prettier, Vitest, Ajv 2020-12. The deterministic
                         care-engine module exists as a placeholder
                         (`backend/care-engine/index.ts` exports nothing).
                         No HTTP server, no Supabase client wiring yet.
supabase/                Supabase migrations workspace. `migrations/` holds
                         the placeholder `0001_init_extensions.sql` which
                         enables `uuid-ossp` and `pgcrypto`. No tables yet.
android/                 Gradle multi-module skeleton with the Slice 1
                         modules only: `:app`, `:design-system`, `:domain`,
                         `:data`, `:network`, `:feature-inventory`. Version
                         catalog at `android/gradle/libs.versions.toml`.
                         No production Kotlin source. The wrapper jar and
                         `gradlew` scripts are not committed — see
                         `android/README.md` to generate them.
```

## Default branch

`master`.

## Current phase

**Post-scaffolding review before Slice 1 business logic.** The foundation docs are in agreement; the empty module skeletons are committed; the next pass is a project-subagent review of the scaffolding and the docs that describe it, followed by the first failing tests of the Slice 1 plan.

## Next step

Run project subagent reviews, fix any scaffold issues they surface, then begin the first failing tests listed in `docs/slice-01-implementation-plan.md`. No business logic until those tests are red.

## ⚠️ Do not implement production behavior yet

The build is green-by-emptiness today. Adding Slice 1 business logic requires (a) a passing subagent review of the scaffolding, (b) red-first tests in each layer per the implementation plan, and (c) explicit owner approval to begin.

See `docs/repo-hygiene.md` for branch strategy, commit style, and the PR checklist.
