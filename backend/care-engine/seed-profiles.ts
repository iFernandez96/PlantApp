// Slice 1 seed PlantProfile catalog. Placeholder — the 5 profiles land in the
// green commit. Shape mirrors shared-schemas/plant-profile.schema.json (subset used
// by the engine + the seed-catalog test).
export interface SeedPlantProfile {
  id: string;
  scientificName: string;
  commonNames: string[];
  category: string;
  growthHabit: string;
  requiresSupport?: boolean;
  selfFruitful?: boolean | null;
  pollinationPartnersRequired?: number;
  wateringProfile: { baseIntervalDays: number; dryingTolerance: string };
  feedingProfile: { baseIntervalDays: number; fruitingIntervalDays?: number; postHarvestIntervalDays?: number };
  containerProfile: { recommendedMinLiters: number };
  lightProfile: { targetSunHours: number };
  temperatureProfile: { frostSensitive: boolean };
  version: number;
}

export const seedProfiles: SeedPlantProfile[] = [
  {
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
  {
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
  {
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
  {
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
  {
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
];
