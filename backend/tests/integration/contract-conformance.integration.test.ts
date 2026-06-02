// Contract conformance: every API response must validate against the camelCase shared
// JSON Schema (shared-schemas/*.schema.json) — the cross-boundary contract (D-06).
// RED-FIRST: the API currently returns snake_case DB rows, which fail these schemas.
import { describe, it, expect, beforeAll, afterAll } from 'vitest';
import { createClient, type SupabaseClient } from '@supabase/supabase-js';
import type { FastifyInstance } from 'fastify';
import type { ValidateFunction } from 'ajv';
import { compileSchema } from '../schema/_helpers.js';

const SUPABASE_URL = process.env.SUPABASE_URL ?? process.env.API_URL ?? 'http://127.0.0.1:54321';
const ANON_KEY = process.env.SUPABASE_ANON_KEY ?? process.env.ANON_KEY ?? '';
const SERVICE_ROLE_KEY = process.env.SUPABASE_SERVICE_ROLE_KEY ?? process.env.SERVICE_ROLE_KEY ?? '';

let app: FastifyInstance;
let admin: SupabaseClient;
let token: string;
let userId: string;
const email = `slice1-contract-${Date.now()}@example.test`;
const password = 'Test-Passw0rd!';

let validateGardenSpace: ValidateFunction;
let validateContainer: ValidateFunction;
let validatePlantInstance: ValidateFunction;
let validateCareTask: ValidateFunction;

function assertValid(validate: ValidateFunction, value: unknown, label: string) {
  const ok = validate(value);
  if (!ok) {
    throw new Error(`${label} failed schema validation: ${JSON.stringify(validate.errors, null, 2)}`);
  }
  expect(ok).toBe(true);
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
  const created = await admin.auth.admin.createUser({ email, password, email_confirm: true });
  if (created.error) throw created.error;
  userId = created.data.user!.id;
  const anon = createClient(SUPABASE_URL, ANON_KEY, {
    auth: { persistSession: false, autoRefreshToken: false },
  });
  const signedIn = await anon.auth.signInWithPassword({ email, password });
  if (signedIn.error) throw signedIn.error;
  token = signedIn.data.session!.access_token;

  validateGardenSpace = compileSchema('garden-space');
  validateContainer = compileSchema('container');
  validatePlantInstance = compileSchema('plant-instance');
  validateCareTask = compileSchema('care-task');

  const { buildApp } = await import('../../src/app.js');
  app = await buildApp();
  await app.ready();
});

afterAll(async () => {
  await app?.close();
  if (admin && userId) await admin.auth.admin.deleteUser(userId);
});

function auth() {
  return { authorization: `Bearer ${token}` };
}

describe('API contract conformance against shared-schemas (camelCase)', () => {
  it('all endpoints return schema-conformant bodies', async () => {
    // POST /garden-spaces → GardenSpace
    const gsRes = await app.inject({
      method: 'POST',
      url: '/garden-spaces',
      headers: auth(),
      payload: { name: 'West Balcony', kind: 'balcony' },
    });
    expect(gsRes.statusCode).toBe(201);
    assertValid(validateGardenSpace, gsRes.json(), 'POST /garden-spaces');
    const gardenSpaceId = gsRes.json().id as string;

    // POST /containers → Container
    const cRes = await app.inject({
      method: 'POST',
      url: '/containers',
      headers: auth(),
      payload: { name: 'Blue barrel', volumeLiters: 19, material: 'plastic', drainage: 'good' },
    });
    expect(cRes.statusCode).toBe(201);
    assertValid(validateContainer, cRes.json(), 'POST /containers');
    const containerId = cRes.json().id as string;

    // POST /plants → { plant: PlantInstance, task: CareTask }
    const pRes = await app.inject({
      method: 'POST',
      url: '/plants',
      headers: auth(),
      payload: {
        profileId: 'solanum-lycopersicum',
        containerId,
        gardenSpaceId,
        growthStage: 'vegetative',
        lastWateredAt: '2026-05-26T07:00:00.000Z',
      },
    });
    expect(pRes.statusCode).toBe(201);
    assertValid(validatePlantInstance, pRes.json().plant, 'POST /plants → plant');
    assertValid(validateCareTask, pRes.json().task, 'POST /plants → task');
    const plantId = pRes.json().plant.id as string;

    // GET /plants → PlantInstance[]
    const listRes = await app.inject({ method: 'GET', url: '/plants', headers: auth() });
    expect(listRes.statusCode).toBe(200);
    const list = listRes.json() as unknown[];
    expect(list.length).toBeGreaterThanOrEqual(1);
    for (const item of list) assertValid(validatePlantInstance, item, 'GET /plants item');

    // GET /plants/:id → PlantInstance
    const getRes = await app.inject({ method: 'GET', url: `/plants/${plantId}`, headers: auth() });
    expect(getRes.statusCode).toBe(200);
    assertValid(validatePlantInstance, getRes.json(), 'GET /plants/:id');

    // GET /plants/:id/tasks → CareTask[]
    const tasksRes = await app.inject({ method: 'GET', url: `/plants/${plantId}/tasks`, headers: auth() });
    expect(tasksRes.statusCode).toBe(200);
    const tasks = tasksRes.json() as unknown[];
    expect(tasks).toHaveLength(1);
    for (const t of tasks) assertValid(validateCareTask, t, 'GET /plants/:id/tasks item');
  });
});
