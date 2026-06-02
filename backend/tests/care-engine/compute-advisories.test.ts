// Slice 2 advisory-engine tests (per docs/slice-02-implementation-plan.md and the
// @slice-2 scenarios in features/container-health.feature).
//
// RED-FIRST: computeAdvisories is loaded via a DYNAMIC import in beforeAll so a missing
// export fails each test individually ("computeAdvisories is not a function"), not at
// suite collection. Every emitted advisory is validated against advisory.schema.json.
import { describe, it, expect, beforeAll } from 'vitest';
import type { ValidateFunction } from 'ajv';
import { compileSchema } from '../schema/_helpers.js';

interface AdvisoryLike {
  kind: string;
  severity: string;
  plantInstanceId: string;
  profileId: string;
  title: string;
  message: string;
  details?: Record<string, unknown>;
}

let computeAdvisories: (input: unknown) => AdvisoryLike[];
let validateAdvisory: ValidateFunction;

beforeAll(async () => {
  const mod = await import('../../care-engine/advisories.js');
  computeAdvisories = (mod as unknown as {
    computeAdvisories: (input: unknown) => AdvisoryLike[];
  }).computeAdvisories;
  validateAdvisory = compileSchema('advisory');
});

const PLANT_ID = '00000000-0000-4000-8000-000000000001';

const passionProfile = {
  id: 'passiflora-edulis',
  commonNames: ['Passion fruit'],
  requiresSupport: true,
  selfFruitful: true,
  containerProfile: { recommendedMinLiters: 95, idealMinLiters: 95, idealMaxLiters: 190 },
};
const tomatilloProfile = {
  id: 'physalis-philadelphica',
  commonNames: ['Tomatillo'],
  requiresSupport: false,
  selfFruitful: false,
  pollinationPartnersRequired: 2,
  containerProfile: { recommendedMinLiters: 19 },
};
const basilProfile = {
  id: 'ocimum-basilicum',
  commonNames: ['Basil'],
  requiresSupport: false,
  selfFruitful: true,
  containerProfile: { recommendedMinLiters: 3 },
};

function input(overrides: Record<string, unknown> = {}) {
  return {
    plant: { id: PLANT_ID, profileId: passionProfile.id, supportRecorded: false },
    profile: passionProfile,
    container: { volumeLiters: 19 },
    profileInstanceCount: 1,
    ...overrides,
  };
}

function assertAllValid(advisories: AdvisoryLike[]) {
  for (const a of advisories) {
    const ok = validateAdvisory(a);
    expect(validateAdvisory.errors ?? []).toEqual([]);
    expect(ok).toBe(true);
  }
}

describe('computeAdvisories — Slice 2 (red-first)', () => {
  it('container-size: passion fruit in a 19L container → one high container-size advisory citing 95 and 190', () => {
    const advisories = computeAdvisories(input());
    const cs = advisories.filter((a) => a.kind === 'container-size');
    expect(cs).toHaveLength(1);
    expect(cs[0]!.severity).toBe('high');
    expect(cs[0]!.plantInstanceId).toBe(PLANT_ID);
    expect(cs[0]!.message).toContain('95');
    expect(cs[0]!.message).toContain('190');
    assertAllValid(advisories);
  });

  it('container-size: a 95L container → no container-size advisory', () => {
    const advisories = computeAdvisories(input({ container: { volumeLiters: 95 } }));
    expect(advisories.filter((a) => a.kind === 'container-size')).toHaveLength(0);
    assertAllValid(advisories);
  });

  it('support: requiresSupport && not recorded → one support advisory; recorded → none', () => {
    const without = computeAdvisories(input({ container: { volumeLiters: 95 } }));
    expect(without.filter((a) => a.kind === 'support')).toHaveLength(1);

    const recorded = computeAdvisories(
      input({ container: { volumeLiters: 95 }, plant: { id: PLANT_ID, profileId: passionProfile.id, supportRecorded: true } }),
    );
    expect(recorded.filter((a) => a.kind === 'support')).toHaveLength(0);
    assertAllValid(without)
    assertAllValid(recorded)
  });

  it('pollination: non-self-fruitful with too few partners → one advisory; enough → none', () => {
    const lone = computeAdvisories({
      plant: { id: PLANT_ID, profileId: tomatilloProfile.id, supportRecorded: false },
      profile: tomatilloProfile,
      container: { volumeLiters: 19 },
      profileInstanceCount: 1,
    });
    const poll = lone.filter((a) => a.kind === 'pollination')
    expect(poll).toHaveLength(1);
    expect(poll[0]!.message.toLowerCase()).toContain('self-fruitful');
    assertAllValid(lone);

    const paired = computeAdvisories({
      plant: { id: PLANT_ID, profileId: tomatilloProfile.id, supportRecorded: false },
      profile: tomatilloProfile,
      container: { volumeLiters: 19 },
      profileInstanceCount: 2,
    });
    expect(paired.filter((a) => a.kind === 'pollination')).toHaveLength(0);
  });

  it('no advisories for a well-provisioned, self-fruitful plant', () => {
    const advisories = computeAdvisories({
      plant: { id: PLANT_ID, profileId: basilProfile.id, supportRecorded: false },
      profile: basilProfile,
      container: { volumeLiters: 5 },
      profileInstanceCount: 1,
    });
    expect(advisories).toEqual([]);
  });

  it('invariant + determinism: returns Advisory[] (never a CareTask), equal inputs → equal output', () => {
    const a = computeAdvisories(input());
    const b = computeAdvisories(JSON.parse(JSON.stringify(input())));
    expect(b).toEqual(a);
    for (const adv of a) {
      // advisory shape, not a CareTask: no engineVersion/inputsHash/dueAt/kind-of-task fields
      expect(adv).not.toHaveProperty('engineVersion');
      expect(adv).not.toHaveProperty('inputsHash');
      expect(adv).not.toHaveProperty('dueAt');
      expect(['container-size', 'support', 'pollination']).toContain(adv.kind);
    }
    assertAllValid(a);
  });
});
