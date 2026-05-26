// Test #1 (per docs/slice-01-implementation-plan.md):
// plant-profile.schema.json accepts each seed profile
// (passion fruit, tomato, tomatillo, strawberry, basil).
//
// Red-first stage: fixtures inline. When the real seed catalog lands, these
// fixtures may be replaced by loading from the catalog data and asserting
// shape compatibility.
import { describe, it, expect, beforeAll } from 'vitest';
import type { ValidateFunction } from 'ajv';
import { compileSchema } from './_helpers.js';

const seedFixtures = [
  {
    species: 'passion fruit',
    profile: {
      id: 'passiflora-edulis',
      scientificName: 'Passiflora edulis',
      commonNames: ['Passion fruit', 'Maracujá'],
      category: 'fruit',
      growthHabit: 'climbing',
      requiresSupport: true,
      selfFruitful: true,
      wateringProfile: { baseIntervalDays: 3, dryingTolerance: 'medium' },
      feedingProfile: { baseIntervalDays: 14 },
      containerProfile: { recommendedMinLiters: 95 },
      lightProfile: { targetSunHours: 6 },
      temperatureProfile: { frostSensitive: true },
      version: 1,
    },
  },
  {
    species: 'tomato',
    profile: {
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
    species: 'tomatillo',
    profile: {
      id: 'physalis-philadelphica',
      scientificName: 'Physalis philadelphica',
      commonNames: ['Tomatillo'],
      category: 'fruit',
      growthHabit: 'bush',
      selfFruitful: false,
      pollinationPartnersRequired: 2,
      wateringProfile: { baseIntervalDays: 3, dryingTolerance: 'medium' },
      feedingProfile: { baseIntervalDays: 10 },
      containerProfile: { recommendedMinLiters: 19 },
      lightProfile: { targetSunHours: 7 },
      temperatureProfile: { frostSensitive: true },
      version: 1,
    },
  },
  {
    species: 'strawberry',
    profile: {
      id: 'fragaria-x-ananassa',
      scientificName: 'Fragaria x ananassa',
      commonNames: ['Strawberry'],
      category: 'berry',
      growthHabit: 'rosette',
      selfFruitful: true,
      wateringProfile: { baseIntervalDays: 2, dryingTolerance: 'low' },
      feedingProfile: { baseIntervalDays: 14, postHarvestIntervalDays: 21 },
      containerProfile: { recommendedMinLiters: 4 },
      lightProfile: { targetSunHours: 6 },
      temperatureProfile: { frostSensitive: false },
      version: 1,
    },
  },
  {
    species: 'basil',
    profile: {
      id: 'ocimum-basilicum',
      scientificName: 'Ocimum basilicum',
      commonNames: ['Basil'],
      category: 'herb',
      growthHabit: 'bush',
      selfFruitful: true,
      wateringProfile: { baseIntervalDays: 1.5, dryingTolerance: 'low' },
      feedingProfile: { baseIntervalDays: 14 },
      containerProfile: { recommendedMinLiters: 3 },
      lightProfile: { targetSunHours: 6 },
      temperatureProfile: { frostSensitive: true },
      version: 1,
    },
  },
];

describe('plant-profile.schema.json — test #1: accepts each seed profile', () => {
  let validate: ValidateFunction;

  beforeAll(() => {
    validate = compileSchema('plant-profile');
  });

  it.each(seedFixtures)('accepts the $species seed profile', ({ profile }) => {
    const ok = validate(profile);
    expect(validate.errors ?? []).toEqual([]);
    expect(ok).toBe(true);
  });
});
