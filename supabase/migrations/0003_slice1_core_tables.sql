-- 0003_slice1_core_tables.sql — Slice 1 core tables + RLS + seeded plant_profiles.
-- Mirrors shared-schemas/{plant-profile,container,plant-instance,care-task}.schema.json
-- (snake_case columns; nested sub-objects stored as jsonb). RLS owner-scoped on all
-- user-owned tables; plant_profiles is read-only catalog (SELECT for authenticated only).
-- gen_random_uuid() comes from pgcrypto (enabled by 0001).

-- ─────────────────────────────────────────────────────────────────────────────
-- plant_profiles (catalog; read-only to clients)
-- ─────────────────────────────────────────────────────────────────────────────
create table if not exists public.plant_profiles (
  id                             text primary key check (id ~ '^[a-z0-9-]+$'),
  scientific_name                text not null,
  common_names                   text[] not null,
  category                       text not null check (category in (
                                   'fruit','vegetable','herb','ornamental','vine',
                                   'root','berry','succulent','other')),
  growth_habit                   text not null check (growth_habit in (
                                   'bush','vine','trailing','upright','climbing',
                                   'rosette','tree')),
  requires_support               boolean not null default false,
  self_fruitful                  boolean,
  pollination_partners_required  integer not null default 0 check (pollination_partners_required >= 0),
  watering_profile               jsonb not null,
  feeding_profile                jsonb not null,
  container_profile              jsonb not null,
  light_profile                  jsonb not null,
  temperature_profile            jsonb not null,
  seasonality                    jsonb,
  common_issues                  text[],
  vertical_suitability           numeric check (vertical_suitability >= 0 and vertical_suitability <= 1),
  source                         jsonb,
  version                        integer not null check (version >= 1),
  last_reviewed_at               date
);

alter table public.plant_profiles enable row level security;

-- Read-only to authenticated clients; no insert/update/delete policies (catalog is
-- managed via migrations/service role only).
create policy "plant_profiles_select_authenticated" on public.plant_profiles
  for select to authenticated using (true);

-- ─────────────────────────────────────────────────────────────────────────────
-- containers (mirror container.schema.json)
-- ─────────────────────────────────────────────────────────────────────────────
create table if not exists public.containers (
  id            uuid primary key default gen_random_uuid(),
  user_id       uuid not null references auth.users (id) on delete cascade,
  name          text check (char_length(name) <= 80),
  volume_liters numeric not null check (volume_liters > 0 and volume_liters <= 10000),
  material      text not null check (material in (
                  'terracotta','plastic','fabric','glazed-ceramic','wood',
                  'metal','self-watering','other')),
  drainage      text not null check (drainage in ('good','moderate','poor')),
  self_watering boolean not null default false,
  saucer        boolean not null default false,
  soil_mix      text check (char_length(soil_mix) <= 200),
  created_at    timestamptz not null default now(),
  updated_at    timestamptz
);

alter table public.containers enable row level security;

create policy "containers_select_own" on public.containers
  for select using (auth.uid() = user_id);
create policy "containers_insert_own" on public.containers
  for insert with check (auth.uid() = user_id);
create policy "containers_update_own" on public.containers
  for update using (auth.uid() = user_id) with check (auth.uid() = user_id);
create policy "containers_delete_own" on public.containers
  for delete using (auth.uid() = user_id);

-- ─────────────────────────────────────────────────────────────────────────────
-- plant_instances (mirror plant-instance.schema.json)
-- ─────────────────────────────────────────────────────────────────────────────
create table if not exists public.plant_instances (
  id                  uuid primary key default gen_random_uuid(),
  user_id             uuid not null references auth.users (id) on delete cascade,
  profile_id          text not null references public.plant_profiles (id),
  container_id        uuid not null references public.containers (id),
  garden_space_id     uuid not null references public.garden_spaces (id),
  nickname            text check (char_length(nickname) <= 80),
  cultivar            text check (char_length(cultivar) <= 80),
  placement           text check (placement in (
                        'floor','shelf','railing','hanging','trellis',
                        'vertical-rack','windowsill')),
  placement_height_cm integer check (placement_height_cm >= 0),
  acquired_at         date,
  planted_at          date,
  last_watered_at     timestamptz,
  growth_stage        text not null check (growth_stage in (
                        'seed','seedling','vegetative','flowering','fruiting',
                        'dormant','harvested')),
  support_recorded    boolean not null default false,
  notes               text check (char_length(notes) <= 4000),
  photos              text[],
  created_at          timestamptz not null default now(),
  updated_at          timestamptz
);

alter table public.plant_instances enable row level security;

create policy "plant_instances_select_own" on public.plant_instances
  for select using (auth.uid() = user_id);
create policy "plant_instances_insert_own" on public.plant_instances
  for insert with check (auth.uid() = user_id);
create policy "plant_instances_update_own" on public.plant_instances
  for update using (auth.uid() = user_id) with check (auth.uid() = user_id);
