// Test #3 (per docs/slice-01-implementation-plan.md):
// container.schema.json rejects volumeLiters <= 0 and unknown material.
import { describe, it, expect, beforeAll } from 'vitest';
import type { ValidateFunction } from 'ajv';
import { compileSchema, clone } from './_helpers.js';

const validContainer = {
  id: '00000000-0000-4000-8000-000000000010',
  userId: 'user-owner',
  name: 'Blue barrel',
  volumeLiters: 19,
  material: 'plastic',
  drainage: 'good',
  createdAt: '2026-05-26T07:00:00Z',
};

describe('container.schema.json — test #3', () => {
  let validate: ValidateFunction;

  beforeAll(() => {
    validate = compileSchema('container');
  });

  it('accepts a valid Container', () => {
    const ok = validate(validContainer);
    expect(validate.errors ?? []).toEqual([]);
    expect(ok).toBe(true);
  });

  it('rejects a Container with volumeLiters = 0', () => {
    const bad = clone(validContainer);
    bad.volumeLiters = 0;
    const ok = validate(bad);
    expect(ok).toBe(false);
    expect(validate.errors ?? []).toEqual(
      expect.arrayContaining([
        expect.objectContaining({
          instancePath: '/volumeLiters',
        }),
      ]),
    );
  });

  it('rejects a Container with volumeLiters < 0', () => {
    const bad = clone(validContainer);
    bad.volumeLiters = -5;
    const ok = validate(bad);
    expect(ok).toBe(false);
    expect(validate.errors ?? []).toEqual(
      expect.arrayContaining([
        expect.objectContaining({
          instancePath: '/volumeLiters',
        }),
      ]),
    );
  });

  it('rejects a Container with an unknown material', () => {
    const bad = clone(validContainer) as { material: string } & typeof validContainer;
    bad.material = 'moon-rock';
    const ok = validate(bad);
    expect(ok).toBe(false);
    expect(validate.errors ?? []).toEqual(
      expect.arrayContaining([
        expect.objectContaining({
          keyword: 'enum',
          instancePath: '/material',
        }),
      ]),
    );
  });
});
