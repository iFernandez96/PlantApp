# PlantApp — Privacy & Threat Model

Status: Draft v0.1 — 2026-05-26

## 1. Sensitive data classes

| Class | Examples | Sensitivity |
|---|---|---|
| Identity | email, account id | medium |
| Location | zip/postal code, coarse lat/lon | medium (proxy for home) |
| Garden imagery | balcony/condo photos | **high** (reveals home interior, address-correlated) |
| Plant photos | leaf, fruit, soil close-ups | medium |
| Care behavior | watering log, notes | low |
| AI logs | prompts, responses, model metadata | medium |

Garden-space imagery is treated as the highest-risk class because it can reveal home layout and be linked to a residence.

## 2. Threats (STRIDE-flavored, abbreviated)

| # | Threat | Affected data | Mitigation |
|---|---|---|---|
| T1 | Photo leak via public storage URL | photos | Private buckets only, signed URLs with short TTL, no list permissions |
| T2 | EXIF GPS exfil in plant or balcony photo | photos | Strip EXIF GPS on-device before upload unless user opts in |
| T3 | LLM provider retention of photos | photos, AI logs | Use zero-retention provider mode where available; document choice in `ai-architecture.md` |
| T4 | API key compromise (OpenAI, weather) | account, billing | Keys live only in backend secrets manager; never in Android |
| T5 | Account takeover | identity, all user data | Strong auth (passkeys/OAuth + email magic link), session rotation |
| T6 | Insider/log exposure of PII | identity, location, photos | Log allow-list; no photo bytes or precise location in logs; redaction at logger boundary |
| T7 | Prompt injection via OCR'd image text | AI flow | Strict structured outputs, schema validation, ignore-text rule in system prompt |
| T8 | Cross-user data exposure via Supabase RLS gaps | all | Row-level security per `userId` on every table; tests assert RLS |
| T9 | Stale auth on lost device | all | Short-lived JWTs + refresh rotation; remote revoke list |
| T10 | Backup/export leak | all | User export endpoint produces signed, expiring archive; no public CDN |

## 3. Data minimization

- Collect zip/postal code rather than precise GPS by default.
- Strip EXIF GPS before upload (CameraX pipeline).
- Compress and downscale images before AI inference (smaller blast radius).
- Do not retain raw image bytes server-side longer than needed for the active diagnosis context (TTL: 30 days, configurable).
- Store only references (storage keys + signed URL minted on demand) in DB rows.

## 4. Retention

| Data | Retention default | User control |
|---|---|---|
| Account | until deletion | Self-service delete |
| Plant inventory | until user deletes | Per-plant delete |
| Care logs | indefinite for adherence trends | Per-period delete |
| Photos | 30 days raw, thumbnails as user keeps them | Per-photo delete |
| AI request logs | 90 days, no PII content | Aggregate kept post-retention |
| Crash reports | 90 days | N/A |

## 5. Permissions (Android)

- **Notifications** — required on Android 13+ for reminders. Permission requested at first reminder setup.
- **Camera** — required for AI diagnosis and space-optimization capture.
- **Location** — **NOT** required. Zip-code entry is the default. Coarse location only if user opts in.
- **Background location** — never requested.
- **Foreground service** — only if a watering session UI is added later; otherwise none.

## 6. Logging rules

- Never log: raw photo bytes, photo URLs (use storage key suffix only), precise lat/lon, full email, nicknames, AI prompt content with photo refs, AI response free-text.
- Always log: event name, user id (hashed), plant id, decision metadata, schema validation outcomes, engine version.

## 7. Slice 1 privacy posture (locked)

Slice 1 ships **no** photo capture, **no** AI flows, **no** precise location, **no** notifications. The privacy surface is therefore narrow but explicit:

- **Photos**: not collected. Android app does not request CAMERA permission in Slice 1. The `photos` arrays in `PlantInstance`/`GardenSpace` remain empty.
- **Location**: only `postalCode` (and optional `countryCode`) on `GardenSpace`. Precise GPS, background location, and EXIF GPS handling are out of scope (Slice 7+).
- **AI**: the Android app contains **zero** AI SDK dependencies in Slice 1. The backend `ai-gateway` module is not built or deployed.
- **Notifications**: not requested. Tasks are visible in-app only.
- **RLS**: every user-owned table (`plant_instances`, `containers`, `garden_spaces`, `care_tasks`, `care_log_events`) has row-level security enforced per `userId`. Tests under `@slice-1 @authorization` assert that cross-user reads are denied.
- **Logs**: allow-list enforced from day one. Never logged: `nickname`, `postalCode`, raw user-agent IP precise location, any `*.notes` field, plant ids in plain text (use hashed ids if needed). Always logged: event name, hashed `userId`, hashed `plantInstanceId`, decision metadata, schema validation outcomes, engine version.
- **Secrets**: no LLM provider keys exist in any environment yet. Supabase service-role key is backend-only.

## 8. Threat-model review cadence

- Re-review on every ADR touching auth, storage, AI, or telemetry.
- Quarterly audit of log fields against allow-list.
