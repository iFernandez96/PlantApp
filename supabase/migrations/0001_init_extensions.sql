-- Migration: 0001_init_extensions
-- Purpose:   Enable Postgres extensions required by later Slice 1 migrations.
-- Notes:     This migration intentionally creates NO tables. Tables for
--            plant_profiles, garden_spaces, containers, plant_instances,
--            care_tasks, and care_log_events arrive in subsequent migrations
--            once their schemas are reviewed.
--
-- References:
--   docs/slice-01-implementation-plan.md  - Slice 1 backend scope
--   docs/slice-01-decision-log.md          - D-03 Supabase migrations CLI
--   shared-schemas/*.schema.json           - JSON Schemas the tables must mirror

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
