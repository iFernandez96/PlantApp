# ADR-0002 — Android Stack

Date: 2026-05-26
Status: Accepted — 2026-05-26 (pins D-02, D-06, D-07 from `docs/slice-01-decision-log.md`)

## Context

We need a modern Android stack that gives us fast UI iteration, strong testing primitives, reliable background work, camera access, and clean DI. The owner is the first and only user for MVP and runs a current Android version.

## Decision

- **Language:** Kotlin only.
- **UI:** Jetpack Compose + Material 3.
- **State / arch:** ViewModels + Kotlin Coroutines/Flow. Unidirectional data flow per screen.
- **DI:** Hilt.
- **Persistence:** Room for relational entities; DataStore (Preferences) for small key/value config.
- **Background:** WorkManager for scheduled reminders and retry-with-backoff jobs.
- **Push:** Firebase Cloud Messaging.
- **Camera:** CameraX with explicit EXIF-stripping step before upload.
- **HTTP:** **Retrofit + OkHttp** with the **kotlinx.serialization converter** (decision log D-02, accepted 2026-05-26). Chosen for its mature tooling on Android (logging interceptors, mock web server, well-trodden auth-refresh patterns). Ktor client remains the fallback if a Kotlin Multiplatform client is later adopted.
- **Serialization:** **kotlinx.serialization** with **hand-written DTOs** validated against `shared-schemas/`. Full JSON-Schema runtime validation only in tests, using `networknt/json-schema-validator` (decision log D-06, accepted 2026-05-26). This keeps the production APK from bundling a full JSON-Schema runtime.
- **Image loading:** Coil 3.
- **Navigation:** Compose Navigation; type-safe routes.
- **Testing:** JUnit + kotlinx-coroutines-test + Turbine for Flows; Compose UI testing; Robolectric where useful; Paparazzi or Roborazzi for screenshot tests on stable screens.

## Module layout

```
:app                       composition root
:design-system             tokens, components, theming
:domain                    use cases, models, ports (pure Kotlin)
:data                      Room, DataStore, repository impls
:care-engine               pure Kotlin (mirrors backend module; MVP server-authoritative)
:network                   Ktor client, DTOs, schema validation
:notifications             FCM + WorkManager schedulers
:camera                    CameraX wrappers, EXIF stripping
:feature-inventory
:feature-care
:feature-diagnosis
:feature-space
```

## Alternatives considered

- **XML Views.** Rejected: Compose is the strategic UI direction; Material 3 design tokens are first-class on Compose.
- **Dagger (raw) instead of Hilt.** Rejected: Hilt is the supported ergonomic layer.
- **Ktor client.** Considered for Kotlin symmetry and Multiplatform optionality; rejected for Slice 1 (D-02) in favor of Retrofit's tooling maturity. Re-evaluate if KMP enters the picture.
- **Bundling a full JSON-Schema runtime on Android.** Rejected (D-06): increases APK size without proportional value; production code paths can rely on compile-time DTO types.

## Consequences

- Compose lock-in is acceptable given app size and our timeline.
- All framework-specific code lives in feature/network/data modules — `:domain` stays framework-free for portability and test speed. (Per D-09, no `:care-engine` module on Android for Slice 1; backend is authoritative.)
- Crash reporting deferred to Slice 3 (D-07): Slice 1 ships in-app only on a single device, so manual capture is acceptable until notifications arrive.
