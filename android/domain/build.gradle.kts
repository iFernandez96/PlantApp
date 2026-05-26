// :domain is a pure Kotlin (JVM) library per ADR-0002. No Android dependencies.
// Per D-09, there is no :care-engine Android module in Slice 1 — the care-engine
// lives in the backend. Care-engine domain types consumed by Android are still
// modeled here but match the backend's TypeScript engine via shared JSON Schemas.
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
}
