// Slice 1 API integration tests #19 (RLS isolation) + #20 (delete cascade).
// Two users provisioned via the service-role admin API; each acts with its own bearer.
// A direct pg query confirms the care_tasks cascade. RED-FIRST: GET /plants, GET
// /plants/:id, and DELETE /plants/:id don't exist yet, so the relevant assertions fail
// until the green commit.
import { describe, it, expect, beforeAll, afterAll } from 'vitest';
import { createClient, type SupabaseClient } from '@supabase/supabase-js';
import { Client } from 'pg';
import type { FastifyInstance } from 'fastify';

const SUPABASE_URL = process.env.SUPABASE_URL ?? process.env.API_URL ?? 'http://127.0.0.1:54321';
const ANON_KEY = process.env.SUPABASE_ANON_KEY ?? process.env.ANON_KEY ?? '';
const SERVICE_ROLE_KEY = process.env.SUPABASE_SERVICE_ROLE_KEY ?? process.env.SERVICE_ROLE_KEY ?? '';
const DB_URL = process.env.SUPABASE_DB_URL ?? 'postgresql://postgres:postgres@127.0.0.1:54322/postgres';

let app: FastifyInstance;
let admin: SupabaseClient;
let db: Client;
const users: Array<{ id: string; token: string; email: string }> = [];

async function provisionUser(label: string): Promise<{ id: string; token: string; email: string }> {
  const email = `slice1-${label}-${Date.now()}-${Math.floor(performance.now())}@example.test`;
  const password = 'Test-Passw0rd!';
  const created = await admin.auth.admin.createUser({ email, password, email_confirm: true });
  if (created.error) throw created.error;
  const anon = createClient(SUPABASE_URL, ANON_KEY, {
    auth: { persistSession: false, autoRefreshToken: false },
  });
  const signedIn = await anon.auth.signInWithPassword({ email, password });
  if (signedIn.error) throw signedIn.error;
  return { id: created.data.user!.id, token: signedIn.data.session!.access_token, email };
}

beforeAll(async () => {
  if (!ANON_KEY || !SERVICE_ROLE_KEY) {
    throw new Error(
      'Missing ANON_KEY/SERVICE_ROLE_KEY env. Run: set -a; eval "$(npx supabase status -o env)"; set +a; npm run test:int',
    );
  }
  admin = createClient(SUPABASE_URL, SERVICE_ROLE_KEY, {
    auth: { persistSession: false, autoRefreshToken: false },
  });
  db = new Client({ connectionString: DB_URL });
  await db.connect();

  users.push(await provisionUser('a'));
  users.push(await provisionUser('b'));

  const { buildApp } = await import('../../src/app.js');
  app = await buildApp();
  await app.ready();
});

afterAll(async () => {
  await app?.close();
  await db?.end();
  for (const u of users) {
    await admin.auth.admin.deleteUser(u.id);
  }
});

function bearer(token: string) {
  return { authorization: `Bearer ${token}` };
}

async function createPlantFor(token: string): Promise<string> {
  const gs = await app.inject({
    method: 'POST',
    url: '/garden-spaces',
    headers: bearer(token),
    payload: { name: 'West Balcony', kind: 'balcony' },
  });
  expect(gs.statusCode).toBe(201);
  const c = await app.inject({
    method: 'POST',
    url: '/containers',
    headers: bearer(token),
    payload: { name: 'Blue barrel', volumeLiters: 19, material: 'plastic', drainage: 'good' },
  });
  expect(c.statusCode).toBe(201);
  const p = await app.inject({
    method: 'POST',
    url: '/plants',
    headers: bearer(token),
    payload: {
      profileId: 'solanum-lycopersicum',
      containerId: c.json().id,
      gardenSpaceId: gs.json().id,
      growthStage: 'vegetative',
      lastWateredAt: '2026-05-26T07:00:00.000Z',
    },
  });
  expect(p.statusCode).toBe(201);
  return p.json().plant.id as string;
}

describe('Slice 1 API — RLS isolation (#19) + delete cascade (#20)', () => {
  it('#19 a second user cannot read user A\'s plant or its tasks', async () => {
    const userA = users[0]!;
    const userB = users[1]!;
    const plantId = await createPlantFor(userA.token);

    // User B's list must not contain A's plant.
    const listB = await app.inject({ method: 'GET', url: '/plants', headers: bearer(userB.token) });
    expect(listB.statusCode).toBe(200);
    const idsB = (listB.json() as Array<{ id: string }>).map((r) => r.id);
    expect(idsB).not.toContain(plantId);

    // User B fetching A's plant by id → 404.
    const getB = await app.inject({ method: 'GET', url: `/plants/${plantId}`, headers: bearer(userB.token) });
    expect(getB.statusCode).toBe(404);

    // User B fetching A's plant tasks → 404 or empty.
    const tasksB = await app.inject({ method: 'GET', url: `/plants/${plantId}/tasks`, headers: bearer(userB.token) });
    expect([404, 200]).toContain(tasksB.statusCode);
    if (tasksB.statusCode === 200) {
      expect(tasksB.json()).toEqual([]);
    }

    // Sanity: user A can read their own plant.
    const getA = await app.inject({ method: 'GET', url: `/plants/${plantId}`, headers: bearer(userA.token) });
    expect(getA.statusCode).toBe(200);
    expect(getA.json().id).toBe(plantId);
  });

  it('#20 DELETE /plants/:id removes the plant and cascades its CareTasks', async () => {
    const userA = users[0]!;
    const plantId = await createPlantFor(userA.token);

    // Precondition: one care_task exists for this plant (direct DB check).
    const before = await db.query('select count(*)::int as n from public.care_tasks where plant_instance_id = $1', [plantId]);
    expect(before.rows[0]?.n).toBe(1);

    const del = await app.inject({ method: 'DELETE', url: `/plants/${plantId}`, headers: bearer(userA.token) });
    expect(del.statusCode).toBe(204);

    // Plant no longer readable.
    const getA = await app.inject({ method: 'GET', url: `/plants/${plantId}`, headers: bearer(userA.token) });
    expect(getA.statusCode).toBe(404);

    // Tasks gone via API.
    const tasksA = await app.inject({ method: 'GET', url: `/plants/${plantId}/tasks`, headers: bearer(userA.token) });
    expect([404, 200]).toContain(tasksA.statusCode);
    if (tasksA.statusCode === 200) {
      expect(tasksA.json()).toEqual([]);
    }

    // Cascade confirmed at the DB: 0 care_tasks rows for this plant.
    const after = await db.query('select count(*)::int as n from public.care_tasks where plant_instance_id = $1', [plantId]);
    expect(after.rows[0]?.n).toBe(0);
  });

  it('#20b deleting a plant not owned by the caller → 404', async () => {
    const userA = users[0]!;
    const userB = users[1]!;
    const plantId = await createPlantFor(userA.token);
    const del = await app.inject({ method: 'DELETE', url: `/plants/${plantId}`, headers: bearer(userB.token) });
    expect(del.statusCode).toBe(404);
    // A's plant still there.
    const getA = await app.inject({ method: 'GET', url: `/plants/${plantId}`, headers: bearer(userA.token) });
    expect(getA.statusCode).toBe(200);
  });
});
