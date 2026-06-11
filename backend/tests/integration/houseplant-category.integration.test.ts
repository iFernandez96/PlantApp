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

describe('W2 Gate B — plant_profiles.category accepts houseplant', () => {
  it('check constraint allows houseplant (transaction rolled back)', async () => {
    await client.query('begin');
    try {
      await client.query(
        "update plant_profiles set category = 'houseplant' where id = 'ocimum-basilicum'",
      );
      const { rows } = await client.query(
        "select category from plant_profiles where id = 'ocimum-basilicum'",
      );
      expect(rows[0].category).toBe('houseplant');
    } finally {
      await client.query('rollback');
    }
  });
});
