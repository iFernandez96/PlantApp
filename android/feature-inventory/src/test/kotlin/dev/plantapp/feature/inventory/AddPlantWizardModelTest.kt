package dev.plantapp.feature.inventory

import dev.plantapp.feature.inventory.addplant.AddPlantWizardModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** The pure add-plant wizard model: pot sizes (label → litres), location presets (label → kind),
 *  and the category → emoji map. No Android. */
class AddPlantWizardModelTest {

    @Test
    fun potSizesAreTheSixExpectedOptionsInOrderWithPositiveVolumes() {
        val sizes = AddPlantWizardModel.POT_SIZES
        assertEquals(
            listOf("4-inch pot", "6-inch pot", "1-gallon pot", "5-gallon bucket", "Window box", "Raised bed / in-ground"),
            sizes.map { it.label },
        )
        assertEquals(
            listOf(0.5, 1.5, 4.0, 19.0, 6.0, 75.0),
            sizes.map { it.volumeLiters },
        )
        assertTrue("all volumes positive", sizes.all { it.volumeLiters > 0.0 })
    }

    @Test
    fun locationPresetsMapLabelsToBackendKinds() {
        val byLabel = AddPlantWizardModel.LOCATION_PRESETS.associate { it.label to it.kind }
        assertEquals(
            mapOf(
                "Windowsill" to "window-ledge",
                "Balcony" to "balcony",
                "Backyard" to "other",
                "Indoors" to "indoor-room",
            ),
            byLabel,
        )
    }

    @Test
    fun `location preset kinds are accepted by the garden_spaces DB constraint`() {
        val allowed = setOf(
            "balcony", "patio", "window-ledge", "indoor-room",
            "vertical-rack-zone", "hanging-zone", "grow-light-shelf", "other",
        ) // mirrors supabase/migrations/0002_slice1_garden_spaces.sql
        AddPlantWizardModel.LOCATION_PRESETS.forEach { preset ->
            check(preset.kind in allowed) { "preset '${preset.label}' sends invalid kind '${preset.kind}'" }
        }
    }

    @Test
    fun hiddenDefaultsAreSetForTheEngine() {
        assertEquals("plastic", AddPlantWizardModel.DEFAULT_MATERIAL)
        assertEquals("good", AddPlantWizardModel.DEFAULT_DRAINAGE)
        assertEquals("seedling", AddPlantWizardModel.DEFAULT_GROWTH_STAGE)
    }
}
