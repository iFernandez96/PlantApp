package dev.plantapp.network

import retrofit2.converter.kotlinx.serialization.asConverterFactory
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import kotlinx.serialization.json.Json
import retrofit2.Retrofit

/** Supplies the current Supabase access token for the Authorization header. */
fun interface AuthTokenProvider {
    fun currentToken(): String?
}

/** Refreshes the session on a 401; returns the NEW access token, or null if refresh failed. */
fun interface SessionRefresher {
    fun refreshSession(): String?
}

/** Builds the [PlantAppApi] over Retrofit + OkHttp + the kotlinx.serialization
 *  converter (D-02). [baseUrl] is configurable; the bearer token is injected per-request
 *  from [tokenProvider]. The logging interceptor logs request lines only (no bodies),
 *  so no PII or payloads are written to logs. */
object PlantAppApiFactory {
    val json: Json = Json {
        encodeDefaults = false
        explicitNulls = false
        ignoreUnknownKeys = true
    }

    fun create(
        baseUrl: String,
        tokenProvider: AuthTokenProvider,
        sessionRefresher: SessionRefresher? = null,
    ): PlantAppApi {
        val authInterceptor = Interceptor { chain ->
            val builder = chain.request().newBuilder()
            tokenProvider.currentToken()?.let { token ->
                builder.header("Authorization", "Bearer $token")
            }
            chain.proceed(builder.build())
        }

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC // request line + status only; no bodies/PII
        }

        val clientBuilder = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
        if (sessionRefresher != null) {
            clientBuilder.authenticator(
                Authenticator { _, response ->
                    // One refresh attempt per call: a prior response means we already retried.
                    if (response.priorResponse != null) return@Authenticator null
                    val newToken = sessionRefresher.refreshSession() ?: return@Authenticator null
                    response.request.newBuilder()
                        .header("Authorization", "Bearer $newToken")
                        .build()
                },
            )
        }
        val client = clientBuilder.build()

        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(PlantAppApi::class.java)
    }
}