create policy "plant_instances_delete_own" on public.plant_instances
  for delete using (auth.uid() = user_id);

-- ─────────────────────────────────────────────────────────────────────────────
-- care_tasks (mirror care-task.schema.json; user_id denormalized for simple RLS)
-- ─────────────────────────────────────────────────────────────────────────────
create table if not exists public.care_tasks (
  id                 uuid primary key default gen_random_uuid(),
  plant_instance_id  uuid not null references public.plant_instances (id) on delete cascade,
  user_id            uuid not null references auth.users (id) on delete cascade,
  kind               text not null check (kind in (
                       'water','feed','prune','repot','scout-pests','harvest',
                       'support','rotate','seasonal-prep')),
  due_at             timestamptz not null,
  priority           text not null check (priority in ('low','normal','high','urgent')),
  rationale          text not null check (char_length(rationale) <= 2000),
  rationale_metadata jsonb,
  engine_version     text not null check (engine_version ~ '^[0-9]+\.[0-9]+\.[0-9]+$'),
  inputs_hash        text not null check (char_length(inputs_hash) >= 8),
  source_inputs      jsonb not null,
  status             text not null check (status in ('pending','done','skipped','dismissed')),
  completed_at       timestamptz,
  feedback           text check (feedback in (
                       'on-time','early','late','soil-still-wet','plant-wilted',
                       'fertilizer-too-strong','skipped','watered-early')),
  created_at         timestamptz not null default now()
);

alter table public.care_tasks enable row level security;

create policy "care_tasks_select_own" on public.care_tasks
  for select using (auth.uid() = user_id);
create policy "care_tasks_insert_own" on public.care_tasks
  for insert with check (auth.uid() = user_id);
create policy "care_tasks_update_own" on public.care_tasks
  for update using (auth.uid() = user_id) with check (auth.uid() = user_id);
create policy "care_tasks_delete_own" on public.care_tasks
  for delete using (auth.uid() = user_id);

-- ─────────────────────────────────────────────────────────────────────────────
-- Seed the 5 Slice 1 plant_profiles (ids match backend/care-engine/seed-profiles.ts).
-- ─────────────────────────────────────────────────────────────────────────────
insert into public.plant_profiles (
  id, scientific_name, common_names, category, growth_habit, requires_support,
  self_fruitful, pollination_partners_required, watering_profile, feeding_profile,
  container_profile, light_profile, temperature_profile, version
) values
  (
    'passiflora-edulis', 'Passiflora edulis', array['Passion fruit','Maracujá'],
    'fruit', 'climbing', true, true, 0,
    '{"baseIntervalDays":3,"dryingTolerance":"medium"}'::jsonb,
    '{"baseIntervalDays":14}'::jsonb,
    '{"recommendedMinLiters":95}'::jsonb,
    '{"targetSunHours":6}'::jsonb,
    '{"frostSensitive":true}'::jsonb,
    1
  ),
  (
    'solanum-lycopersicum', 'Solanum lycopersicum', array['Tomato'],
    'fruit', 'vine', true, true, 0,
    '{"baseIntervalDays":2,"dryingTolerance":"low"}'::jsonb,
    '{"baseIntervalDays":7,"fruitingIntervalDays":5}'::jsonb,
    '{"recommendedMinLiters":19}'::jsonb,
    '{"targetSunHours":8}'::jsonb,
    '{"frostSensitive":true}'::jsonb,
    1
  ),
  (
    'physalis-philadelphica', 'Physalis philadelphica', array['Tomatillo'],
    'fruit', 'bush', false, false, 2,
    '{"baseIntervalDays":3,"dryingTolerance":"medium"}'::jsonb,
    '{"baseIntervalDays":10}'::jsonb,
    '{"recommendedMinLiters":19}'::jsonb,
    '{"targetSunHours":7}'::jsonb,
    '{"frostSensitive":true}'::jsonb,
    1
  ),
  (
    'fragaria-x-ananassa', 'Fragaria x ananassa', array['Strawberry'],
    'berry', 'rosette', false, true, 0,
    '{"baseIntervalDays":2,"dryingTolerance":"low"}'::jsonb,
    '{"baseIntervalDays":14,"postHarvestIntervalDays":21}'::jsonb,
    '{"recommendedMinLiters":4}'::jsonb,
    '{"targetSunHours":6}'::jsonb,
    '{"frostSensitive":false}'::jsonb,
    1
  ),
  (
    'ocimum-basilicum', 'Ocimum basilicum', array['Basil'],
    'herb', 'bush', false, true, 0,
    '{"baseIntervalDays":1.5,"dryingTolerance":"low"}'::jsonb,
    '{"baseIntervalDays":14}'::jsonb,
    '{"recommendedMinLiters":3}'::jsonb,
    '{"targetSunHours":6}'::jsonb,
    '{"frostSensitive":true}'::jsonb,
    1
  );
