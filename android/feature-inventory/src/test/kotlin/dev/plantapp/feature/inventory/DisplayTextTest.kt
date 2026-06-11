package dev.plantapp.feature.inventory

import dev.plantapp.domain.SessionExpiredException
import org.junit.Assert.assertEquals
import org.junit.Test

/** Plain-language label mappings (beginner copy is the behavior). Pure JUnit — no Robolectric. */
class DisplayTextTest {
    @Test
    fun waterKindIsCapitalised() {
        assertEquals("Water", DisplayText.taskKindLabel("water"))
    }

    @Test
    fun repotKindIsPlainLanguage() {
        assertEquals("Move to a bigger pot", DisplayText.taskKindLabel("repot"))
    }

    @Test
    fun unknownKindFallsBackToDeSluggedCapitalised() {
        assertEquals("Unknown kind", DisplayText.taskKindLabel("unknown-kind"))
    }

    @Test
    fun vegetativeStageIsPlainLanguage() {
        assertEquals("Growing well", DisplayText.growthStageLabel("vegetative"))
    }

    @Test
    fun unknownStageFallsBackToDeSluggedCapitalised() {
        assertEquals("Odd stage", DisplayText.growthStageLabel("odd-stage"))
    }

    @Test
    fun categoryLabelsAreBeginnerFriendly() {
        assertEquals("Houseplants", DisplayText.categoryLabel("houseplant"))
        assertEquals("Flowers", DisplayText.categoryLabel("ornamental"))
    }

    @Test
    fun unknownCategoryFallsBackToDeSluggedCapitalised() {
        assertEquals("Mystery kind", DisplayText.categoryLabel("mystery-kind"))
    }

    @Test
    fun friendlyErrorUsesTheFallbackAndNeverTheExceptionMessage() {
        val msg = DisplayText.friendlyError(RuntimeException("raw http://10.0.0.179"), "Friendly fallback.")
        assertEquals("Friendly fallback.", msg)
    }

    @Test
    fun friendlyErrorNamesSessionExpiry() {
        val msg = DisplayText.friendlyError(SessionExpiredException(), "Friendly fallback.")
        assertEquals("Your session ended. Please sign in again.", msg)
    }
}
