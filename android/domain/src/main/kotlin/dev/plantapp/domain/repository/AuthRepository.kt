package dev.plantapp.domain.repository

/** Sign-in port (D-05: Supabase email-OTP). Implemented in :data over the Supabase GoTrue API;
 *  the access token is persisted on successful verification and read by the network auth
 *  interceptor. Android holds no other auth logic. */
interface AuthRepository {
    /** Ask Supabase to email a one-time code to [email]. */
    suspend fun requestOtp(email: String)

    /** Verify [code] for [email]; on success the access token is persisted. */
    suspend fun verifyOtp(email: String, code: String)
}
