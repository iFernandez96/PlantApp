plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "dev.plantapp.data"
    compileSdk = 35

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        minSdk = 26

        // Base URLs are build-overridable (pass -Pplantapp.apiBaseUrl / -Pplantapp.authBaseUrl to
        // target a LAN-hosted backend from a device). Defaults are the emulator loopback values;
        // the PlantApp API default points at the Fastify server (:3000), auth at Supabase (:54321).
        val apiBase = (project.findProperty("plantapp.apiBaseUrl") as String?) ?: "http://10.0.2.2:3000/"
        val authBase = (project.findProperty("plantapp.authBaseUrl") as String?) ?: "http://10.0.2.2:54321/"
        buildConfigField("String", "API_BASE_URL", "\"$apiBase\"")
        buildConfigField("String", "AUTH_BASE_URL", "\"$authBase\"")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true // Robolectric (WorkManager test harness) needs resources
            all { it.useJUnit() }
        }
    }
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":network"))

    // Room is deferred (Slice 1 reads live from the backend; no offline cache yet — D-09).
    implementation(libs.datastore.preferences)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(libs.retrofit) // for retrofit2.Response handling in the repository impl
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.androidx.work.runtime.ktx) // Slice 3 local reminders

    testImplementation(libs.junit)
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.retrofit)
    testImplementation(libs.androidx.work.testing)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.ext.junit)
}
