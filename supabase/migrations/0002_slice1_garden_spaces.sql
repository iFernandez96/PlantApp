-- 0002_slice1_garden_spaces.sql — Slice 1 GardenSpace table + RLS.
-- Mirrors shared-schemas/garden-space.schema.json (Slice 1 subset). RLS is enabled
-- with owner-scoped policies (auth.uid() = user_id) per the privacy threat model.
-- gen_random_uuid() comes from pgcrypto, enabled by 0001_init_extensions.sql.
create table if not exists public.garden_spaces (
  id           uuid primary key default gen_random_uuid(),
  user_id      uuid not null references auth.users (id) on delete cascade,
  name         text not null check (char_length(name) between 1 and 80),
  kind         text not null check (kind in (
                 'balcony','patio','window-ledge','indoor-room',
                 'vertical-rack-zone','hanging-zone','grow-light-shelf','other')),
  indoor       boolean not null default false,
  postal_code  text,
  country_code text check (country_code ~ '^[A-Z]{2}$'),
  created_at   timestamptz not null default now(),
  updated_at   timestamptz
);

alter table public.garden_spaces enable row level security;

create policy "garden_spaces_select_own" on public.garden_spaces
  for select using (auth.uid() = user_id);
create policy "garden_spaces_insert_own" on public.garden_spaces
  for insert with check (auth.uid() = user_id);
create policy "garden_spaces_update_own" on public.garden_spaces
  for update using (auth.uid() = user_id) with check (auth.uid() = user_id);
create policy "garden_spaces_delete_own" on public.garden_spaces
  for delete using (auth.uid() = user_id);
