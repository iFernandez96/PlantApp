-- W2 Gate B (PD-14): the catalog adds houseplants (pothos, monstera, snake plant, ...).
-- Widen the category vocabulary; keep every existing value.
alter table public.plant_profiles
  drop constraint plant_profiles_category_check;
alter table public.plant_profiles
  add constraint plant_profiles_category_check
  check (category in (
    'fruit','vegetable','herb','ornamental','vine',
    'root','berry','succulent','houseplant','other'));
