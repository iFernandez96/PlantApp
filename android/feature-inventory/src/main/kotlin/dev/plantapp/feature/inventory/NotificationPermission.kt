package dev.plantapp.feature.inventory

/** Pure, Android-free decision for the POST_NOTIFICATIONS runtime permission (JVM-testable). */
object NotificationPermission {
    /** Android 13+ (API 33) gates POST_NOTIFICATIONS behind a runtime grant; below that it is
     *  granted at install. Request only when on 33+ AND not already granted. */
    fun shouldRequest(sdkInt: Int, granted: Boolean): Boolean = sdkInt >= 33 && !granted
}
