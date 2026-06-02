// Slice 1 deterministic watering engine (decision D-10).
// Pure function: same inputs -> byte-equal output and identical inputsHash.
import { createHash } from 'node:crypto';

export interface ComputeInitialWaterTaskInput {
  id: string;
  clockUtc: string;
  plant: {
    id: string;
    profileId: string;
    containerId: string;
    gardenSpaceId: string;
    createdAt: string;
    lastWateredAt?: string;
  };
  profile: {
    id: string;
    version: number;
    commonNames: string[];
    wateringProfile: { baseIntervalDays: number };
    containerProfile: { recommendedMinLiters: number };
  };
  container: { id: string; volumeLiters: number };
  gardenSpace: { id: string };
}

export interface CareTaskSourceInputs {
  plantInstanceId: string;
  profileId: string;
  profileVersion: number;
  containerId: string;
  gardenSpaceId: string;
  clockUtc: string;
  wateringBaselineAt: string;
  weatherWindowRef: string | null;
  feedbackWindowRef: string | null;
}

export interface CareTask {
  id: string;
  plantInstanceId: string;
  kind: 'water';
  dueAt: string;
  priority: 'normal';
  rationale: string;
  engineVersion: string;
  inputsHash: string;
  sourceInputs: CareTaskSourceInputs;
  status: 'pending';
}

const ENGINE_VERSION = '0.1.0';
const DAY_MS = 86_400_000;

function clamp(n: number, lo: number, hi: number): number {
  return Math.min(hi, Math.max(lo, n));
}

/** Deterministic canonical JSON: recursively sorted object keys. */
function canonicalJson(value: unknown): string {
  if (value === null || typeof value !== 'object') return JSON.stringify(value);
  if (Array.isArray(value)) return `[${value.map(canonicalJson).join(',')}]`;
  const obj = value as Record<string, unknown>;
  const keys = Object.keys(obj).sort();
  return `{${keys.map((k) => `${JSON.stringify(k)}:${canonicalJson(obj[k])}`).join(',')}}`;
}

export function computeInitialWaterTask(input: ComputeInitialWaterTaskInput): CareTask {
  const { id, clockUtc, plant, profile, container, gardenSpace } = input;

  const wateringBaselineAt = plant.lastWateredAt ?? plant.createdAt;
  const baseIntervalDays = profile.wateringProfile.baseIntervalDays;
  const containerFactor = clamp(
    container.volumeLiters / profile.containerProfile.recommendedMinLiters,
    0.5,
    1.5,
  );
  const dueAt = new Date(
    new Date(wateringBaselineAt).getTime() + baseIntervalDays * containerFactor * DAY_MS,
  ).toISOString();

  const sourceInputs: CareTaskSourceInputs = {
    plantInstanceId: plant.id,
    profileId: profile.id,
    profileVersion: profile.version,
    containerId: container.id,
    gardenSpaceId: gardenSpace.id,
    clockUtc,
    wateringBaselineAt,
    weatherWindowRef: null,
    feedbackWindowRef: null,
  };

  const inputsHash = createHash('sha256').update(canonicalJson(sourceInputs)).digest('hex');

  const rationale = `${profile.commonNames[0]}: base interval ${baseIntervalDays}d adjusted by container factor ${containerFactor}; baseline ${wateringBaselineAt}`;

  return {
    id,
    plantInstanceId: plant.id,
    kind: 'water',
    dueAt,
    priority: 'normal',
    rationale,
    engineVersion: ENGINE_VERSION,
    inputsHash,
    sourceInputs,
    status: 'pending',
  };
}
