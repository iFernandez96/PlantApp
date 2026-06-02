// Slice 1 add-plant API integration tests (#15–#18). Uses Fastify app.inject() (no port).
// RED-FIRST: backend/src/app.ts does not exist yet, so buildApp is dynamically imported
// in beforeAll — the suite fails in setup until the server lands (green commit).
//
// Env (read at runtime from `supabase status -o env`, never committed):
//   API_URL / SUPABASE_URL, ANON_KEY / SUPABASE_ANON_KEY, SERVICE_ROLE_KEY.
import { describe, it, expect, beforeAll, afterAll } from 'vitest';
import { createClient, type SupabaseClient } from '@supabase/supabase-js';
import type { FastifyInstance } from 'fastify';

const SUPABASE_URL = process.env.SUPABASE_URL ?? process.env.API_URL ?? 'http://127.0.0.1:54321';
const ANON_KEY = process.env.SUPABASE_ANON_KEY ?? process.env.ANON_KEY ?? '';
const SERVICE_ROLE_KEY = process.env.SUPABASE_SERVICE_ROLE_KEY ?? process.env.SERVICE_ROLE_KEY ?? '';

let app: FastifyInstance;
let admin: SupabaseClient;
let accessToken: string;
let userId: string;
const testEmail = `slice1-${Date.now()}@example.test`;
const testPassword = 'Test-Passw0rd!';

beforeAll(async () => {
  if (!ANON_KEY || !SERVICE_ROLE_KEY) {
    throw new Error(
      'Missing ANON_KEY/SERVICE_ROLE_KEY env. Run: set -a; eval "$(npx supabase status -o env)"; set +a; npm run test:int',
    );
  }

  admin = createClient(SUPABASE_URL, SERVICE_ROLE_KEY, {
    auth: { persistSession: false, autoRefreshToken: false },
  });
  const created = await admin.auth.admin.createUser({
    email: testEmail,
    password: testPassword,
    email_confirm: true,
  });
  if (created.error) throw created.error;
  userId = created.data.user!.id;

  const anon = createClient(SUPABASE_URL, ANON_KEY, {
    auth: { persistSession: false, autoRefreshToken: false },
  });
  const signedIn = await anon.auth.signInWithPassword({ email: testEmail, password: testPassword });
  if (signedIn.error) throw signedIn.error;
  accessToken = signedIn.data.session!.access_token;

  const { buildApp } = await import('../../src/app.js');
  app = await buildApp();
  await app.ready();
});

afterAll(async () => {
  await app?.close();
  if (admin && userId) {
    await admin.auth.admin.deleteUser(userId);
  }
});

function authHeaders() {
  return { authorization: `Bearer ${accessToken}` };
}

async function createGardenSpace() {
  const res = await app.inject({
    method: 'POST',
    url: '/garden-spaces',
    headers: authHeaders(),
    payload: { name: 'West Balcony', kind: 'balcony' },
  });
  expect(res.statusCode).toBe(201);
  return res.json().id as string;
}

async function createContainer() {
  const res = await app.inject({
    method: 'POST',
    url: '/containers',
    headers: authHeaders(),
    payload: { name: 'Blue barrel', volumeLiters: 19, material: 'plastic', drainage: 'good' },
  });
  expect(res.statusCode).toBe(201);
  return res.json().id as string;
}

describe('Slice 1 API — add plant', () => {
  it('#15 happy path: POST /plants creates a plant + one initial water task', async () => {
    const gardenSpaceId = await createGardenSpace();
    const containerId = await createContainer();

    const res = await app.inject({
      method: 'POST',
      url: '/plants',
      headers: authHeaders(),
      payload: {
        profileId: 'solanum-lycopersicum',
        containerId,
        gardenSpaceId,
        growthStage: 'vegetative',
        lastWateredAt: '2026-05-26T07:00:00.000Z',
        nickname: 'Pasi',
      },
    });
    expect(res.statusCode).toBe(201);
    const body = res.json();
    const task = body.task;
    expect(task.kind).toBe('water');
    expect(task.engineVersion).toBe('0.1.0');
    expect(typeof task.inputsHash).toBe('string');
    expect(task.inputsHash.length).toBeGreaterThanOrEqual(8);
    expect(task.priority).toBe('normal');
    expect(typeof task.dueAt).toBe('string');
    expect(task.sourceInputs.wateringBaselineAt).toBe('2026-05-26T07:00:00.000Z');

    // Follow-up GET returns exactly one water task for the plant.
    const tasksRes = await app.inject({
      method: 'GET',
      url: `/plants/${body.plant.id}/tasks`,
      headers: authHeaders(),
    });
    expect(tasksRes.statusCode).toBe(200);
    const tasks = tasksRes.json();
    expect(Array.isArray(tasks)).toBe(true);
    expect(tasks).toHaveLength(1);
    expect(tasks[0].kind).toBe('water');
  });

  it('#16 missing containerId → 400', async () => {
    const gardenSpaceId = await createGardenSpace();
    const res = await app.inject({
      method: 'POST',
      url: '/plants',
      headers: authHeaders(),
      payload: { profileId: 'solanum-lycopersicum', gardenSpaceId, growthStage: 'vegetative' },
    });
    expect(res.statusCode).toBe(400);
  });

  it('#17 missing gardenSpaceId → 400', async () => {
    const containerId = await createContainer();
    const res = await app.inject({
      method: 'POST',
      url: '/plants',
      headers: authHeaders(),
      payload: { profileId: 'solanum-lycopersicum', containerId, growthStage: 'vegetative' },
    });
    expect(res.statusCode).toBe(400);
  });

  it('#18 unknown profileId → 400', async () => {
    const gardenSpaceId = await createGardenSpace();
    const containerId = await createContainer();
    const res = await app.inject({
      method: 'POST',
      url: '/plants',
      headers: authHeaders(),
      payload: {
        profileId: 'does-not-exist',
        containerId,
        gardenSpaceId,
        growthStage: 'vegetative',
      },
    });
    expect(res.statusCode).toBe(400);
  });

  it('rejects an unauthenticated POST /plants with 401', async () => {
    const res = await app.inject({
      method: 'POST',
      url: '/plants',
      payload: { profileId: 'solanum-lycopersicum', growthStage: 'vegetative' },
    });
    expect(res.statusCode).toBe(401);
  });
});
