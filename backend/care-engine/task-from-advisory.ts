// Deterministic engine function: turn an *accepted* advisory into a CareTask (3d-engine).
//
// Pure function: no I/O, no Date.now, no randomness — same inputs → byte-equal output and
// identical inputsHash, mirroring computeInitialWaterTask in ./index.ts.
//
// INVARIANT: advisories never *auto*-create CareTasks. This function only ever runs when the
// (future) accept endpoint calls it on explicit user acceptance. It computes a task object and
// persists nothing.
import { createHash } from 'node:crypto';

const ENGINE_VERSION = '0.1.0';

export interface ComputeTaskFromAdvisoryInput {
  id: string; // caller-supplied uuid for the new task
  clockUtc: string; // ISO; the acceptance moment
  advisory: {
    kind: 'container-size' | 'support' | 'pollination';
    severity: 'low' | 'medium' | 'high';
    title: string;
    message: string;
  };
  plant: { id: string; profileId: string; containerId: string; gardenSpaceId: string };
  profile: { id: string; version: number };
}

export interface AdvisoryCareTaskSourceInputs {
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

export interface AdvisoryCareTask {
  id: string;
  plantInstanceId: string;
  kind: 'repot' | 'support';
  dueAt: string;
  priority: 'low' | 'normal' | 'high';
  rationale: string;
  rationaleMetadata: { acceptedAdvisoryKind: string };
  engineVersion: string;
  inputsHash: string;
  sourceInputs: AdvisoryCareTaskSourceInputs;
  status: 'pending';
}

// container-size → repot, support → support. pollination is "grow another plant", not a single
// actionable CareTask, so it (and any unknown kind) throws — the 3d-api endpoint maps the throw
// to HTTP 400.
const KIND_BY_ADVISORY: Record<string, 'repot' | 'support'> = {
  'container-size': 'repot',
  support: 'support',
};

const PRIORITY_BY_SEVERITY: Record<string, 'low' | 'normal' | 'high'> = {
  low: 'low',
  medium: 'normal',
  high: 'high',
};

/** Deterministic canonical JSON: recursively sorted object keys.
 *  (Local copy of the helper in ./index.ts — kept private so this file does not modify index.ts.) */
function canonicalJson(value: unknown): string {
  if (value === null || typeof value !== 'object') return JSON.stringify(value);
  if (Array.isArray(value)) return `[${value.map(canonicalJson).join(',')}]`;
  const obj = value as Record<string, unknown>;
  const keys = Object.keys(obj).sort();
  return `{${keys.map((k) => `${JSON.stringify(k)}:${canonicalJson(obj[k])}`).join(',')}}`;
}

export function computeTaskFromAdvisory(input: ComputeTaskFromAdvisoryInput): AdvisoryCareTask {
  const { id, clockUtc, advisory, plant, profile } = input;

  const kind = KIND_BY_ADVISORY[advisory.kind];
  if (kind === undefined) {
    throw new Error(`unsupported advisory kind: ${advisory.kind}`);
  }
  const priority = PRIORITY_BY_SEVERITY[advisory.severity] ?? 'normal';

  // sourceInputs is water-centric but schema-required for every CareTask. There is no watering
  // baseline for a repot/support task, so wateringBaselineAt is set to clockUtc as the required
  // placeholder.
  const sourceInputs: AdvisoryCareTaskSourceInputs = {
    plantInstanceId: plant.id,
    profileId: profile.id,
    profileVersion: profile.version,
    containerId: plant.containerId,
    gardenSpaceId: plant.gardenSpaceId,
    clockUtc,
    wateringBaselineAt: clockUtc,
    weatherWindowRef: null,
    feedbackWindowRef: null,
  };

  // Hash {kind, sourceInputs} (not sourceInputs alone like the water engine) so that accepting
  // different advisory kinds for the same plant+clock yields distinct inputsHash values.
  const inputsHash = createHash('sha256')
    .update(canonicalJson({ kind, sourceInputs }))
    .digest('hex');

  return {
    id,
    plantInstanceId: plant.id,
    kind,
    dueAt: clockUtc, // actionable immediately on acceptance
    priority,
    rationale: `${advisory.title}: ${advisory.message}`,
    rationaleMetadata: { acceptedAdvisoryKind: advisory.kind },
    engineVersion: ENGINE_VERSION,
    inputsHash,
    sourceInputs,
    status: 'pending',
  };
}
