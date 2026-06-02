// Care-engine red-first tests (per docs/slice-01-implementation-plan.md tests #7–#14,
// formula D-10 in docs/slice-01-decision-log.md).
//
// RED-FIRST: computeInitialWaterTask is intentionally NOT implemented yet
// (backend/care-engine/index.ts is still `export {};`). The module is loaded with a
// DYNAMIC import in beforeAll on purpose: a static `import { computeInitialWaterTask }`
// of a missing named export throws at module-link time and aborts the whole suite
// before any test runs. Loading the namespace and reading the (absent) export yields
// `undefined`, so each test fails individually with
// "computeInitialWaterTask is not a function" — the precise red we want. Do NOT
// convert this to a static named import, and do not implement the engine here (that
// is a separate later commit). This same file passes unchanged once the export exists.
import { describe, it, expect, beforeAll } from 'vitest';

// Local shape we assert against. The real CareTask type lands with the engine; this
// keeps the red-first test type-clean without importing from an unimplemented module.
interface WaterTaskLike {
  id: string;
  plantInstanceId: string;
  kind: string;
  dueAt: string;
  priority: string;
  rationale: string;
  engineVersion: string;
  inputsHash: string;
  status: string;
  sourceInputs: Record<string, unknown>;
}

// Resolves to `undefined` while the engine is a placeholder, so each test below
// throws "computeInitialWaterTask is not a function" — not a suite-load error.
let computeInitialWaterTask: (input: unknown) => WaterTaskLike;

beforeAll(async () => {
  const mod = await import('../../care-engine/index.js');
  computeInitialWaterTask = (mod as unknown as {
    computeInitialWaterTask: (input: unknown) => WaterTaskLike;
  }).computeInitialWaterTask;
});

const DAY_MS = 86_400_000;

const tomatoProfile = {
  id: 'solanum-lycopersicum',
  version: 1,
  commonNames: ['Tomato'],
  wateringProfile: { baseIntervalDays: 2 },
  containerProfile: { recommendedMinLiters: 19 },
};
const passionProfile = {
  id: 'passiflora-edulis',
  version: 1,
  commonNames: ['Passion fruit'],
  wateringProfile: { baseIntervalDays: 3 },
  containerProfile: { recommendedMinLiters: 95 },
};

const container19 = { id: '00000000-0000-4000-8000-000000000002', volumeLiters: 19 };   // tomato ratio 1.0 → factor 1.0
const containerBig = { id: '00000000-0000-4000-8000-000000000002', volumeLiters: 100 };  // tomato ratio 5.26 → clamp 1.5
const gardenSpace = { id: '00000000-0000-4000-8000-000000000003' };

const basePlant = {
  id: '00000000-0000-4000-8000-000000000001',
  profileId: 'solanum-lycopersicum',
  containerId: container19.id,
  gardenSpaceId: gardenSpace.id,
  createdAt: '2026-05-26T07:00:00.000Z',
};

const baseline = '2026-05-26T07:00:00.000Z';

function tomatoInput(overrides = {}) {
  return {
    id: 'task-1',
    clockUtc: baseline,
    plant: { ...basePlant, lastWateredAt: baseline },
    profile: tomatoProfile,
    container: container19,
    gardenSpace,
    ...overrides,
  };
}

