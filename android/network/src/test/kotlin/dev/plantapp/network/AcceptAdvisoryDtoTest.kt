package dev.plantapp.network

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.serialization.encodeToString

/** AcceptAdvisoryRequest body for POST /plants/:id/advisories/accept: encodes its `kind` and
 *  round-trips. Uses the contract encoder (TestSupport.json). */
class AcceptAdvisoryDtoTest {
    private val json = TestSupport.json

    @Test
    fun acceptAdvisoryRequestEncodesKindAndRoundTrips() {
        val req = AcceptAdvisoryRequest(kind = "container-size")
        val encoded = json.encodeToString(req)
        assertTrue(encoded.contains("\"kind\":\"container-size\""), "expected kind field, got: $encoded")
        assertEquals(req, json.decodeFromString<AcceptAdvisoryRequest>(encoded))
    }
}
