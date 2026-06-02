package dev.plantapp.network

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.serialization.encodeToString

/** Supabase GoTrue email-OTP DTOs: snake_case wire names + round-trip + lenient decode.
 *  Uses the production auth encoder (SupabaseAuthApiFactory.json, encodeDefaults=true) so the
 *  GoTrue-required default fields (create_user, type) are actually sent. */
class AuthDtoTest {
    private val json = SupabaseAuthApiFactory.json

    @Test
    fun otpRequestEncodesSnakeCaseCreateUserAndRoundTrips() {
        val req = OtpRequest(email = "owner@example.test")
        val encoded = json.encodeToString(req)
        assertTrue(encoded.contains("\"create_user\""), "expected snake_case create_user, got: $encoded")
        assertTrue(!encoded.contains("createUser"), "should not emit camelCase createUser: $encoded")
        assertEquals(req, json.decodeFromString<OtpRequest>(encoded))
    }

    @Test
    fun verifyOtpRequestEncodesFieldsAndRoundTrips() {
        val req = VerifyOtpRequest(email = "owner@example.test", token = "123456")
        val encoded = json.encodeToString(req)
        assertTrue(encoded.contains("\"email\"") && encoded.contains("\"token\"") && encoded.contains("\"type\""))
        assertEquals(req, json.decodeFromString<VerifyOtpRequest>(encoded))
    }

    @Test
    fun sessionResponseDecodesGoTrueJsonIgnoringUnknownKeys() {
        val body = """
            {"access_token":"abc.def.ghi","token_type":"bearer","expires_in":3600,
             "refresh_token":"r","user":{"id":"x"}}
        """.trimIndent()
        val session = json.decodeFromString<SessionResponse>(body)
        assertEquals("abc.def.ghi", session.accessToken)
        assertEquals("bearer", session.tokenType)
        assertEquals(3600L, session.expiresIn)
        assertEquals("r", session.refreshToken)
    }
}
