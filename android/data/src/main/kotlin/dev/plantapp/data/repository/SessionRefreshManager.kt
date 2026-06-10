package dev.plantapp.data.repository

import dev.plantapp.data.settings.SettingsStore
import dev.plantapp.network.RefreshTokenRequest
import dev.plantapp.network.SupabaseAuthApi
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

/** Exchanges the stored refresh token for a fresh session (blocking — called from OkHttp's
 *  authenticator thread). Success: persists the new token pair, returns the new access
 *  token. Failure or no stored refresh token: clears the session (next launch lands on
 *  sign-in) and returns null. */
@Singleton
class SessionRefreshManager @Inject constructor(
    private val authApi: SupabaseAuthApi,
    private val settings: SettingsStore,
) {
    fun refreshSessionBlocking(): String? = runBlocking {
        val stored = settings.refreshTokenBlocking()
            ?: run {
                settings.setSession(null, null)
                return@runBlocking null
            }
        try {
            val s = authApi.refreshToken(RefreshTokenRequest(stored))
            settings.setSession(s.accessToken, s.refreshToken)
            s.accessToken
        } catch (e: Exception) {
            settings.setSession(null, null)
            null
        }
    }
}
