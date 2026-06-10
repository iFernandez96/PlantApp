package dev.plantapp.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import dev.plantapp.data.repository.SessionRefreshManager
import dev.plantapp.data.settings.SettingsStore
import dev.plantapp.network.OtpRequest
import dev.plantapp.network.RefreshTokenRequest
import dev.plantapp.network.SessionResponse
import dev.plantapp.network.SupabaseAuthApi
import dev.plantapp.network.VerifyOtpRequest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import retrofit2.Response

/** SessionRefreshManager over a fake GoTrue api and a real SettingsStore on an in-memory
 *  DataStore (no disk, no Robolectric): success persists the new pair; failure or a missing
 *  stored refresh token clears the session and returns null. */
class SessionRefreshManagerTest {
    /** In-memory DataStore<Preferences> so the real SettingsStore key logic is exercised. */
    private class InMemoryDataStore : DataStore<Preferences> {
        private val state = MutableStateFlow(emptyPreferences())
        override val data: Flow<Preferences> = state
        override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
            val next = transform(state.value)
            state.value = next
            return next
        }
    }

    /** refreshToken-only fake; otp/verify are out of scope here. */
    private class FakeRefreshApi : SupabaseAuthApi {
        var refreshCalls = 0
        var lastRefresh: RefreshTokenRequest? = null
        var result: SessionResponse? = SessionResponse(accessToken = "a2", refreshToken = "r2")

        override suspend fun requestOtp(body: OtpRequest): Response<Unit> =
            throw UnsupportedOperationException("not used")

        override suspend fun verifyOtp(body: VerifyOtpRequest): SessionResponse =
            throw UnsupportedOperationException("not used")

        override suspend fun refreshToken(body: RefreshTokenRequest): SessionResponse {
            refreshCalls++
            lastRefresh = body
            return result ?: throw RuntimeException("refresh failed")
        }
    }

    private fun store() = SettingsStore(InMemoryDataStore())

    @Test
    fun `success persists the new token pair and returns the new access token`() {
        val settings = store()
        runBlocking { settings.setSession("a1", "r1") }
        val api = FakeRefreshApi()

        val result = SessionRefreshManager(api, settings).refreshSessionBlocking()

        assertEquals("a2", result)
        assertEquals("r1", api.lastRefresh?.refreshToken)
        assertEquals("a2", settings.tokenBlocking())
        assertEquals("r2", settings.refreshTokenBlocking())
    }

    @Test
    fun `api failure clears the session and returns null`() {
        val settings = store()
        runBlocking { settings.setSession("a1", "r1") }
        val api = FakeRefreshApi().apply { result = null }

        val result = SessionRefreshManager(api, settings).refreshSessionBlocking()

        assertNull(result)
        assertEquals(1, api.refreshCalls)
        assertNull(settings.tokenBlocking())
        assertNull(settings.refreshTokenBlocking())
    }

    @Test
    fun `no stored refresh token never calls the api, clears the session, returns null`() {
        val settings = store()
        runBlocking { settings.setSession("a1", null) }
        val api = FakeRefreshApi()

        val result = SessionRefreshManager(api, settings).refreshSessionBlocking()

        assertNull(result)
        assertEquals(0, api.refreshCalls)
        assertNull(settings.tokenBlocking())
        assertNull(settings.refreshTokenBlocking())
    }
}
