package dev.plantapp.network

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/** Slice 2: AdvisoryDto round-trips and validates against advisory.schema.json (D-06). */
class AdvisoryDtoTest {
    private val json = TestSupport.json

    private val advisory = AdvisoryDto(
        kind = "container-size",
        severity = "high",
        plantInstanceId = "00000000-0000-4000-8000-000000000001",
        profileId = "passiflora-edulis",
        title = "Container is smaller than recommended",
        message = "Passion fruit prefers at least 95 L (ideal 95-190 L); this container is 19 L.",
        details = JsonObject(
            mapOf(
                "recommendedMinLiters" to JsonPrimitive(95),
                "idealMinLiters" to JsonPrimitive(95),
                "idealMaxLiters" to JsonPrimitive(190),
                "currentVolumeLiters" to JsonPrimitive(19),
            ),
        ),
        createdAt = "2026-05-26T07:00:00.000Z",
    )

    @Test
    fun roundTrips() {
        val encoded = json.encodeToString(advisory)
        assertEquals(advisory, json.decodeFromString<AdvisoryDto>(encoded))
    }

    @Test
    fun conformsToSchema() {
        val errors = TestSupport.validateAgainstSchema("advisory", json.encodeToString(advisory))
        assertTrue(errors.isEmpty(), "AdvisoryDto schema errors: $errors")
    }
}
