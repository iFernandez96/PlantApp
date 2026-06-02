// 3d-api — POST /plants/:id/advisories/accept integration tests. On explicit acceptance the
// endpoint creates ONE persisted CareTask via the deterministic computeTaskFromAdvisory; the
// GET advisories handler must still create nothing. Created task validates against
// care-task.schema.json. RLS-scoped (404 on another user's plant); unsupported/absent kind → 400.
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
let validateCareTask: ValidateFunction;
const users: Array<{ id: string; token: string }> = [];

async function provisionUser(label: string): Promise<{ id: string; token: string }> {
  const email = `slice3d-${label}-${Date.now()}-${Math.floor(performance.now())}@example.test`;
  const password = 'Test-Passw0rd!';
  const created = await admin.auth.admin.createUser({ email, password, email_confirm: true });
  if (created.error) throw created.error;
  const anon = createClient(SUPABASE_URL, ANON_KEY, { auth: { persistSession: false, autoRefreshToken: false } });
  const signedIn = await anon.auth.signInWithPassword({ email, password });
  if (signedIn.error) throw signedIn.error;
  return { id: created.data.user!.id, token: signedIn.data.session!.access_token };
}

beforeAll(async () => {
  if (!ANON_KEY || !SERVICE_ROLE_KEY) {
    throw new Error('Missing ANON_KEY/SERVICE_ROLE_KEY env. Run via: set -a; eval "$(npx supabase status -o env)"; set +a; npm run test:int');
  }
  admin = createClient(SUPABASE_URL, SERVICE_ROLE_KEY, { auth: { persistSession: false, autoRefreshToken: false } });
  validateCareTask = compileSchema('care-task');
  users.push(await provisionUser('a'));
  users.push(await provisionUser('b'));
  const { buildApp } = await import('../../src/app.js');
  app = await buildApp();
  await app.ready();
});

afterAll(async () => {
  await app?.close();
  for (const u of users) await admin.auth.admin.deleteUser(u.id);
});

function auth(token: string) {
  return { authorization: `Bearer ${token}` };
}
async function createSpace(token: string): Promise<string> {
  const r = await app.inject({ method: 'POST', url: '/garden-spaces', headers: auth(token), payload: { name: 'West Balcony', kind: 'balcony' } });
  expect(r.statusCode).toBe(201);
  return r.json().id as string;
}
async function createContainer(token: string, volumeLiters: number): Promise<string> {
  const r = await app.inject({ method: 'POST', url: '/containers', headers: auth(token), payload: { name: 'C', volumeLiters, material: 'plastic', drainage: 'good' } });
  expect(r.statusCode).toBe(201);
  return r.json().id as string;
}
async function addPlant(token: string, profileId: string, containerId: string, gardenSpaceId: string): Promise<string> {
  const r = await app.inject({
    method: 'POST', url: '/plants', headers: auth(token),
    payload: { profileId, containerId, gardenSpaceId, growthStage: 'vegetative', lastWateredAt: '2026-05-26T07:00:00.000Z' },
  });
  expect(r.statusCode).toBe(201);
  return r.json().plant.id as string;
}
async function getAdvisories(token: string, plantId: string) {
  return app.inject({ method: 'GET', url: `/plants/${plantId}/advisories`, headers: auth(token) });
}
async function getTasks(token: string, plantId: string) {
  return app.inject({ method: 'GET', url: `/plants/${plantId}/tasks`, headers: auth(token) });
}
async function accept(token: string, plantId: string, kind: string) {
  return app.inject({ method: 'POST', url: `/plants/${plantId}/advisories/accept`, headers: auth(token), payload: { kind } });
}

describe('3d API — POST /plants/:id/advisories/accept', () => {
  it('accepting a container-size advisory creates one repot CareTask; GET advisories creates nothing', async () => {
    const a = users[0]!;
    const space = await createSpace(a.token);
    const container = await createContainer(a.token, 19); // passion fruit in 19L → container-size advisory
    const plantId = await addPlant(a.token, 'passiflora-edulis', container, space);

    // The advisory is present; GET it twice — it must create nothing (task count stays the add-plant baseline).
    const adv1 = await getAdvisories(a.token, plantId);
    expect(adv1.statusCode).toBe(200);
    expect((adv1.json() as Array<{ kind: string }>).some((x) => x.kind === 'container-size')).toBe(true);

    const baseline = (await getTasks(a.token, plantId)).json() as unknown[];
    const adv2 = await getAdvisories(a.token, plantId); // a second read…
    expect(adv2.statusCode).toBe(200);
    const afterGet = (await getTasks(a.token, plantId)).json() as unknown[];
    expect(afterGet.length).toBe(baseline.length); // …still created nothing

    // Explicit acceptance creates exactly one new task.
    const res = await accept(a.token, plantId, 'container-size');
    expect(res.statusCode).toBe(201);
    const task = res.json() as Record<string, unknown>;
    const ok = validateCareTask(task);
    expect(validateCareTask.errors ?? []).toEqual([]);
    expect(ok).toBe(true);
    expect(task.kind).toBe('repot');
    expect(task.priority).toBe('high');

    const afterAccept = (await getTasks(a.token, plantId)).json() as unknown[];
    expect(afterAccept.length).toBe(baseline.length + 1);
  });

  it('accepting an unsupported kind (pollination) → 400', async () => {
    const a = users[0]!;
    const space = await createSpace(a.token);
    const c1 = await createContainer(a.token, 19);
    const plantId = await addPlant(a.token, 'physalis-philadelphica', c1, space); // single tomatillo → pollination advisory

    const res = await accept(a.token, plantId, 'pollination');
    expect(res.statusCode).toBe(400);
  });

  it('accepting a kind that is not currently applicable → 400', async () => {
    const a = users[0]!;
    const space = await createSpace(a.token);
    // Strawberry: no support requirement, self-fruitful; an in-ideal-range (4–10L) container →
    // no advisories at all, so 'support' is not applicable.
    const container = await createContainer(a.token, 6);
    const plantId = await addPlant(a.token, 'fragaria-x-ananassa', container, space);

    const res = await accept(a.token, plantId, 'support');
    expect(res.statusCode).toBe(400);
  });

  it('RLS: user B cannot accept on user A\'s plant → 404', async () => {
    const a = users[0]!;
    const b = users[1]!;
    const space = await createSpace(a.token);
    const container = await createContainer(a.token, 19);
    const plantId = await addPlant(a.token, 'passiflora-edulis', container, space);

    const res = await accept(b.token, plantId, 'container-size');
    expect(res.statusCode).toBe(404);
  });
});
