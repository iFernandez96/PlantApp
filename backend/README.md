# backend

PlantApp backend — Node.js + TypeScript. **Scaffolding only as of this commit; no production behavior yet.**

Pinned per accepted decisions:
- API runtime: Node.js + TypeScript (D-01).
- JSON Schema validator: Ajv 2020-12 (D-06).
- Care-engine: backend-only for Slice 1 (D-09); a Kotlin port is deferred until offline scheduling is needed.

## Layout

```
backend/
  package.json                         scripts + dependency declarations
  tsconfig.json                        strict TypeScript config
  eslint.config.js                     ESLint 9 flat config + typescript-eslint + prettier
  .prettierrc.json                     formatting rules
  vitest.config.ts                     unit-test config
  vitest.integration.config.ts         integration-test config
  care-engine/
    index.ts                           placeholder; deterministic watering rule lands later
```

The API surface (Express/Fastify/Hono — TBD when first endpoint lands) is intentionally absent. Slice 1 adds the care-engine first; the HTTP layer follows in the same slice once the engine's tests are green.

## Scripts

```bash
npm install              # one-time
npm run lint             # eslint .
npm test                 # vitest run (unit)
npm run test:int         # vitest run (integration; requires a local Postgres in later slices)
npm run typecheck        # tsc --noEmit
npm run build            # tsc -p tsconfig.json
npm run validate-schemas # ajv compile every shared-schemas/*.schema.json
```

## A note on versions

Dependency versions in `package.json` are the latest known stable releases at scaffolding time. Verify against current registry data before running `npm install` — the toolchain (TypeScript, Vitest, ESLint, typescript-eslint, Ajv, Prettier) moves quickly. If a version no longer resolves or has known regressions, bump it and record the rationale in the next commit message.
