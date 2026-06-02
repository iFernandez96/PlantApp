// Slice 1 seed-catalog integration test. RED-FIRST: seed-profiles.ts starts empty,
// so "contains the 5 seed profiles" fails until the catalog is filled (green commit).
// Loads modules via dynamic import so a missing export can't abort suite collection.
import { describe, it, expect, beforeAll } from 'vitest';
import type { ValidateFunction } from 'ajv';
import { compileSchema } from '../schema/_helpers.js';

interface SeedProfile {
  id: string;
  version: number;
  commonNames: string[];
  wateringProfile: { baseIntervalDays: number };
  containerProfile: { recommendedMinLiters: number };
  [k: string]: unknown;
}
type WaterEngine = (input: unknown) => Record<string, unknown>;

let seedProfiles: SeedProfile[];
let computeInitialWaterTask: WaterEngine;
let validateProfile: ValidateFunction;
let validateCareTask: ValidateFunction;

beforeAll(async () => {
  seedProfiles = ((await import('../../care-engine/seed-profiles.js')) as { seedProfiles: SeedProfile[] }).seedProfiles;
  computeInitialWaterTask = ((await import('../../care-engine/index.js')) as { computeInitialWaterTask: WaterEngine }).computeInitialWaterTask;
  validateProfile = compileSchema('plant-profile');
  validateCareTask = compileSchema('care-task');
});

const PLANT_ID = '00000000-0000-4000-8000-000000000001';
const CONTAINER_ID = '00000000-0000-4000-8000-000000000002';
const SPACE_ID = '00000000-0000-4000-8000-000000000003';
const TASK_ID = '00000000-0000-4000-8000-0000000000aa';
const CLOCK = '2026-05-26T07:00:00.000Z';

const EXPECTED_IDS = [
  'fragaria-x-ananassa',
  'ocimum-basilicum',
  'passiflora-edulis',
  'physalis-philadelphica',
  'solanum-lycopersicum',
];

describe('Slice 1 seed catalog', () => {
  it('contains the 5 Slice 1 seed profiles', () => {
    expect(seedProfiles).toHaveLength(5);
    expect(seedProfiles.map((p) => p.id).sort()).toEqual([...EXPECTED_IDS].sort());
  });

  it('each seed profile validates against plant-profile.schema.json', () => {
    for (const p of seedProfiles) {
      const ok = validateProfile(p);
      expect(validateProfile.errors ?? []).toEqual([]);
      expect(ok).toBe(true);
    }
  });

  it('computeInitialWaterTask emits a schema-valid CareTask for each seed profile', () => {
    for (const p of seedProfiles) {
      const task = computeInitialWaterTask({
        id: TASK_ID,
        clockUtc: CLOCK,
        plant: {
          id: PLANT_ID,
          profileId: p.id,
          containerId: CONTAINER_ID,
          gardenSpaceId: SPACE_ID,
          createdAt: CLOCK,
          lastWateredAt: CLOCK,
        },
        profile: p,
        container: { id: CONTAINER_ID, volumeLiters: 19 },
        gardenSpace: { id: SPACE_ID },
      });
      const ok = validateCareTask(task);
      expect(validateCareTask.errors ?? []).toEqual([]);
      expect(ok).toBe(true);
    }
  });
});
