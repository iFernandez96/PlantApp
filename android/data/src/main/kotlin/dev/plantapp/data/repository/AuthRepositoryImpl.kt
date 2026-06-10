package dev.plantapp.data.repository

import dev.plantapp.data.settings.TokenWriter
import dev.plantapp.domain.repository.AuthRepository
import dev.plantapp.network.OtpRequest
import dev.plantapp.network.SupabaseAuthApi
import dev.plantapp.network.VerifyOtpRequest
import javax.inject.Inject

/** AuthRepository over the Supabase GoTrue [SupabaseAuthApi]. On a successful verify the full
 *  session pair is persisted via [TokenWriter] (the network auth interceptor reads the access
 *  token back; the refresh token feeds the 401 auto-refresh). */
class AuthRepositoryImpl @Inject constructor(
    private val api: SupabaseAuthApi,
    private val tokenWriter: TokenWriter,
) : AuthRepository {

    override suspend fun requestOtp(email: String) {
        val r = api.requestOtp(OtpRequest(email = email))
        check(r.isSuccessful) { "requestOtp failed: HTTP ${r.code()}" }
    }

    override suspend fun verifyOtp(email: String, code: String) {
        val session = api.verifyOtp(VerifyOtpRequest(email = email, token = code))
        tokenWriter.setSession(session.accessToken, session.refreshToken)
    }
}