describe('computeInitialWaterTask — Slice 1 (red-first)', () => {
  it('#7 returns one water CareTask', () => {
    const task = computeInitialWaterTask(tomatoInput());
    expect(task.kind).toBe('water');
    expect(task.id).toBe('task-1');
    expect(task.plantInstanceId).toBe(basePlant.id);
    expect(task.status).toBe('pending');
  });

  it('#8 carries engineVersion, inputsHash, sourceInputs, rationale, dueAt, priority', () => {
    const task = computeInitialWaterTask(tomatoInput());
    expect(task.engineVersion).toBe('0.1.0');
    expect(task.priority).toBe('normal');
    expect(typeof task.inputsHash).toBe('string');
    expect(task.inputsHash.length).toBeGreaterThanOrEqual(8);
    expect(task.rationale).toContain('Tomato');
    expect(task.rationale).toContain(baseline);
    expect(task.sourceInputs).toMatchObject({
      plantInstanceId: basePlant.id,
      profileId: 'solanum-lycopersicum',
      profileVersion: 1,
      containerId: container19.id,
      gardenSpaceId: gardenSpace.id,
      clockUtc: baseline,
      wateringBaselineAt: baseline,
      weatherWindowRef: null,
      feedbackWindowRef: null,
    });
    expect(typeof task.dueAt).toBe('string');
  });

  it('#9 is deterministic: equal inputs → byte-equal output and identical inputsHash', () => {
    const a = computeInitialWaterTask(tomatoInput());
    const b = computeInitialWaterTask(JSON.parse(JSON.stringify(tomatoInput())));
    expect(b).toEqual(a);
    expect(JSON.stringify(b)).toBe(JSON.stringify(a));
    expect(b.inputsHash).toBe(a.inputsHash);
  });

  it('#10 clamps container factor to [0.5, 1.5]', () => {
    // below 0.5: passion fruit (recMin 95) in a 19 L container → 0.2 → clamp 0.5
    const low = computeInitialWaterTask({
      id: 't', clockUtc: baseline,
      plant: { ...basePlant, profileId: passionProfile.id, lastWateredAt: baseline },
      profile: passionProfile, container: container19, gardenSpace,
    });
    expect(new Date(low.dueAt).getTime() - new Date(baseline).getTime()).toBe(3 * 0.5 * DAY_MS);

    // above 1.5: tomato (recMin 19) in a 100 L container → 5.26 → clamp 1.5
    const high = computeInitialWaterTask(tomatoInput({ container: containerBig }));
    expect(new Date(high.dueAt).getTime() - new Date(baseline).getTime()).toBe(2 * 1.5 * DAY_MS);
  });

  it('#11 later clockUtc (same baseline) → different inputsHash but SAME dueAt', () => {
    const early = computeInitialWaterTask(tomatoInput({ clockUtc: '2026-05-26T07:00:00.000Z' }));
    const later = computeInitialWaterTask(tomatoInput({ clockUtc: '2026-05-27T09:30:00.000Z' }));
    expect(later.dueAt).toBe(early.dueAt);                 // anchored on wateringBaselineAt, not the clock
    expect(later.inputsHash).not.toBe(early.inputsHash);   // clockUtc is part of sourceInputs
  });

  it('#12 baseline supplied: wateringBaselineAt === lastWateredAt; dueAt = lastWateredAt + interval×factor', () => {
    const lw = '2026-05-20T12:00:00.000Z';
    const task = computeInitialWaterTask(tomatoInput({ plant: { ...basePlant, lastWateredAt: lw } }));
    expect(task.sourceInputs.wateringBaselineAt).toBe(lw);
    expect(new Date(task.dueAt).getTime() - new Date(lw).getTime()).toBe(2 * 1.0 * DAY_MS);
  });

  it('#13 baseline fallback: no lastWateredAt → wateringBaselineAt === createdAt', () => {
    const plantNoLW = { ...basePlant }; // createdAt = baseline; no lastWateredAt
    const task = computeInitialWaterTask({
      id: 't', clockUtc: '2026-05-30T00:00:00.000Z',
      plant: plantNoLW, profile: tomatoProfile, container: container19, gardenSpace,
    });
    expect(task.sourceInputs.wateringBaselineAt).toBe(plantNoLW.createdAt);
    expect(new Date(task.dueAt).getTime() - new Date(plantNoLW.createdAt).getTime()).toBe(2 * 1.0 * DAY_MS);
  });

  it('#14 different lastWateredAt → different inputsHash AND different dueAt', () => {
    const t1 = computeInitialWaterTask(tomatoInput({ plant: { ...basePlant, lastWateredAt: '2026-05-20T12:00:00.000Z' } }));
    const t2 = computeInitialWaterTask(tomatoInput({ plant: { ...basePlant, lastWateredAt: '2026-05-22T12:00:00.000Z' } }));
    expect(t2.inputsHash).not.toBe(t1.inputsHash);
    expect(t2.dueAt).not.toBe(t1.dueAt);
  });
});
