# ADR-0006 — API Authentication & Request-Scoped Supabase Client

Date: 2026-06-02
Status: Accepted

## Context

D-05 pinned auth to Supabase Auth (email magic link for Slice 1). The backend API must
authenticate requests and ensure every user only ever reads/writes their own rows.
Postgres Row-Level Security (RLS) is already enabled on all user-owned tables
(migrations 0002–0003) with owner-scoped policies (`auth.uid() = user_id`).

## Decision

**Supabase JWT forwarding with a request-scoped `@supabase/supabase-js` client.**

- The client sends `Authorization: Bearer <Supabase access token>`.
- A Fastify `preHandler` hook:
  1. Rejects requests without a valid `Bearer` token with **401**.
  2. Verifies the token and resolves the user via `supabase.auth.getUser(token)`.
  3. Builds a **request-scoped** Supabase client initialized with that token in its
     `global.headers.Authorization`, so every DB call in the handler executes **as that
     user** and Postgres RLS enforces ownership end-to-end.
  4. Attaches `{ userId, supabase }` to the request.
- Handlers set `user_id` columns to the authenticated `userId` and never trust a
  client-supplied user id. Because the request-scoped client runs under the user's JWT,
  RLS is the backstop even if a handler forgot a check.

The server uses the project URL + the anon/publishable key for the request-scoped
client (the JWT in the header is what authorizes); the **service-role key is never used
by the API server** — it is used only by integration tests to provision test users via
the admin API.

## Alternatives considered

- **Service-role client + manual `where user_id = …` filters** — rejected: bypasses
  RLS and makes a forgotten filter a cross-tenant leak. The request-scoped user client
  keeps RLS authoritative.
- **Custom JWT verification in-process** — unnecessary; `auth.getUser()` validates
  against the local GoTrue/Auth service.

## Consequences

- `@supabase/supabase-js` is a backend dependency.
- Config (`SUPABASE_URL`, `SUPABASE_ANON_KEY`) is read from the environment at app
  build time; no keys are committed.
- RLS isolation (A3b test #19) is enforced by the database, not just application code.
- Local-dev anon/service-role keys are read from `supabase status` at test runtime and
  passed via environment variables — never committed.
