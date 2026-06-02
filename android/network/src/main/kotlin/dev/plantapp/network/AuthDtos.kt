package dev.plantapp.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Supabase GoTrue email-OTP auth DTOs. Wire names are snake_case (@SerialName); the module
// Json uses ignoreUnknownKeys=true, so GoTrue's extra response fields (user, etc.) are ignored.

@Serializable
data class OtpRequest(
    val email: String,
    @SerialName("create_user") val createUser: Boolean = true,
)

@Serializable
data class VerifyOtpRequest(
    val email: String,
    val token: String,
    val type: String = "email",
)

@Serializable
data class SessionResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("token_type") val tokenType: String? = null,
    @SerialName("expires_in") val expiresIn: Long? = null,
    @SerialName("refresh_token") val refreshToken: String? = null,
)
