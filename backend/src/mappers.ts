// DB-row (snake_case) → shared-schema (camelCase) response mappers.
// Each returns an object conforming to the matching shared-schemas/*.schema.json,
// omitting null/absent optional fields so the additionalProperties:false schemas
// validate. No field is invented beyond the schema.

type Row = Record<string, unknown>;

/** Assigns key→value only when value is neither null nor undefined. */
function put(target: Row, key: string, value: unknown): void {
  if (value !== null && value !== undefined) target[key] = value;
}

export function toGardenSpace(row: Row): Row {
  const out: Row = {
    id: row.id,
    userId: row.user_id,
    name: row.name,
    kind: row.kind,
  };
  put(out, 'indoor', row.indoor);
  put(out, 'postalCode', row.postal_code);
  put(out, 'countryCode', row.country_code);
  put(out, 'hardinessZone', row.hardiness_zone);
  put(out, 'direction', row.direction);
  put(out, 'sunHoursEstimate', row.sun_hours_estimate);
  put(out, 'windExposure', row.wind_exposure);
  put(out, 'shadeFraction', row.shade_fraction);
  put(out, 'rainReaches', row.rain_reaches);
  put(out, 'dimensionsCm', row.dimensions_cm);
  put(out, 'photos', row.photos);
  put(out, 'createdAt', row.created_at);
  put(out, 'updatedAt', row.updated_at);
  return out;
}

export function toContainer(row: Row): Row {
  const out: Row = {
    id: row.id,
    userId: row.user_id,
    volumeLiters: row.volume_liters === null || row.volume_liters === undefined
      ? row.volume_liters
      : Number(row.volume_liters),
    material: row.material,
    drainage: row.drainage,
  };
  put(out, 'name', row.name);
  put(out, 'selfWatering', row.self_watering);
  put(out, 'saucer', row.saucer);
  put(out, 'soilMix', row.soil_mix);
  put(out, 'createdAt', row.created_at);
  put(out, 'updatedAt', row.updated_at);
  return out;
}

export function toPlantInstance(row: Row): Row {
  const out: Row = {
    id: row.id,
    userId: row.user_id,
    profileId: row.profile_id,
    containerId: row.container_id,
    gardenSpaceId: row.garden_space_id,
    growthStage: row.growth_stage,
  };
  put(out, 'nickname', row.nickname);
  put(out, 'cultivar', row.cultivar);
  put(out, 'placement', row.placement);
  put(out, 'placementHeightCm', row.placement_height_cm);
  put(out, 'acquiredAt', row.acquired_at);
  put(out, 'plantedAt', row.planted_at);
  put(out, 'lastWateredAt', row.last_watered_at);
  put(out, 'supportRecorded', row.support_recorded);
  put(out, 'notes', row.notes);
  put(out, 'photos', row.photos);
  put(out, 'createdAt', row.created_at);
  put(out, 'updatedAt', row.updated_at);
  return out;
}

export function toCareTask(row: Row): Row {
  const out: Row = {
    id: row.id,
    plantInstanceId: row.plant_instance_id,
    kind: row.kind,
    dueAt: row.due_at,
    priority: row.priority,
    rationale: row.rationale,
    engineVersion: row.engine_version,
    inputsHash: row.inputs_hash,
    sourceInputs: row.source_inputs,
    status: row.status,
  };
  put(out, 'rationaleMetadata', row.rationale_metadata);
  put(out, 'completedAt', row.completed_at);
  put(out, 'feedback', row.feedback);
  put(out, 'createdAt', row.created_at);
  return out;
}

export function toPlantProfile(row: Row): Row {
  const out: Row = {
    id: row.id,
    scientificName: row.scientific_name,
    commonNames: row.common_names,
    category: row.category,
    growthHabit: row.growth_habit,
    requiresSupport: row.requires_support,
    pollinationPartnersRequired: row.pollination_partners_required,
    wateringProfile: row.watering_profile,
    feedingProfile: row.feeding_profile,
    containerProfile: row.container_profile,
    lightProfile: row.light_profile,
    temperatureProfile: row.temperature_profile,
    version: row.version,
  };
  put(out, 'selfFruitful', row.self_fruitful);
  put(out, 'seasonality', row.seasonality);
  put(out, 'commonIssues', row.common_issues);
  put(
    out,
    'verticalSuitability',
    row.vertical_suitability === null || row.vertical_suitability === undefined
      ? row.vertical_suitability
      : Number(row.vertical_suitability),
  );
  put(out, 'source', row.source);
  put(out, 'lastReviewedAt', row.last_reviewed_at);
  return out;
}
