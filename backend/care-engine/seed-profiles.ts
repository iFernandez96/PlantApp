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

export const seedProfiles: SeedPlantProfile[] = [];
