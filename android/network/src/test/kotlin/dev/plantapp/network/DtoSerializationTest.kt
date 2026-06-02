package dev.plantapp.network

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.encodeToString

/** kotlinx.serialization round-trip (encode → decode → equality) for each Slice 1 DTO. */
class DtoSerializationTest {
    private val json = TestSupport.json

    @Test
    fun gardenSpaceRoundTrips() {
        val encoded = json.encodeToString(DtoFixtures.gardenSpace)
        assertEquals(DtoFixtures.gardenSpace, json.decodeFromString<GardenSpaceDto>(encoded))
    }

    @Test
    fun containerRoundTrips() {
        val encoded = json.encodeToString(DtoFixtures.container)
        assertEquals(DtoFixtures.container, json.decodeFromString<ContainerDto>(encoded))
    }

    @Test
    fun plantInstanceRoundTrips() {
        val encoded = json.encodeToString(DtoFixtures.plantInstance)
        assertEquals(DtoFixtures.plantInstance, json.decodeFromString<PlantInstanceDto>(encoded))
    }

    @Test
    fun careTaskRoundTrips() {
        val encoded = json.encodeToString(DtoFixtures.careTask)
        assertEquals(DtoFixtures.careTask, json.decodeFromString<CareTaskDto>(encoded))
    }

    @Test
    fun addPlantResponseRoundTrips() {
        val resp = AddPlantResponse(plant = DtoFixtures.plantInstance, task = DtoFixtures.careTask)
        val encoded = json.encodeToString(resp)
        assertEquals(resp, json.decodeFromString<AddPlantResponse>(encoded))
    }

    @Test
    fun addPlantRequestOmitsAbsentOptionalFields() {
        val req = AddPlantRequest(
            profileId = "solanum-lycopersicum",
            containerId = DtoFixtures.CONTAINER_ID,
            gardenSpaceId = DtoFixtures.SPACE_ID,
            growthStage = "vegetative",
        )
        val encoded = json.encodeToString(req)
        // explicitNulls=false + encodeDefaults=false → absent optionals are not emitted.
        assert(!encoded.contains("nickname")) { "expected nickname omitted, got: $encoded" }
        assert(!encoded.contains("lastWateredAt")) { "expected lastWateredAt omitted, got: $encoded" }
    }
}
