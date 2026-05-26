# supabase

Supabase migration scaffolding. Migrations are the source of truth for the Postgres schema and are managed via the Supabase CLI (decision D-03).

## Current state

```
supabase/
  README.md
  migrations/
    0001_init_extensions.sql    Enables uuid-ossp + pgcrypto. No tables yet.
```

No tables exist. The Slice 1 table set (`plant_profiles`, `garden_spaces`, `containers`, `plant_instances`, `care_tasks`) will be added in subsequent, separately-reviewed migrations once their column-level shapes are agreed against `shared-schemas/`.

## Prerequisites

- Supabase CLI (`supabase`) installed locally. Version pinned later when first run.
- Docker (the CLI uses it to start the local stack).

## Workflow

```bash
# One-time, at repo root: link this directory to a Supabase project.
supabase init                       # writes supabase/config.toml on first run
supabase link --project-ref <ref>   # links to a remote project (later, when one exists)

# Start the local stack (Postgres + Studio + Auth + Storage).
supabase start

# Run all pending local migrations.
supabase migration up

# After editing or adding migrations, re-apply against the local DB.
supabase db reset                   # destructive: drops and re-applies from migrations/

# When pushing to a remote (later slices, once hosting is chosen per D-08).
supabase db push                    # applies new migrations to the linked remote
```

## Conventions

- Filename format: `NNNN_short_snake_case.sql`. Numbers are zero-padded to 4 digits and strictly monotonic.
- Each migration is forward-only. Never edit a migration after it has been applied to a shared environment.
- RLS will be enabled at the same time as each user-owned table is introduced — never as a separate later migration.
- No data seeds in extension/init migrations. Catalog seeds (e.g. `plant_profiles` for passion fruit, tomato, tomatillo, strawberry, basil) arrive with the migration that creates the table.

## Out of scope for Slice 1 scaffolding

- Tables.
- RLS policies (added with the tables that need them).
- Storage buckets (Slice 7).
- Edge Functions.
- Auth providers beyond email magic link (decision D-05).
