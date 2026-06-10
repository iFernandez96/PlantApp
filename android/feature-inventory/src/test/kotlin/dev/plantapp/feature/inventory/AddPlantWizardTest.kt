package dev.plantapp.feature.inventory

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dev.plantapp.domain.model.Container
import dev.plantapp.domain.model.GardenSpace
import dev.plantapp.domain.model.PlantProfile
import dev.plantapp.feature.inventory.addplant.AddPlantWizard
import dev.plantapp.feature.inventory.addplant.potTileTagSuffix
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** The beginner add-plant wizard: 3 icon-tile steps + confirm. Driven via a stateful host that
 *  mirrors the VM (create callbacks append to the re-supplied lists), so id resolution by identity
 *  is exercised. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w411dp-h2000dp")
class AddPlantWizardTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val profiles = listOf(
        PlantProfile("solanum-lycopersicum", "Solanum lycopersicum", listOf("Tomato"), "fruit"),
        PlantProfile("ocimum-basilicum", "Ocimum basilicum", listOf("Basil"), "herb"),
        PlantProfile("fragaria-x-ananassa", "Fragaria x ananassa", listOf("Strawberry"), "berry"),
        PlantProfile("passiflora-edulis", "Passiflora edulis", listOf("Passion fruit"), "vine"),
        PlantProfile("physalis-philadelphica", "Physalis philadelphica", listOf("Tomatillo"), "fruit"),
    )

    private class Spy {
        var createSpaceCalls = 0
        var createContainerCalls = 0
        var lastContainerVolume: Double? = null
        var submitted: AddPlantForm? = null
    }

    /** Hosts the wizard with mutable lists; create callbacks append (like AddPlantViewModel). */
    @Composable
    private fun Host(spy: Spy) {
        var spaces by remember { mutableStateOf(emptyList<GardenSpace>()) }
        var containers by remember { mutableStateOf(emptyList<Container>()) }
        var seq by remember { mutableStateOf(0) }
        AddPlantWizard(
            profiles = profiles,
            gardenSpaces = spaces,
            containers = containers,
            onCreateGardenSpace = { name, kind ->
                spy.createSpaceCalls++
                seq++
                spaces = spaces + GardenSpace("space-$seq", name, kind)
            },
            onCreateContainer = { name, vol, material, drainage ->
                spy.createContainerCalls++
                spy.lastContainerVolume = vol
                seq++
                containers = containers + Container("container-$seq", name, vol, material, drainage)
            },
            onSubmit = { spy.submitted = it },
        )
    }

    @Test
    fun walkTomatoBalconyFiveGallonBucketThenAddSubmitsResolvedForm() {
        val spy = Spy()
        composeRule.setContent { Host(spy) }

        // Step 1: a species tile per profile.
        profiles.forEach { p ->
            composeRule.onNodeWithTag(InventoryTestTags.WIZARD_SPECIES_TILE_PREFIX + p.id).assertIsDisplayed()
        }
        composeRule.onNodeWithTag(InventoryTestTags.WIZARD_SPECIES_TILE_PREFIX + "solanum-lycopersicum").performClick()
        composeRule.waitForIdle()

        // Step 2: pick Balcony → creates a garden space.
        composeRule.onNodeWithTag(InventoryTestTags.WIZARD_LOCATION_TILE_PREFIX + "balcony").performClick()
        composeRule.waitForIdle()
        assertEquals(1, spy.createSpaceCalls)

        // Step 3: pick the 5-gallon bucket → creates a 19L container.
        val potTag = InventoryTestTags.WIZARD_POT_TILE_PREFIX + potTileTagSuffix("5-gallon bucket")
        composeRule.onNodeWithTag(potTag).performClick()
        composeRule.waitForIdle()
        assertEquals(1, spy.createContainerCalls)
        assertEquals(19.0, spy.lastContainerVolume!!, 0.0)

        // Confirm: Add.
        composeRule.onNodeWithTag(InventoryTestTags.WIZARD_ADD_BUTTON).performClick()
        composeRule.waitForIdle()

        val form = spy.submitted
        assertNotNull("onSubmit should have fired", form)
        assertEquals("solanum-lycopersicum", form!!.profileId)
        assertEquals("seedling", form.growthStage)
        assertEquals(null, form.lastWateredAt)
        assertTrue("containerId resolved", form.containerId.isNotBlank())
        assertTrue("gardenSpaceId resolved", form.gardenSpaceId.isNotBlank())
    }

    @Test
    fun confirmStepUsesTheNaturalLocationPhrase() {
        val spy = Spy()
        composeRule.setContent { Host(spy) }

        composeRule.onNodeWithTag(InventoryTestTags.WIZARD_SPECIES_TILE_PREFIX + "ocimum-basilicum").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(InventoryTestTags.WIZARD_LOCATION_TILE_PREFIX + "window-ledge").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(InventoryTestTags.WIZARD_POT_TILE_PREFIX + potTileTagSuffix("6-inch pot")).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Add your Basil on the windowsill?").assertIsDisplayed()
    }

    @Test
    fun errorCardShowsPlainCopyAndTheRawMessage() {
        composeRule.setContent {
            AddPlantWizard(
                profiles = profiles,
                gardenSpaces = emptyList(),
                containers = emptyList(),
                onCreateGardenSpace = { _, _ -> },
                onCreateContainer = { _, _, _, _ -> },
                onSubmit = {},
                error = "boom",
            )
        }
        composeRule.onNodeWithTag(InventoryTestTags.WIZARD_ERROR).assertIsDisplayed()
        composeRule.onNodeWithText("Something didn't work. Please try again.").assertIsDisplayed()
        composeRule.onNodeWithText("boom", substring = true).assertIsDisplayed()
    }

    @Test
    fun backThenReselectSameLocationDoesNotCreateDuplicate() {
        val spy = Spy()
        composeRule.setContent { Host(spy) }

        composeRule.onNodeWithTag(InventoryTestTags.WIZARD_SPECIES_TILE_PREFIX + "ocimum-basilicum").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(InventoryTestTags.WIZARD_LOCATION_TILE_PREFIX + "window-ledge").performClick()
        composeRule.waitForIdle()
        assertEquals(1, spy.createSpaceCalls)

        // Back to step 2, reselect the same location → must reuse, not create again.
        composeRule.onNodeWithTag(InventoryTestTags.WIZARD_BACK_BUTTON).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(InventoryTestTags.WIZARD_LOCATION_TILE_PREFIX + "window-ledge").performClick()
        composeRule.waitForIdle()
        assertEquals(1, spy.createSpaceCalls)
    }
}
