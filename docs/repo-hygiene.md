# Repo Hygiene

How we work in this repository.

## Branching

- Default branch: **`master`**.
- `master` is always shippable. No direct broken commits.
- One branch per slice: `slice/<n>-<short-name>` (e.g. `slice/1-plant-inventory`).
- Sub-branches for sub-tasks within a slice: `slice/<n>-<short-name>/<subtask>`.
- Spike / exploration branches: `spike/<topic>` — never merged directly; findings are folded into a slice branch.
- Documentation-only branches: `docs/<topic>`.

## Commit style

Conventional Commits, lowercase type, imperative subject.

```
<type>(<optional-scope>): <subject in imperative mood>

<body — what changed and, more importantly, why>

<optional footers — Co-Authored-By, Refs #issue, Closes #issue>
```

Types: `feat`, `fix`, `chore`, `docs`, `test`, `refactor`, `perf`, `build`, `ci`, `revert`.

- Subject ≤ 72 chars, no trailing period.
- Body wraps at ~80 chars.
- Focus the body on **why**; the diff already shows the what.
- Co-author tag at the bottom when AI-assisted.

Examples:

```
docs: pin slice 1 backend-only care-engine decision
feat(care-engine): add container-factor clamp at [0.5, 1.5]
chore: tighten repo foundation before slice 1 implementation
```

## Pull request checklist

Before requesting review:

- [ ] Linked to a slice in `docs/roadmap.md` (or labeled `docs-only` / `chore`).
- [ ] Gherkin scenarios added or updated in `features/`, tagged with the correct `@slice-N`.
- [ ] Affected schemas in `shared-schemas/` updated (and version bumped if breaking).
- [ ] ADRs added or updated when boundaries, dependencies, or AI behavior changed.
- [ ] `docs/architecture.md` and/or `docs/domain-model.md` updated if the design moved.
- [ ] `CLAUDE.md` updated if a working rule changed.
- [ ] No secrets, photos, location strings, or user identifiers in the diff.
- [ ] No `.env`, keystore, service-account file, or other credential committed.
- [ ] Privacy posture re-checked against `docs/privacy-threat-model.md`.

## Required tests before merge (once code exists)

These gates apply per slice as soon as the code that backs them lands. Not all gates exist yet — they appear in sequence with the slices.

- **Schema validation tests** — every schema in `shared-schemas/` is exercised on both backend and Android.
- **Care-engine unit tests** — table-driven, deterministic, with `engineVersion` and `inputsHash` reproducibility assertions.
- **Backend contract / integration tests** — endpoints validated against shared schemas; RLS asserted per user-owned table.
- **Android UI tests** — Compose semantics for golden paths of the active slice.
- **AI evaluation suites** — only when a prompt or model change is part of the PR; the suite under `evals/<flow>/` must meet the recorded thresholds.
- **BDD scenario coverage** — every `@slice-N` scenario for the active slice has at least one automated test that exercises it.

## What never gets committed

- Real photos of plants, gardens, balconies, or any home interior — synthetic or sanitized fixtures only, stored outside the repo until a process exists in Slice 7.
- Precise location data (lat/lon, addresses).
- Real user identifiers (email, phone, full names beyond the owner's name in author metadata).
- API keys, service-account JSON, signing keystores, `.env` files.
- AI eval `report/` outputs (gitignored; regenerated locally and in CI).

## When in doubt

- Read `CLAUDE.md`.
- Read the most relevant doc under `docs/`.
- If still unclear, open a discussion or a `feature` issue before writing code.
