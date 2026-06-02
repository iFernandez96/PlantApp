package dev.plantapp.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

/** Persists the API base URL + Supabase auth token in a Preferences DataStore.
 *  No hard-coded secrets; the token is written at sign-in (a later slice). */
@Singleton
class SettingsStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    private val baseUrlKey = stringPreferencesKey("api_base_url")
    private val tokenKey = stringPreferencesKey("auth_token")

    suspend fun setBaseUrl(url: String) {
        dataStore.edit { it[baseUrlKey] = url }
    }

    suspend fun setToken(token: String?) {
        dataStore.edit {
            if (token == null) it.remove(tokenKey) else it[tokenKey] = token
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
}
