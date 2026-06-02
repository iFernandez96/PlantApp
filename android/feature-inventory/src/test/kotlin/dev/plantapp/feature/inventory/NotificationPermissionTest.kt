package dev.plantapp.feature.inventory

import org.junit.Assert.assertEquals
import org.junit.Test

/** POST_NOTIFICATIONS request decision: only on API 33+ when not already granted. */
class NotificationPermissionTest {
    @Test
    fun belowApi33IsNeverRequested() {
        assertEquals(false, NotificationPermission.shouldRequest(sdkInt = 32, granted = false))
        assertEquals(false, NotificationPermission.shouldRequest(sdkInt = 32, granted = true))
    }

    @Test
    fun api33NotGrantedIsRequested() {
        assertEquals(true, NotificationPermission.shouldRequest(sdkInt = 33, granted = false))
    }

    @Test
    fun api33GrantedIsNotRequested() {
        assertEquals(false, NotificationPermission.shouldRequest(sdkInt = 33, granted = true))
    }

    @Test
    fun api34NotGrantedIsRequested() {
        assertEquals(true, NotificationPermission.shouldRequest(sdkInt = 34, granted = false))
    }
}
