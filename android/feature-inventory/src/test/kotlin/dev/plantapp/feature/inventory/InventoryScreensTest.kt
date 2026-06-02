package dev.plantapp.feature.inventory

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import dev.plantapp.domain.model.CareTask
import dev.plantapp.domain.model.GardenSpace
import dev.plantapp.domain.model.Plant
import dev.plantapp.domain.model.PlantProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Slice 1 Compose UI tests #21–#24, run on the JVM via Robolectric (no emulator). The
 *  stateless screens are driven directly with fixture state + callback spies. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w411dp-h2000dp") // tall window so the whole form lays out on-screen
class InventoryScreensTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val plant = Plant(
        id = "00000000-0000-4000-8000-000000000001",
        profileId = "solanum-lycopersicum",
        containerId = "00000000-0000-4000-8000-000000000002",
        gardenSpaceId = "00000000-0000-4000-8000-000000000003",
        growthStage = "vegetative",
        nickname = "Pasi",
        lastWateredAt = "2026-05-26T07:00:00.000Z",
    )
    private val task = CareTask(
        id = "00000000-0000-4000-8000-0000000000aa",
        kind = "water",
        dueAt = "2026-05-28T07:00:00.000Z",
        priority = "normal",
        rationale = "Tomato: base interval 2d adjusted by container factor 1; baseline ...",
        engineVersion = "0.1.0",
        inputsHash = "a".repeat(64),
        status = "pending",
    )
    private val profiles = listOf(
        PlantProfile("solanum-lycopersicum", "Solanum lycopersicum", listOf("Tomato"), "fruit"),
        PlantProfile("ocimum-basilicum", "Ocimum basilicum", listOf("Basil"), "herb"),
    )
    private val gardenSpaces = listOf(
        GardenSpace("00000000-0000-4000-8000-000000000003", "West Balcony", "balcony"),
        GardenSpace("00000000-0000-4000-8000-000000000004", "East Patio", "patio"),
    )

    @Test
    fun `#21 list shows empty state when there are no plants`() {
        composeRule.setContent { PlantListScreen(state = PlantListUiState.Empty) }
        composeRule.onNodeWithTag(InventoryTestTags.EMPTY_STATE).assertIsDisplayed()
    }

    @Test
    fun `#22 add-plant submits the form when required fields are filled`() {
        var submitted: AddPlantForm? = null
        composeRule.setContent {
            AddPlantScreen(profiles = profiles, gardenSpaces = gardenSpaces, onCreateGardenSpace = { _, _ -> }, onSubmit = { submitted = it })
        }

        // pick the profile from the dropdown
        composeRule.onNodeWithTag(InventoryTestTags.FIELD_PROFILE_SELECTOR).performClick()
        composeRule.onNodeWithText("Tomato").performClick()
        // pick the garden space from its selector
        composeRule.onNodeWithTag(InventoryTestTags.FIELD_GARDEN_SPACE_SELECTOR).performClick()
        composeRule.onNodeWithText("West Balcony").performClick()

        composeRule.onNodeWithTag(InventoryTestTags.FIELD_CONTAINER_ID).performTextInput(plant.containerId)
        composeRule.onNodeWithTag(InventoryTestTags.FIELD_GROWTH_STAGE).performTextInput("vegetative")
        composeRule.onNodeWithTag(InventoryTestTags.SUBMIT_BUTTON).performScrollTo().performClick()

        assertEquals("solanum-lycopersicum", submitted?.profileId)
        assertEquals(plant.containerId, submitted?.containerId)
        assertEquals("00000000-0000-4000-8000-000000000003", submitted?.gardenSpaceId)
    }

    @Test
    fun `add-plant profile dropdown lists catalog profiles`() {
        composeRule.setContent {
            AddPlantScreen(profiles = profiles, gardenSpaces = gardenSpaces, onCreateGardenSpace = { _, _ -> }, onSubmit = {})
        }
        composeRule.onNodeWithTag(InventoryTestTags.FIELD_PROFILE_SELECTOR).performClick()
        composeRule.onNodeWithText("Tomato").assertIsDisplayed()
        composeRule.onNodeWithText("Basil").assertIsDisplayed()
    }

    @Test
    fun `garden-space selector lists existing spaces`() {
        composeRule.setContent {
            AddPlantScreen(profiles = profiles, gardenSpaces = gardenSpaces, onCreateGardenSpace = { _, _ -> }, onSubmit = {})
        }
        composeRule.onNodeWithTag(InventoryTestTags.FIELD_GARDEN_SPACE_SELECTOR).performClick()
        // names may appear in both the anchor (auto-selected) and the menu, so match >=1
        composeRule.onAllNodesWithText("West Balcony").onFirst().assertIsDisplayed()
        composeRule.onAllNodesWithText("East Patio").onFirst().assertIsDisplayed()
    }

    @Test
    fun `garden-space create path invokes callback`() {
        var created: Pair<String, String>? = null
        composeRule.setContent {
            AddPlantScreen(profiles = profiles, gardenSpaces = gardenSpaces, onCreateGardenSpace = { n, k -> created = n to k }, onSubmit = {})
        }
        composeRule.onNodeWithTag(InventoryTestTags.FIELD_GARDEN_SPACE_SELECTOR).performClick()
        composeRule.onNodeWithTag(InventoryTestTags.GARDEN_SPACE_CREATE_ITEM).performClick()
        composeRule.onNodeWithTag(InventoryTestTags.FIELD_NEW_GARDEN_SPACE_NAME).performTextInput("North Ledge")
        composeRule.onNodeWithTag(InventoryTestTags.FIELD_NEW_GARDEN_SPACE_KIND).performTextInput("window-ledge")
        composeRule.onNodeWithTag(InventoryTestTags.GARDEN_SPACE_CREATE_BUTTON).performScrollTo().performClick()
        assertEquals("North Ledge" to "window-ledge", created)
    }

    @Test
    fun `#23 detail shows the water task with rationale, engineVersion badge, and dueAt`() {
        composeRule.setContent {
            PlantDetailScreen(state = PlantDetailUiState.Content(plant = plant, task = task))
        }
        composeRule.onNodeWithTag(InventoryTestTags.TASK_KIND).assertIsDisplayed()
        composeRule.onNodeWithText("water", substring = true, ignoreCase = true).assertIsDisplayed()
        composeRule.onNodeWithTag(InventoryTestTags.TASK_RATIONALE).assertIsDisplayed()
        composeRule.onNodeWithTag(InventoryTestTags.ENGINE_VERSION_BADGE).assertIsDisplayed()
        composeRule.onNodeWithText("0.1.0", substring = true).assertIsDisplayed()
        composeRule.onNodeWithTag(InventoryTestTags.TASK_DUE_AT).assertIsDisplayed()
    }

    @Test
    fun `#24 add-plant without a container shows error and does not submit`() {
        var submitted: AddPlantForm? = null
        composeRule.setContent {
            AddPlantScreen(profiles = profiles, gardenSpaces = gardenSpaces, onCreateGardenSpace = { _, _ -> }, onSubmit = { submitted = it })
        }

        composeRule.onNodeWithTag(InventoryTestTags.FIELD_PROFILE_SELECTOR).performClick()
        composeRule.onNodeWithText("Tomato").performClick()
        composeRule.onNodeWithTag(InventoryTestTags.FIELD_GARDEN_SPACE_SELECTOR).performClick()
        composeRule.onNodeWithText("West Balcony").performClick()
        composeRule.onNodeWithTag(InventoryTestTags.FIELD_GROWTH_STAGE).performTextInput("vegetative")
        // containerId intentionally left blank
        composeRule.onNodeWithTag(InventoryTestTags.SUBMIT_BUTTON).performScrollTo().performClick()

        composeRule.onNodeWithTag(InventoryTestTags.CONTAINER_ERROR).assertIsDisplayed()
        assertNull(submitted)
    }
}
