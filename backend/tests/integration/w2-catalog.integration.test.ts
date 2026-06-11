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

// Grows with each W2 seed batch (batch 2..4 update this constant).
const EXPECTED_PROFILE_COUNT = 20;

const BATCH1_IDS = [
  'allium-fistulosum', 'allium-sativum', 'beta-vulgaris', 'beta-vulgaris-cicla',
  'brassica-oleracea-acephala', 'brassica-oleracea-italica', 'capsicum-annuum',
  'cucumis-sativus', 'cucurbita-pepo', 'daucus-carota', 'lactuca-sativa',
  'phaseolus-vulgaris', 'pisum-sativum', 'raphanus-sativus',
  'solanum-lycopersicum', 'spinacia-oleracea',
];

describe('W2 catalog batch 1 — vegetables + roots', () => {
  it('all 16 batch-1 profiles are present', async () => {
    const { rows } = await client.query(
      'select id from public.plant_profiles where id = any($1) order by id',
      [BATCH1_IDS],
    );
    expect(rows.map((r) => r.id)).toEqual(BATCH1_IDS);
  });

  it('every batch-1 profile carries citations and a version', async () => {
    const { rows } = await client.query(
      "select id from public.plant_profiles where id = any($1) and (source is null or jsonb_array_length(source) = 0 or version < 1)",
      [BATCH1_IDS],
    );
    expect(rows).toEqual([]);
  });

  it('tomato was enriched in place, not duplicated', async () => {
    const { rows } = await client.query(
      "select category, version from public.plant_profiles where id = 'solanum-lycopersicum'",
    );
    expect(rows).toHaveLength(1);
    expect(rows[0].category).toBe('vegetable');
    expect(rows[0].version).toBeGreaterThanOrEqual(2);
  });

  it('catalog total matches the seeded batches', async () => {
    const { rows } = await client.query('select count(*)::int as n from public.plant_profiles');
    expect(rows[0].n).toBe(EXPECTED_PROFILE_COUNT);
  });
});
