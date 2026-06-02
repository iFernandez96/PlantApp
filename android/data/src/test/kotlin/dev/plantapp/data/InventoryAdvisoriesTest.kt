package dev.plantapp.data

import dev.plantapp.data.repository.InventoryRepositoryImpl
import dev.plantapp.domain.repository.InventoryRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

/** Slice 2: getAdvisories maps the fake API's AdvisoryDto list → domain Advisory list. */
class InventoryAdvisoriesTest {
    private fun repo(api: FakePlantAppApi): InventoryRepository = InventoryRepositoryImpl(api)

    @Test
    fun getAdvisoriesMapsDtoToDomain() = runTest {
        val api = FakePlantAppApi()
        val advisories = repo(api).getAdvisories(api.plant.id)
        assertEquals(1, advisories.size)
        assertEquals("container-size", advisories[0].kind)
        assertEquals("high", advisories[0].severity)
        assertEquals(api.advisory.title, advisories[0].title)
        assertEquals(api.advisory.message, advisories[0].message)
    }
}
