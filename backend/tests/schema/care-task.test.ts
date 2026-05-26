// Test #5 (per docs/slice-01-implementation-plan.md):
// care-task.schema.json rejects records without sourceInputs, engineVersion,
// or inputsHash. Also positively confirms the full Slice 1 CareTask shape.
import { describe, it, expect, beforeAll } from 'vitest';
import type { ValidateFunction } from 'ajv';
import { compileSchema, clone } from './_helpers.js';

const validCareTask = {
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
};

describe('care-task.schema.json — test #5', () => {
  let validate: ValidateFunction;

  beforeAll(() => {
    validate = compileSchema('care-task');
  });

  it('accepts a valid CareTask', () => {
    const ok = validate(validCareTask);
    expect(validate.errors ?? []).toEqual([]);
    expect(ok).toBe(true);
  });

  it.each([
    'sourceInputs',
    'engineVersion',
    'inputsHash',
    'rationale',
    'dueAt',
    'priority',
    'status',
    'kind',
    'plantInstanceId',
    'id',
  ] as const)('rejects a CareTask missing %s', (field) => {
    const bad = clone(validCareTask) as Record<string, unknown>;
    delete bad[field];
    const ok = validate(bad);
    expect(ok).toBe(false);
    expect(validate.errors ?? []).toEqual(
      expect.arrayContaining([
        expect.objectContaining({
          keyword: 'required',
          params: expect.objectContaining({ missingProperty: field }),
        }),
      ]),
    );
  });

  it.each([
    'plantInstanceId',
    'profileId',
    'profileVersion',
    'containerId',
    'gardenSpaceId',
    'clockUtc',
    'wateringBaselineAt',
  ] as const)('rejects a CareTask whose sourceInputs is missing %s', (field) => {
    const bad = clone(validCareTask);
    delete (bad.sourceInputs as Record<string, unknown>)[field];
    const ok = validate(bad);
    expect(ok).toBe(false);
    expect(validate.errors ?? []).toEqual(
      expect.arrayContaining([
        expect.objectContaining({
          keyword: 'required',
          instancePath: '/sourceInputs',
          params: expect.objectContaining({ missingProperty: field }),
        }),
      ]),
    );
  });
});
