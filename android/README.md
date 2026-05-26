# android

PlantApp Android skeleton. **Scaffolding only as of this commit; no Kotlin source.**

Pinned per accepted decisions (see `docs/slice-01-decision-log.md` and ADR-0002):
- Kotlin + Compose + Material 3 + Hilt + Room + DataStore.
- HTTP: Retrofit + OkHttp + kotlinx.serialization (D-02).
- Schema validation in tests only via `networknt/json-schema-validator` (D-06); production code uses kotlinx.serialization DTOs.
- No `:care-engine` Android module in Slice 1 (D-09). Backend is authoritative for task generation.
- No `:notifications`, no `:camera`, no AI dependencies in Slice 1.

## Modules (Slice 1)

```
:app                  application module — Compose, Hilt, Navigation
:design-system        Material 3 tokens + shared components
:domain               pure Kotlin (JVM) — use cases, models, ports
:data                 Room + DataStore + repository impls
:network              Retrofit + OkHttp + kotlinx.serialization
:feature-inventory    Slice 1 screens (add plant, list, detail)
```

## Generating the Gradle wrapper

The repository does not commit `gradlew`, `gradlew.bat`, or `gradle/wrapper/gradle-wrapper.jar`. After installing a recent Gradle (8.11.1 or newer):

```bash
cd android
gradle wrapper --gradle-version 8.11.1 --distribution-type bin
```

That step writes `gradlew`, `gradlew.bat`, and `gradle/wrapper/gradle-wrapper.jar`. The properties file at `gradle/wrapper/gradle-wrapper.properties` is already committed.

## Common commands (once `gradlew` is generated)

```bash
./gradlew tasks                     # list available tasks
./gradlew :app:assembleDebug        # build the debug APK
./gradlew :app:testDebugUnitTest    # run app-module unit tests
./gradlew :domain:test              # run pure-Kotlin domain tests
./gradlew lint                      # Android lint across modules
```

## A note on versions

Plugin, library, and Compose BOM versions in `gradle/libs.versions.toml` reflect the latest known-stable releases at scaffolding time and should be verified against current upstream releases before the first build:

- Android Gradle Plugin (`agp`)
- Kotlin (`kotlin`) and KSP (`ksp` — must match Kotlin's KSP suffix)
- Compose BOM and Material 3
- Hilt, Room, DataStore
- Retrofit, OkHttp, kotlinx.serialization, kotlinx-coroutines

If a version no longer resolves, bump it and record the rationale in the next commit message.
