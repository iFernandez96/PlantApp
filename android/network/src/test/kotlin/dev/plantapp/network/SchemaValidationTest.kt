package dev.plantapp.network

import kotlin.test.Test
import kotlin.test.assertTrue
import kotlinx.serialization.encodeToString

/** D-06: serialized DTOs must validate against the matching schema under shared-schemas
 *  (JSON Schema 2020-12, via networknt). Proves the Android DTOs honor the cross-boundary
 *  contract shared with the backend. */
class SchemaValidationTest {
    private val json = TestSupport.json

    @Test
    fun gardenSpaceDtoConformsToSchema() {
        val errors = TestSupport.validateAgainstSchema("garden-space", json.encodeToString(DtoFixtures.gardenSpace))
        assertTrue(errors.isEmpty(), "GardenSpaceDto schema errors: $errors")
    }

    @Test
    fun containerDtoConformsToSchema() {
        val errors = TestSupport.validateAgainstSchema("container", json.encodeToString(DtoFixtures.container))
        assertTrue(errors.isEmpty(), "ContainerDto schema errors: $errors")
    }

    @Test
    fun plantInstanceDtoConformsToSchema() {
        val errors = TestSupport.validateAgainstSchema("plant-instance", json.encodeToString(DtoFixtures.plantInstance))
        assertTrue(errors.isEmpty(), "PlantInstanceDto schema errors: $errors")
    }

    @Test
    fun careTaskDtoConformsToSchema() {
        val errors = TestSupport.validateAgainstSchema("care-task", json.encodeToString(DtoFixtures.careTask))
        assertTrue(errors.isEmpty(), "CareTaskDto schema errors: $errors")
    }
}
