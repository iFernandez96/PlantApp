// Care-engine red-first tests for computeTaskFromAdvisory (3d-engine).
//
// RED-FIRST: the engine function is loaded with a DYNAMIC import in beforeAll on
// purpose. A static `import { computeTaskFromAdvisory }` of a missing named export throws
// at module-link time and aborts the whole suite; loading the namespace and reading the
// (absent) export yields `undefined`, so each test fails individually with
// "computeTaskFromAdvisory is not a function" — the precise red we want. Do NOT convert
// this to a static named import. The file passes unchanged once the export exists.
import { describe, it, expect, beforeAll } from 'vitest';
import { compileSchema } from '../schema/_helpers.js';
import type { ValidateFunction } from 'ajv';

interface TaskLike {
  id: string;
  plantInstanceId: string;
  kind: string;
  dueAt: string;
  priority: string;
  rationale: string;
  rationaleMetadata?: Record<string, unknown>;
  engineVersion: string;
  inputsHash: string;
  status: string;
  sourceInputs: Record<string, unknown>;
}

let computeTaskFromAdvisory: (input: unknown) => TaskLike;
let validateCareTask: ValidateFunction;

beforeAll(async () => {
  const mod = await import('../../care-engine/task-from-advisory.js');
  computeTaskFromAdvisory = (mod as unknown as {
    computeTaskFromAdvisory: (input: unknown) => TaskLike;
  }).computeTaskFromAdvisory;
  validateCareTask = compileSchema('care-task');
});

const clock = '2026-06-02T07:00:00.000Z';
const plant = {
  id: '00000000-0000-4000-8000-000000000001',
  profileId: 'passiflora-edulis',
  containerId: '00000000-0000-4000-8000-000000000002',
  gardenSpaceId: '00000000-0000-4000-8000-000000000003',
};
const profile = { id: 'passiflora-edulis', version: 1 };

function input(overrides: Record<string, unknown> = {}) {
  return {
    id: '00000000-0000-4000-8000-0000000000aa',
    clockUtc: clock,
    advisory: {
      kind: 'container-size',
      severity: 'high',
      title: 'Container is smaller than recommended',
      message: 'Passion fruit prefers at least 95 L; this container is 19 L.',
    },
    plant,
    profile,
    ...overrides,
  };
}

describe('computeTaskFromAdvisory — accepted advisory → CareTask (red-first)', () => {
  it('container-size (high) → repot task, validates against care-task.schema.json', () => {
    const task = computeTaskFromAdvisory(input());
    expect(task.kind).toBe('repot');
    expect(task.priority).toBe('high');
    expect(task.dueAt).toBe(clock);
    expect(task.rationale).toContain('Passion fruit prefers at least 95 L');
    expect(task.sourceInputs.plantInstanceId).toBe(plant.id);
    expect(task.engineVersion).toBe('0.1.0');
    expect(task.status).toBe('pending');
    expect(task.rationaleMetadata).toMatchObject({ acceptedAdvisoryKind: 'container-size' });
    const ok = validateCareTask(task);
    expect(validateCareTask.errors ?? []).toEqual([]);
    expect(ok).toBe(true);
  });

  it('support (medium) → support task with normal priority, validates', () => {
    const task = computeTaskFromAdvisory(
      input({
        advisory: {
          kind: 'support',
          severity: 'medium',
          title: 'Needs a trellis',
          message: 'This vine needs vertical support.',
        },
      }),
    );
    expect(task.kind).toBe('support');
    expect(task.priority).toBe('normal');
    expect(validateCareTask(task)).toBe(true);
  });

  it('deterministic: identical input → deep-equal incl. identical inputsHash', () => {
    const a = computeTaskFromAdvisory(input());
    const b = computeTaskFromAdvisory(JSON.parse(JSON.stringify(input())));
    expect(b).toEqual(a);
    expect(JSON.stringify(b)).toBe(JSON.stringify(a));
    expect(b.inputsHash).toBe(a.inputsHash);
  });

  it('different advisory kinds for the same plant+clock → different inputsHash', () => {
    const repot = computeTaskFromAdvisory(input());
    const support = computeTaskFromAdvisory(
      input({
        advisory: { kind: 'support', severity: 'high', title: 't', message: 'm' },
      }),
    );
    expect(support.inputsHash).not.toBe(repot.inputsHash);
  });

  it('pollination (and any other kind) → throws unsupported advisory kind', () => {
    expect(() =>
      computeTaskFromAdvisory(
        input({ advisory: { kind: 'pollination', severity: 'low', title: 't', message: 'm' } }),
      ),
    ).toThrow(/unsupported advisory kind/);
  });
});
