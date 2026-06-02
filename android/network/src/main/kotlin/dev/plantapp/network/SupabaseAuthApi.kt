package dev.plantapp.network

import retrofit2.http.Body
import retrofit2.http.POST

/** Supabase GoTrue email-OTP endpoints. `requestOtp` emails a 6-digit code (204/200 with no
 *  meaningful body); `verifyOtp` exchanges the code for a session (access token). The anon
 *  `apikey` header is injected by [SupabaseAuthApiFactory]. */
interface SupabaseAuthApi {
    @POST("auth/v1/otp")
    suspend fun requestOtp(@Body body: OtpRequest): retrofit2.Response<Unit>

    @POST("auth/v1/verify")
    suspend fun verifyOtp(@Body body: VerifyOtpRequest): SessionResponse
}
