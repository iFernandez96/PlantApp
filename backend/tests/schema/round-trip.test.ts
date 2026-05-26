// Test #6 (backend half, per docs/slice-01-implementation-plan.md):
// Round-trip encode/decode equality for each schema's DTO. The Android half of
// this test lives under the future Android module's test source set and is
// not part of this commit.
//
// "Round-trip" here means: take a valid fixture, JSON.stringify -> JSON.parse,
// assert deep equality, and assert the parsed value still validates against
// the same schema.
import { describe, it, expect, beforeAll } from 'vitest';
import type { ValidateFunction } from 'ajv';
import { compileSchema } from './_helpers.js';

const fixtures = [
  {
    name: 'plant-profile',
    value: {
      id: 'solanum-lycopersicum',
      scientificName: 'Solanum lycopersicum',
      commonNames: ['Tomato'],
      category: 'fruit',
      growthHabit: 'vine',
      requiresSupport: true,
      selfFruitful: true,
      wateringProfile: { baseIntervalDays: 2, dryingTolerance: 'low' },
      feedingProfile: { baseIntervalDays: 7, fruitingIntervalDays: 5 },
      containerProfile: { recommendedMinLiters: 19 },
      lightProfile: { targetSunHours: 8 },
      temperatureProfile: { frostSensitive: true },
      version: 1,
    },
  },
  {
    name: 'plant-instance',
    value: {
      id: '00000000-0000-4000-8000-000000000001',
      userId: 'user-owner',
      profileId: 'solanum-lycopersicum',
      containerId: '00000000-0000-4000-8000-000000000002',
      gardenSpaceId: '00000000-0000-4000-8000-000000000003',
      growthStage: 'vegetative',
      createdAt: '2026-05-26T07:00:00Z',
      lastWateredAt: '2026-05-26T07:00:00Z',
    },
  },
  {
    name: 'container',
    value: {
      id: '00000000-0000-4000-8000-000000000010',
      userId: 'user-owner',
      name: 'Blue barrel',
      volumeLiters: 19,
      material: 'plastic',
      drainage: 'good',
      createdAt: '2026-05-26T07:00:00Z',
    },
  },
  {
    name: 'garden-space',
    value: {
      id: '00000000-0000-4000-8000-000000000020',
      userId: 'user-owner',
      name: 'West Balcony',
      kind: 'balcony',
      createdAt: '2026-05-26T07:00:00Z',
    },
  },
  {
    name: 'care-task',
    value: {
      id: '00000000-0000-4000-8000-000000000100',
      plantInstanceId: '00000000-0000-4000-8000-000000000001',
      kind: 'water',
      dueAt: '2026-05-28T07:00:00Z',
      priority: 'normal',
      rationale:
        'Tomato: base interval 2d adjusted by container factor 1.0; baseline 2026-05-26T07:00:00Z',
      engineVersion: '0.1.0',
      inputsHash: 'a'.repeat(64),
      sourceInputs: {
        plantInstanceId: '00000000-0000-4000-8000-000000000001',
        profileId: 'solanum-lycopersicum',
        profileVersion: 1,
        containerId: '00000000-0000-4000-8000-000000000002',
        gardenSpaceId: '00000000-0000-4000-8000-000000000003',
        clockUtc: '2026-05-26T09:00:00Z',
        wateringBaselineAt: '2026-05-26T07:00:00Z',
        weatherWindowRef: null,
        feedbackWindowRef: null,
      },
      status: 'pending',
    },
  },
];

describe('round-trip encode/decode equality (backend) — test #6', () => {
  const validators = new Map<string, ValidateFunction>();

  beforeAll(() => {
    for (const { name } of fixtures) {
      validators.set(name, compileSchema(name));
    }
  });

  it.each(fixtures)('round-trips a $name fixture without loss', ({ name, value }) => {
    const encoded = JSON.stringify(value);
    const decoded = JSON.parse(encoded) as unknown;

    expect(decoded).toEqual(value);

    const validate = validators.get(name)!;
    const ok = validate(decoded);
    expect(validate.errors ?? []).toEqual([]);
    expect(ok).toBe(true);
  });
});
