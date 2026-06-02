// Slice 2 — GET /plants/:id/advisories integration tests (maps the @slice-2 BDD
// scenarios in features/container-health.feature). Advisories are computed on read;
// no CareTask is created. Every returned advisory must validate against advisory.schema.json.
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
let validateAdvisory: ValidateFunction;
const users: Array<{ id: string; token: string }> = [];

async function provisionUser(label: string): Promise<{ id: string; token: string }> {
  const email = `slice2-${label}-${Date.now()}-${Math.floor(performance.now())}@example.test`;
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
  validateAdvisory = compileSchema('advisory');
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
function assertAllValid(advisories: Array<Record<string, unknown>>) {
  for (const a of advisories) {
    const ok = validateAdvisory(a);
    expect(validateAdvisory.errors ?? []).toEqual([]);
    expect(ok).toBe(true);
  }
}

describe('Slice 2 API — GET /plants/:id/advisories', () => {
  it('container-size: passion fruit in a 19L container surfaces a high container-size advisory citing 95 and 190', async () => {
    const a = users[0]!;
    const space = await createSpace(a.token);
    const container = await createContainer(a.token, 19);
    const plantId = await addPlant(a.token, 'passiflora-edulis', container, space);

    const res = await getAdvisories(a.token, plantId);
    expect(res.statusCode).toBe(200);
    const advisories = res.json() as Array<Record<string, string>>;
    assertAllValid(advisories);
    const cs = advisories.find((x) => x.kind === 'container-size');
    expect(cs).toBeTruthy();
    expect(cs!.severity).toBe('high');
    expect(cs!.message).toContain('95');
    expect(cs!.message).toContain('190');
  });

  it('support: passion fruit (requiresSupport) without supportRecorded surfaces a support advisory', async () => {
    const a = users[0]!;
    const space = await createSpace(a.token);
    const container = await createContainer(a.token, 200); // large enough → no container-size advisory
    const plantId = await addPlant(a.token, 'passiflora-edulis', container, space);

    const res = await getAdvisories(a.token, plantId);
    expect(res.statusCode).toBe(200);
    const advisories = res.json() as Array<Record<string, string>>;
    assertAllValid(advisories);
    expect(advisories.some((x) => x.kind === 'support')).toBe(true);
  });

  it('pollination: a single tomatillo surfaces a pollination advisory; adding a second clears it', async () => {
    const a = users[0]!;
    const space = await createSpace(a.token);
    const c1 = await createContainer(a.token, 19);
    const firstId = await addPlant(a.token, 'physalis-philadelphica', c1, space);

    const before = await getAdvisories(a.token, firstId);
    expect(before.statusCode).toBe(200);
    const beforeAdv = before.json() as Array<Record<string, string>>;
    assertAllValid(beforeAdv);
    expect(beforeAdv.some((x) => x.kind === 'pollination')).toBe(true);

    // add a second tomatillo for the same user
    const c2 = await createContainer(a.token, 19);
    await addPlant(a.token, 'physalis-philadelphica', c2, space);

    const after = await getAdvisories(a.token, firstId);
    expect(after.statusCode).toBe(200);
    const afterAdv = after.json() as Array<Record<string, string>>;
    assertAllValid(afterAdv);
    expect(afterAdv.some((x) => x.kind === 'pollination')).toBe(false);
  });

  it('RLS: user B cannot read user A\'s plant advisories → 404', async () => {
    const a = users[0]!;
    const b = users[1]!;
    const space = await createSpace(a.token);
    const container = await createContainer(a.token, 19);
    const plantId = await addPlant(a.token, 'passiflora-edulis', container, space);

    const res = await getAdvisories(b.token, plantId);
    expect(res.statusCode).toBe(404);
  });
});
