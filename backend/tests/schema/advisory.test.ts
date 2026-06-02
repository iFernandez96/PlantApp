// Slice 2 — Advisory contract test. Validates advisory.schema.json accepts a valid
// advisory of each kind and rejects unknown kind/severity and missing required fields.
import { describe, it, expect, beforeAll } from 'vitest';
import type { ValidateFunction } from 'ajv';
import { compileSchema, clone } from './_helpers.js';

const validContainerSize = {
  kind: 'container-size',
  severity: 'high',
  plantInstanceId: '00000000-0000-4000-8000-000000000001',
  profileId: 'passiflora-edulis',
  title: 'Container is too small',
  message:
    'Passion fruit prefers at least 95 L (ideal 95-190 L); this container is 19 L. Consider a larger container.',
  details: { recommendedMinLiters: 95, idealMinLiters: 95, idealMaxLiters: 190, currentVolumeLiters: 19 },
  createdAt: '2026-05-26T07:00:00.000Z',
};

const validSupport = {
  kind: 'support',
  severity: 'medium',
  plantInstanceId: '00000000-0000-4000-8000-000000000001',
  profileId: 'solanum-lycopersicum',
  title: 'Needs support',
  message: 'Tomato is a vining plant and benefits from a stake or cage.',
};

const validPollination = {
  kind: 'pollination',
  severity: 'medium',
  plantInstanceId: '00000000-0000-4000-8000-000000000001',
  profileId: 'physalis-philadelphica',
  title: 'Add a second tomatillo',
  message: 'Tomatillos are not self-fruitful; plant at least two for reliable fruit set.',
  details: { instanceCount: 1, requiredPartners: 2 },
};

describe('advisory.schema.json — Slice 2 contract', () => {
  let validate: ValidateFunction;
  beforeAll(() => {
    validate = compileSchema('advisory');
  });

  it.each([
    ['container-size', validContainerSize],
    ['support', validSupport],
    ['pollination', validPollination],
  ])('accepts a valid %s advisory', (_label, fixture) => {
    const ok = validate(fixture);
    expect(validate.errors ?? []).toEqual([]);
    expect(ok).toBe(true);
  });

  it('rejects an unknown kind', () => {
    const bad = clone(validSupport) as { kind: string } & typeof validSupport;
    bad.kind = 'weather';
    expect(validate(bad)).toBe(false);
    expect(validate.errors ?? []).toEqual(
      expect.arrayContaining([
        expect.objectContaining({ keyword: 'enum', instancePath: '/kind' }),
      ]),
    );
  });

  it('rejects an unknown severity', () => {
    const bad = clone(validSupport) as { severity: string } & typeof validSupport;
    bad.severity = 'critical';
    expect(validate(bad)).toBe(false);
    expect(validate.errors ?? []).toEqual(
      expect.arrayContaining([
        expect.objectContaining({ keyword: 'enum', instancePath: '/severity' }),
      ]),
    );
  });

  it.each(['kind', 'severity', 'plantInstanceId', 'profileId', 'title', 'message'] as const)(
    'rejects an advisory missing %s',
    (field) => {
      const bad = clone(validContainerSize) as Record<string, unknown>;
      delete bad[field];
      expect(validate(bad)).toBe(false);
      expect(validate.errors ?? []).toEqual(
        expect.arrayContaining([
          expect.objectContaining({
            keyword: 'required',
            params: expect.objectContaining({ missingProperty: field }),
          }),
        ]),
      );
    },
  );
});
