# ADR-0005 — Backend Web Framework

Date: 2026-06-02
Status: Accepted

## Context

D-01 pinned the backend runtime to Node.js + TypeScript but left the HTTP framework
unpinned. Slice 1 A3 needs an HTTP API for the add-plant flow (`POST /plants` →
`computeInitialWaterTask` → persist `CareTask`) plus the supporting inventory
endpoints, covered by integration tests #15–#20.

## Decision

Use **Fastify** (v5) as the backend web framework.

Rationale:
- First-class TypeScript support and typed route handlers.
- Built-in `app.inject()` lets integration tests exercise the full request pipeline
  (routing, validation, auth hook, handlers) without binding a real port — fast and
  deterministic under Vitest.
- JSON-schema-based request validation is built in, which complements the shared
  JSON Schemas already in `shared-schemas/`.
- Low overhead, mature plugin ecosystem.

## Alternatives considered

- **Express** — ubiquitous but weaker TS story and no first-class inject/validation.
- **Hono** — strong, but Fastify's `inject()` + schema validation fit the
  test-first workflow more directly for a Node server.
- **NestJS** — too heavy for a single-slice MVP API.

## Consequences

- `fastify` is a backend dependency.
- Route handlers validate input via Fastify route schemas (400 on missing required
  fields) plus explicit existence/ownership checks where needed.
- Integration tests use `buildApp()` + `app.inject()`; no port binding in tests.
