# PlantApp — Domain Model

Status: Draft v0.1 — 2026-05-26

## 1. Core entities

### PlantProfile (species-level, catalog)

Data-driven plant knowledge. Source of truth for "how does this species behave."

- `id` (slug, e.g. `solanum-lycopersicum`)
- `commonNames` (string[])
- `scientificName` (string)
- `category` (enum: fruit, vegetable, herb, ornamental, vine, root, berry, …)
- `growthHabit` (enum: bush, vine, trailing, upright, climbing, rosette)
- `requiresSupport` (bool) — trellis/cage/stake required
- `selfFruitful` (bool | null) — null when not applicable; false flags pollination warnings (tomatillo)
- `pollinationPartnersRequired` (int) — e.g. tomatillo = 2
- `wateringProfile` — base mm/day or interval band, drying tolerance, sensitivity to overwatering
- `feedingProfile` — NPK preferences, frequency band, fruiting-stage adjustment
- `containerProfile` — recommended min container volume (liters), ideal volume, max practical, soil mix tags
- `lightProfile` — full-sun hours target, tolerance range
- `temperatureProfile` — cold-hardy min, heat-stress threshold, frost-sensitive bool
- `seasonality` — planting windows by hardiness zone, productive months, dormancy
- `commonIssues` (string[]) — pest/disease tags for diagnosis correlation
- `verticalSuitability` — score for shelf/hanging/trellis stacking
- `source` — provenance (extension service citation, etc.)

Schema: `shared-schemas/plant-profile.schema.json`.

### PlantInstance (the actual plant a user owns)

- `id` (uuid)
- `userId`
- `profileId` → PlantProfile
- `nickname` (string, optional)
- `cultivar` (string, optional — e.g. "Cherokee Purple")
- `containerId` → Container
- `gardenSpaceId` → GardenSpace
- `placement` (enum: floor, shelf, railing, hanging, trellis, vertical-rack)
- `placementHeightCm` (int, optional)
- `acquiredAt` (date)
- `plantedAt` (date)
- `growthStage` (enum: seed, seedling, vegetative, flowering, fruiting, dormant, harvested)
- `notes` (string)
- `photos` (storageKey[])
- `createdAt`, `updatedAt`

Schema: `shared-schemas/plant-instance.schema.json`.

### Container (first-class)

Schema: `shared-schemas/container.schema.json`.

- `id` (uuid), `userId`
- `name` (string, optional)
- `volumeLiters` (decimal, > 0)
- `material` (enum: terracotta, plastic, fabric, glazed-ceramic, wood, metal, self-watering, other)
- `drainage` (enum: good, moderate, poor)
- `selfWatering` (bool)
- `saucer` (bool)
- `soilMix` (string)
- `createdAt`, `updatedAt`

### GardenSpace (first-class)

Schema: `shared-schemas/garden-space.schema.json`.

- `id`, `userId`, `name`
- `kind` (enum: balcony, patio, window-ledge, indoor-room, vertical-rack-zone, hanging-zone, grow-light-shelf)
- `location` — zip/postal code or coarse lat/lon (privacy-controlled)
- `hardinessZone` (cached from USDA PHZM)
- `direction` (enum: N, NE, E, SE, S, SW, W, NW)
- `sunHoursEstimate` (int)
- `windExposure` (enum: low, medium, high)
- `shadeFraction` (0-1)
- `rainReaches` (bool)
- `dimensionsCm` ({widthCm, depthCm, heightCm})
- `photos` (storageKey[])
- `verticalCapacity` (bool, derived from dimensions/placement options)

### CareTask

- `id`, `plantInstanceId`
- `kind` (enum: water, feed, prune, repot, scout-pests, harvest, support, rotate, seasonal-prep)
- `dueAt` (timestamp)
- `priority` (enum: low, normal, high, urgent)
- `rationale` (string — short human-readable)
- `engineVersion` (semver string)
- `inputsHash` (string — SHA-256 of canonical-JSON sourceInputs)
- `sourceInputs` (object — explicit references to plantInstanceId, profileId+profileVersion, containerId, gardenSpaceId, clockUtc, and optional weather/feedback window refs). This is the auditable "source inputs" set required for every task.
- `status` (enum: pending, done, skipped, dismissed)
- `completedAt`, `feedback` (enum: on-time, early, late, soil-still-wet, plant-wilted, fertilizer-too-strong, …)

Schema: `shared-schemas/care-task.schema.json`.

### CareLogEvent

Append-only log of what the user actually did. Feeds back into the engine.

- `id`, `plantInstanceId`, `kind`, `at`, `feedback`, `notes`, `photos`

### DiagnosisResult

- `id`, `plantInstanceId`, `photos` (storageKey[]), `requestedAt`
- `findings` ({label, confidence, evidence}[])
- `suspectedCauses` (string[])
- `recommendations` ({summary, suggestedTaskKind, urgency}[])
- `caveats` (string[])
- `promptVersion`, `model`, `latencyMs`

Schema: `shared-schemas/diagnosis-result.schema.json`.

### SpacePlan

- `id`, `gardenSpaceId`, `requestedAt`, `inputs` (photos, measurements, plant list)
- `layout` (zones[] with placements[], stacking, sun-mapping)
- `verticalRecommendations`, `horizontalRecommendations`
- `tradeoffs` (string[])
- `promptVersion`, `model`

Schema: `shared-schemas/space-plan.schema.json`.

## 2. Relationships

```
User 1───* GardenSpace 1───* PlantInstance *───1 PlantProfile
                                  │
                                  *
                              Container
PlantInstance 1───* CareTask
PlantInstance 1───* CareLogEvent
PlantInstance 1───* DiagnosisResult
GardenSpace   1───* SpacePlan
```

## 3. Invariants

- A `PlantInstance` always references both a `Container` and a `GardenSpace`.
- `CareTask.engineVersion` is required and immutable after creation.
- `CareTask.sourceInputs` is required and immutable. Recomputation produces a new `CareTask`; it never mutates an existing one in place.
- Hard-coded species logic is forbidden. All species behavior derives from `PlantProfile`.
- `selfFruitful=false` + active `PlantInstance` count of that profile = 1 → triggers a pollination-warning notification (not a CareTask).
- A `PlantInstance` whose `Container.volumeLiters` is below `PlantProfile.containerProfile.recommendedMinLiters` triggers a container-size advisory.

## 4. Versioning and migration

- `PlantProfile` records carry a `version` and `lastReviewedAt`. Updates do not retroactively rewrite historical `CareTask` rationales.
- Schemas in `shared-schemas/` are versioned with `$id` and a major version. Breaking changes get a new file (`v2.schema.json`) and an ADR.
