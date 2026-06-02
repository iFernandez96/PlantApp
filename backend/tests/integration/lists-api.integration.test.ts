// Backlog 3a — read-only list endpoints feeding the add-plant selectors:
//   GET /plant-profiles (global catalog), GET /garden-spaces, GET /containers (RLS-scoped).
// Responses validate against the shared schemas; RLS isolation asserted.
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
let validateProfile: ValidateFunction;
let validateGardenSpace: ValidateFunction;
let validateContainer: ValidateFunction;
const users: Array<{ id: string; token: string }> = [];

async function provisionUser(label: string): Promise<{ id: string; token: string }> {
  const email = `lists-${label}-${Date.now()}-${Math.floor(performance.now())}@example.test`;
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
    throw new Error('Missing ANON_KEY/SERVICE_ROLE_KEY env. set -a; eval "$(npx supabase status -o env)"; set +a; npm run test:int');
  }
  admin = createClient(SUPABASE_URL, SERVICE_ROLE_KEY, { auth: { persistSession: false, autoRefreshToken: false } });
  validateProfile = compileSchema('plant-profile');
  validateGardenSpace = compileSchema('garden-space');
  validateContainer = compileSchema('container');
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
function assertAllValid(validate: ValidateFunction, items: unknown[], label: string) {
  for (const item of items) {
    const ok = validate(item);
    if (!ok) throw new Error(`${label} failed schema: ${JSON.stringify(validate.errors)}`);
    expect(ok).toBe(true);
  }
}

describe('Backlog 3a — list endpoints', () => {
  it('GET /plant-profiles returns the seeded catalog, schema-valid', async () => {
    const res = await app.inject({ method: 'GET', url: '/plant-profiles', headers: auth(users[0]!.token) });
    expect(res.statusCode).toBe(200);
    const profiles = res.json() as Array<{ id: string }>;
    expect(profiles.length).toBeGreaterThanOrEqual(5);
    assertAllValid(validateProfile, profiles, 'plant-profile');
    expect(profiles.map((p) => p.id)).toContain('solanum-lycopersicum');
  });

  it('GET /garden-spaces returns the caller\'s spaces (schema-valid) and isolates by RLS', async () => {
    const a = users[0]!;
    const b = users[1]!;
    const created = await app.inject({ method: 'POST', url: '/garden-spaces', headers: auth(a.token), payload: { name: 'A Balcony', kind: 'balcony' } });
    expect(created.statusCode).toBe(201);
    const spaceId = created.json().id as string;

    const listA = await app.inject({ method: 'GET', url: '/garden-spaces', headers: auth(a.token) });
    expect(listA.statusCode).toBe(200);
    const aSpaces = listA.json() as Array<{ id: string }>;
    assertAllValid(validateGardenSpace, aSpaces, 'garden-space');
    expect(aSpaces.map((s) => s.id)).toContain(spaceId);

    const listB = await app.inject({ method: 'GET', url: '/garden-spaces', headers: auth(b.token) });
    expect(listB.statusCode).toBe(200);
    expect((listB.json() as Array<{ id: string }>).map((s) => s.id)).not.toContain(spaceId);
  });

  it('GET /containers returns the caller\'s containers (schema-valid) and isolates by RLS', async () => {
    const a = users[0]!;
    const b = users[1]!;
    const created = await app.inject({ method: 'POST', url: '/containers', headers: auth(a.token), payload: { name: 'A Pot', volumeLiters: 19, material: 'plastic', drainage: 'good' } });
    expect(created.statusCode).toBe(201);
    const containerId = created.json().id as string;

    const listA = await app.inject({ method: 'GET', url: '/containers', headers: auth(a.token) });
    expect(listA.statusCode).toBe(200);
    const aContainers = listA.json() as Array<{ id: string }>;
    assertAllValid(validateContainer, aContainers, 'container');
    expect(aContainers.map((c) => c.id)).toContain(containerId);

    const listB = await app.inject({ method: 'GET', url: '/containers', headers: auth(b.token) });
    expect(listB.statusCode).toBe(200);
    expect((listB.json() as Array<{ id: string }>).map((c) => c.id)).not.toContain(containerId);
  });

  it.each(['/plant-profiles', '/garden-spaces', '/containers'])('%s without a bearer token → 401', async (url) => {
    const res = await app.inject({ method: 'GET', url });
    expect(res.statusCode).toBe(401);
  });
});
