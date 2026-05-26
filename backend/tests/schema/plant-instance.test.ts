// Test #2 (per docs/slice-01-implementation-plan.md):
// plant-instance.schema.json accepts a valid instance and rejects missing
// containerId / gardenSpaceId.
import { describe, it, expect, beforeAll } from 'vitest';
import type { ValidateFunction } from 'ajv';
import { compileSchema, clone } from './_helpers.js';

const validInstance = {
  id: '00000000-0000-4000-8000-000000000001',
  userId: 'user-owner',
  profileId: 'solanum-lycopersicum',
  containerId: '00000000-0000-4000-8000-000000000002',
  gardenSpaceId: '00000000-0000-4000-8000-000000000003',
  growthStage: 'vegetative',
  createdAt: '2026-05-26T07:00:00Z',
};

describe('plant-instance.schema.json — test #2', () => {
  let validate: ValidateFunction;

  beforeAll(() => {
    validate = compileSchema('plant-instance');
  });

  it('accepts a valid PlantInstance', () => {
    const ok = validate(validInstance);
    expect(validate.errors ?? []).toEqual([]);
    expect(ok).toBe(true);
  });

  it('accepts a valid PlantInstance with optional lastWateredAt', () => {
    const fixture = { ...validInstance, lastWateredAt: '2026-05-26T07:00:00Z' };
    const ok = validate(fixture);
    expect(validate.errors ?? []).toEqual([]);
    expect(ok).toBe(true);
  });

  it('rejects a PlantInstance missing containerId', () => {
    const bad = clone(validInstance) as Partial<typeof validInstance>;
    delete bad.containerId;
    const ok = validate(bad);
    expect(ok).toBe(false);
    expect(validate.errors ?? []).toEqual(
      expect.arrayContaining([
        expect.objectContaining({
          keyword: 'required',
          params: expect.objectContaining({ missingProperty: 'containerId' }),
        }),
      ]),
    );
  });

  it('rejects a PlantInstance missing gardenSpaceId', () => {
    const bad = clone(validInstance) as Partial<typeof validInstance>;
    delete bad.gardenSpaceId;
    const ok = validate(bad);
    expect(ok).toBe(false);
    expect(validate.errors ?? []).toEqual(
      expect.arrayContaining([
        expect.objectContaining({
          keyword: 'required',
          params: expect.objectContaining({ missingProperty: 'gardenSpaceId' }),
        }),
      ]),
    );
  });
});
