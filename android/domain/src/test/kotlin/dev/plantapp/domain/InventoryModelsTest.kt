package dev.plantapp.domain

import dev.plantapp.domain.model.AddPlantResult
import dev.plantapp.domain.model.CareTask
import dev.plantapp.domain.model.NewPlant
import dev.plantapp.domain.model.Plant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** Light checks on the pure-Kotlin domain models (no Android, no serialization). */
class InventoryModelsTest {
    @Test
    fun newPlantDefaultsOptionalFieldsToNull() {
        val np = NewPlant(
            profileId = "solanum-lycopersicum",
            containerId = "c",
            gardenSpaceId = "g",
            growthStage = "vegetative",
        )
        assertNull(np.nickname)
        assertNull(np.cultivar)
        assertNull(np.placement)
        assertNull(np.lastWateredAt)
    }

    @Test
    fun addPlantResultHoldsPlantAndTask() {
        val plant = Plant(
            id = "p",
            profileId = "solanum-lycopersicum",
            containerId = "c",
            gardenSpaceId = "g",
            growthStage = "vegetative",
        )
        val task = CareTask(
            id = "t",
            kind = "water",
            dueAt = "2026-05-28T07:00:00.000Z",
            priority = "normal",
            rationale = "r",
            engineVersion = "0.1.0",
            inputsHash = "a".repeat(64),
            status = "pending",
        )
        val result = AddPlantResult(plant = plant, task = task)
        assertEquals(plant, result.plant)
        assertEquals("water", result.task.kind)
    }
}
