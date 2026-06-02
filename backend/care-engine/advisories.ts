// Slice 2 deterministic advisory engine (decision: advisories are profile-driven,
// computed on read, and NEVER create CareTasks). Pure function: no I/O, no Date.now,
// no randomness. Output conforms to shared-schemas/advisory.schema.json.

export interface Advisory {
  kind: 'container-size' | 'support' | 'pollination';
  severity: 'low' | 'medium' | 'high';
  plantInstanceId: string;
  profileId: string;
  title: string;
  message: string;
  details?: Record<string, unknown>;
}

export interface ComputeAdvisoriesInput {
  plant: { id: string; profileId: string; supportRecorded?: boolean };
  profile: {
    id: string;
    commonNames: string[];
    requiresSupport?: boolean;
    selfFruitful?: boolean | null;
    pollinationPartnersRequired?: number;
    containerProfile: {
      recommendedMinLiters: number;
      idealMinLiters?: number;
      idealMaxLiters?: number;
    };
  };
  container: { volumeLiters: number };
  /** Count of the caller's active instances of this profile (for pollination). */
  profileInstanceCount: number;
}

export function computeAdvisories(input: ComputeAdvisoriesInput): Advisory[] {
  const { plant, profile, container, profileInstanceCount } = input;
  const name = profile.commonNames[0] ?? profile.id;
  const advisories: Advisory[] = [];

  // container-size
  const cp = profile.containerProfile;
  if (container.volumeLiters < cp.recommendedMinLiters) {
    const hasIdeal = cp.idealMinLiters !== undefined && cp.idealMaxLiters !== undefined;
    const idealClause = hasIdeal
      ? ` (ideal ${cp.idealMinLiters}–${cp.idealMaxLiters} L)`
      : '';
    const details: Record<string, unknown> = {
      currentVolumeLiters: container.volumeLiters,
      recommendedMinLiters: cp.recommendedMinLiters,
    };
    if (cp.idealMinLiters !== undefined) details.idealMinLiters = cp.idealMinLiters;
    if (cp.idealMaxLiters !== undefined) details.idealMaxLiters = cp.idealMaxLiters;
    advisories.push({
      kind: 'container-size',
      severity: 'high',
      plantInstanceId: plant.id,
      profileId: profile.id,
      title: 'Container is smaller than recommended',
      message:
        `${name} prefers at least ${cp.recommendedMinLiters} L${idealClause}; ` +
        `this container is ${container.volumeLiters} L. Move it to a larger container of that target size.`,
      details,
    });
  }

  // support
  if (profile.requiresSupport === true && plant.supportRecorded !== true) {
    advisories.push({
      kind: 'support',
      severity: 'medium',
      plantInstanceId: plant.id,
      profileId: profile.id,
      title: 'Needs support',
      message: `${name} needs a trellis, stake, or cage for healthy growth; none is recorded for this plant.`,
    });
  }

  // pollination
  const requiredPartners = profile.pollinationPartnersRequired ?? 0;
  if (profile.selfFruitful === false && profileInstanceCount < requiredPartners) {
    advisories.push({
      kind: 'pollination',
      severity: 'medium',
      plantInstanceId: plant.id,
      profileId: profile.id,
      title: 'Add a compatible partner plant',
      message:
        `${name} is not self-fruitful; grow at least ${requiredPartners} compatible plants ` +
        `for reliable fruit set. You currently have ${profileInstanceCount}.`,
      details: { instanceCount: profileInstanceCount, requiredPartners },
    });
  }

  return advisories;
}
