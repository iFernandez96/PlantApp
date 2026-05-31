// Test #4 (per docs/slice-01-implementation-plan.md):
// garden-space.schema.json rejects empty name and missing kind.
//
// garden-space.schema.json enforces `"minLength": 1` on `name`
// (shared-schemas/garden-space.schema.json), so the empty-name case below is a
// passing regression guard, not a pending red contract.
import { describe, it, expect, beforeAll } from 'vitest';
import type { ValidateFunction } from 'ajv';
import { compileSchema, clone } from './_helpers.js';

const validGardenSpace = {
  id: '00000000-0000-4000-8000-000000000020',
  userId: 'user-owner',
  name: 'West Balcony',
  kind: 'balcony',
  createdAt: '2026-05-26T07:00:00Z',
};

describe('garden-space.schema.json — test #4', () => {
  let validate: ValidateFunction;

  beforeAll(() => {
    validate = compileSchema('garden-space');
  });

  it('accepts a valid GardenSpace', () => {
    const ok = validate(validGardenSpace);
    expect(validate.errors ?? []).toEqual([]);
    expect(ok).toBe(true);
  });

  it('rejects a GardenSpace with an empty name', () => {
    const bad = clone(validGardenSpace);
    bad.name = '';
    const ok = validate(bad);
    expect(ok).toBe(false);
    expect(validate.errors ?? []).toEqual(
      expect.arrayContaining([
        expect.objectContaining({ instancePath: '/name' }),
      ]),
    );
  });

  it('rejects a GardenSpace missing kind', () => {
    const bad = clone(validGardenSpace) as Partial<typeof validGardenSpace>;
    delete bad.kind;
    const ok = validate(bad);
    expect(ok).toBe(false);
    expect(validate.errors ?? []).toEqual(
      expect.arrayContaining([
        expect.objectContaining({
          keyword: 'required',
          params: expect.objectContaining({ missingProperty: 'kind' }),
        }),
      ]),
    );
  });
});
