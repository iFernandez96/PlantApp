import { describe, it, expect, beforeAll, afterAll } from 'vitest';
import { Client } from 'pg';

// Local Supabase Postgres (confirm host/port via `supabase status`; default below).
const DB_URL = process.env.SUPABASE_DB_URL ?? 'postgresql://postgres:postgres@127.0.0.1:54322/postgres';
let client: Client;

beforeAll(async () => {
  client = new Client({ connectionString: DB_URL });
  await client.connect();
});
afterAll(async () => {
  await client?.end();
});

describe('Slice 1 DB — garden_spaces', () => {
  it('garden_spaces table exists', async () => {
    const { rows } = await client.query(
      "select 1 from information_schema.tables where table_schema = 'public' and table_name = 'garden_spaces'",
    );
    expect(rows).toHaveLength(1);
  });

  it('garden_spaces has row-level security enabled', async () => {
    const { rows } = await client.query(
      "select relrowsecurity from pg_class where oid = 'public.garden_spaces'::regclass",
    );
    expect(rows[0]?.relrowsecurity).toBe(true);
  });

  it('garden_spaces has owner RLS policies (>= 4)', async () => {
    const { rows } = await client.query(
      "select policyname from pg_policies where schemaname = 'public' and tablename = 'garden_spaces'",
    );
    expect(rows.length).toBeGreaterThanOrEqual(4);
  });
});
