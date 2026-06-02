package dev.plantapp.network

import retrofit2.converter.kotlinx.serialization.asConverterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import kotlinx.serialization.json.Json
import retrofit2.Retrofit

/** Builds [SupabaseAuthApi] over Retrofit + OkHttp + the kotlinx.serialization converter (D-02).
 *  [authBaseUrl] is the Supabase project URL (e.g. the local stack `http://127.0.0.1:54321/`);
 *  [anonKey] is the public anon apikey GoTrue requires for unauthenticated otp/verify. Both are
 *  supplied by the caller — nothing is hard-coded. The logging interceptor logs request lines +
 *  status only (no bodies/PII) — emails and OTP codes must never be logged. */
object SupabaseAuthApiFactory {
    val json: Json = Json {
        encodeDefaults = true
        explicitNulls = false
        ignoreUnknownKeys = true
    }

    fun create(authBaseUrl: String, anonKey: String): SupabaseAuthApi {
        val apiKeyInterceptor = Interceptor { chain ->
            val request = chain.request().newBuilder()
                .header("apikey", anonKey)
                .build()
            chain.proceed(request)
        }

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC // request line + status only; no bodies/PII
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(apiKeyInterceptor)
            .addInterceptor(logging)
            .build()

        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(authBaseUrl)
            .client(client)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(SupabaseAuthApi::class.java)
    }
}
