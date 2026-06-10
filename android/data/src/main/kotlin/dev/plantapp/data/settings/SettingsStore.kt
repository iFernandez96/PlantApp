package dev.plantapp.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

/** Narrow seam for writing the auth token, so the auth repository can be unit-tested without a
 *  real DataStore. Implemented by [SettingsStore]. */
interface TokenWriter {
    suspend fun setToken(token: String?)

    /** Persists the full session pair. Default keeps access-token-only fakes compiling. */
    suspend fun setSession(accessToken: String?, refreshToken: String?) {
        setToken(accessToken)
    }
}

/** Persists the API base URL + Supabase auth token in a Preferences DataStore.
 *  No hard-coded secrets; the token is written at sign-in (a later slice). */
@Singleton
class SettingsStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : TokenWriter {
    private val baseUrlKey = stringPreferencesKey("api_base_url")
    private val tokenKey = stringPreferencesKey("auth_token")
    private val refreshTokenKey = stringPreferencesKey("refresh_token")

    suspend fun setBaseUrl(url: String) {
        dataStore.edit { it[baseUrlKey] = url }
    }

    override suspend fun setToken(token: String?) {
        dataStore.edit {
            if (token == null) it.remove(tokenKey) else it[tokenKey] = token
        }
    }

    override suspend fun setSession(accessToken: String?, refreshToken: String?) {
        dataStore.edit {
            if (accessToken == null) it.remove(tokenKey) else it[tokenKey] = accessToken
            if (refreshToken == null) it.remove(refreshTokenKey) else it[refreshTokenKey] = refreshToken
        }
    }

    /** Current base URL, or [default] if unset. Blocking read for use from non-suspend
     *  contexts (e.g. building the Retrofit client). */
    fun baseUrlBlocking(default: String): String = runBlocking {
        dataStore.data.first()[baseUrlKey] ?: default
    }

    /** Current auth token, or null. Blocking read for the OkHttp auth interceptor. */
    fun tokenBlocking(): String? = runBlocking {
        dataStore.data.first()[tokenKey]
    }

    /** Current refresh token, or null. Blocking read for the OkHttp authenticator path. */
    fun refreshTokenBlocking(): String? = runBlocking {
        dataStore.data.first()[refreshTokenKey]
    }
}
