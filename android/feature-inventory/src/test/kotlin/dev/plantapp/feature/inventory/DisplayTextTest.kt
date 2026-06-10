package dev.plantapp.feature.inventory

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
}
