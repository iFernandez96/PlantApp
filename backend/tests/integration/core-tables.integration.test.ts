import { describe, it, expect, beforeAll, afterAll } from 'vitest';
import { Client } from 'pg';

const DB_URL = process.env.SUPABASE_DB_URL ?? 'postgresql://postgres:postgres@127.0.0.1:54322/postgres';
let client: Client;

beforeAll(async () => {
  client = new Client({ connectionString: DB_URL });
  await client.connect();
});
afterAll(async () => {
  await client?.end();
});

const TABLES = ['plant_profiles', 'containers', 'plant_instances', 'care_tasks'];
const SEED_PROFILE_IDS = [
  'fragaria-x-ananassa',
  'ocimum-basilicum',
  'passiflora-edulis',
  'physalis-philadelphica',
  'solanum-lycopersicum',
];

describe('Slice 1 DB — core tables', () => {
  it.each(TABLES)('table %s exists', async (t) => {
    const { rows } = await client.query(
      "select 1 from information_schema.tables where table_schema = 'public' and table_name = $1",
      [t],
    );
    expect(rows).toHaveLength(1);
  });

  it.each(TABLES)('table %s has row-level security enabled', async (t) => {
    const { rows } = await client.query(
      "select relrowsecurity from pg_class where oid = ('public.' || $1)::regclass",
      [t],
    );
    expect(rows[0]?.relrowsecurity).toBe(true);
  });

  it('plant_profiles is seeded with the 5 Slice 1 profiles', async () => {
    const { rows } = await client.query('select id from public.plant_profiles order by id');
    expect(rows.map((r) => r.id)).toEqual(SEED_PROFILE_IDS);
  });
});
