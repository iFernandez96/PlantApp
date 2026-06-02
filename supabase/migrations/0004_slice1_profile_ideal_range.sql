-- 0004_slice1_profile_ideal_range.sql — Slice 2 support.
-- Enrich the seeded plant_profiles.container_profile (jsonb) with idealMinLiters /
-- idealMaxLiters so the container-size advisory can cite an ideal range. jsonb merge
-- (`||`) preserves the existing recommendedMinLiters. Ideal ranges are >= the
-- recommended minimum. plant-profile.schema.json already allows these optional fields.

update public.plant_profiles
  set container_profile = container_profile || '{"idealMinLiters":95,"idealMaxLiters":190}'::jsonb
  where id = 'passiflora-edulis';

update public.plant_profiles
  set container_profile = container_profile || '{"idealMinLiters":19,"idealMaxLiters":40}'::jsonb
  where id = 'solanum-lycopersicum';

update public.plant_profiles
  set container_profile = container_profile || '{"idealMinLiters":19,"idealMaxLiters":40}'::jsonb
  where id = 'physalis-philadelphica';

update public.plant_profiles
  set container_profile = container_profile || '{"idealMinLiters":4,"idealMaxLiters":10}'::jsonb
  where id = 'fragaria-x-ananassa';

update public.plant_profiles
  set container_profile = container_profile || '{"idealMinLiters":3,"idealMaxLiters":8}'::jsonb
  where id = 'ocimum-basilicum';
