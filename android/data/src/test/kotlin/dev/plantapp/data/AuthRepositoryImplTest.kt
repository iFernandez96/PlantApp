package dev.plantapp.data

import dev.plantapp.data.repository.AuthRepositoryImpl
import dev.plantapp.data.settings.TokenWriter
import dev.plantapp.domain.repository.AuthRepository
import dev.plantapp.network.OtpRequest
import dev.plantapp.network.SessionResponse
import dev.plantapp.network.SupabaseAuthApi
import dev.plantapp.network.VerifyOtpRequest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import retrofit2.Response

/** AuthRepositoryImpl over a fake SupabaseAuthApi (no HTTP). Verifies request delegation and
 *  that a successful verify persists the returned access token via TokenWriter. */
class AuthRepositoryImplTest {
    /** Records the last otp/verify request; returns success for otp and a canned session. */
    private class FakeSupabaseAuthApi : SupabaseAuthApi {
        var lastOtp: OtpRequest? = null
        var lastVerify: VerifyOtpRequest? = null

        override suspend fun requestOtp(body: OtpRequest): Response<Unit> {
            lastOtp = body
            return Response.success(Unit)
        }

        override suspend fun verifyOtp(body: VerifyOtpRequest): SessionResponse {
            lastVerify = body
            return SessionResponse(accessToken = "token-123")
        }
    }

    private class FakeTokenWriter : TokenWriter {
        var last: String? = "<unset>"
        override suspend fun setToken(token: String?) {
            last = token
        }
    }

    private fun repo(api: SupabaseAuthApi, writer: TokenWriter): AuthRepository =
        AuthRepositoryImpl(api, writer)

    @Test
    fun `requestOtp delegates to the api with the email`() = runTest {
        val api = FakeSupabaseAuthApi()
        repo(api, FakeTokenWriter()).requestOtp("a@b.test")
        assertEquals("a@b.test", api.lastOtp?.email)
    }

    @Test
    fun `verifyOtp persists the returned access token`() = runTest {
        val api = FakeSupabaseAuthApi()
        val writer = FakeTokenWriter()
        repo(api, writer).verifyOtp("a@b.test", "123456")
        assertEquals("a@b.test", api.lastVerify?.email)
        assertEquals("123456", api.lastVerify?.token)
        assertEquals("email", api.lastVerify?.type)
        assertEquals("token-123", writer.last)
    }
}
