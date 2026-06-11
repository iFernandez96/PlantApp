-- Baseline grants (W2 harness fix): newer Supabase CLI resets no longer apply the
-- platform-default grants, leaving every API call at 42501 permission-denied.
-- These are the standard Supabase defaults; row access remains governed by RLS
-- (enabled on all tables; anon has no policies and therefore sees no rows).
grant usage on schema public to anon, authenticated, service_role;
grant all on all tables    in schema public to anon, authenticated, service_role;
grant all on all sequences in schema public to anon, authenticated, service_role;
grant all on all functions in schema public to anon, authenticated, service_role;
alter default privileges in schema public grant all on tables    to anon, authenticated, service_role;
alter default privileges in schema public grant all on sequences to anon, authenticated, service_role;
alter default privileges in schema public grant all on functions to anon, authenticated, service_role;
