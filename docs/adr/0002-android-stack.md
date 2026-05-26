# ADR-0002 — Android Stack

Date: 2026-05-26
Status: Proposed

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
- **HTTP:** Ktor client (Kotlin-first, multiplatform-friendly) or Retrofit + OkHttp. Default to **Ktor client** for symmetry with potential Kotlin backend; revisit if a Retrofit-only feature is needed.
- **Serialization:** kotlinx.serialization with JSON Schema-driven codegen (or hand-written DTOs validated against shared schemas).
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
- **Retrofit + Moshi.** Acceptable; chosen Ktor for Kotlin symmetry and multiplatform optionality. Will re-evaluate when first network slice lands.

## Consequences

- Compose lock-in is acceptable given app size and our timeline.
- All framework-specific code lives in feature/network/data modules — `:domain` and `:care-engine` stay framework-free for portability and test speed.

## Open

- Crash reporting choice (Crashlytics vs Sentry) — to be decided in a follow-up ADR before slice 7.
