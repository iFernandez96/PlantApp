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

describe('baseline grants survive db reset (0006)', () => {
  it('api roles have usage on schema public', async () => {
    const { rows } = await client.query(
      "select r as role, has_schema_privilege(r, 'public', 'usage') as ok from unnest(array['anon','authenticated','service_role']) as r",
    );
    expect(rows.filter((x) => !x.ok)).toEqual([]);
  });

  it('authenticated can select plant_profiles at the table-grant level', async () => {
    const { rows } = await client.query(
      "select 1 from information_schema.role_table_grants where table_schema = 'public' and table_name = 'plant_profiles' and grantee = 'authenticated' and privilege_type = 'SELECT'",
    );
    expect(rows.length).toBeGreaterThan(0);
  });
});
