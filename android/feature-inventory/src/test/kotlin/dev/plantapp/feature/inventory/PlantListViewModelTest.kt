package dev.plantapp.feature.inventory

import dev.plantapp.domain.model.Plant
import dev.plantapp.domain.repository.InventoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** PlantListViewModel refresh behavior: spinner only when there is nothing useful on screen
 *  (first load / after error); a refresh over visible content reloads quietly. */
@OptIn(ExperimentalCoroutinesApi::class)
class PlantListViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /** FakeInventoryRepository with a mutable plant list + switchable failure. */
    private class MutablePlantsRepo(
        val base: FakeInventoryRepository = FakeInventoryRepository(),
    ) : InventoryRepository by base {
        var plants: List<Plant> = listOf(base.plant)
        var failure: Exception? = null
        override suspend fun getPlants(): List<Plant> {
            failure?.let { throw it }
            return plants
        }
    }

    @Test
    fun firstLoadGoesLoadingThenContent() = runTest(dispatcher) {
        val repo = MutablePlantsRepo()
        val vm = PlantListViewModel(repo, reminderSync(repo))
        assertTrue("spinner expected on first load", vm.state.value is PlantListUiState.Loading)
        advanceUntilIdle()
        val state = vm.state.value
        assertTrue("content expected after load", state is PlantListUiState.Content)
        assertEquals(1, (state as PlantListUiState.Content).plants.size)
    }

    @Test
    fun refreshOverVisibleContentNeverShowsTheSpinner() = runTest(dispatcher) {
        val repo = MutablePlantsRepo()
        val vm = PlantListViewModel(repo, reminderSync(repo))
        advanceUntilIdle()
        assertTrue(vm.state.value is PlantListUiState.Content)

        repo.plants = listOf(repo.base.plant, repo.base.plant.copy(id = "00000000-0000-4000-8000-000000000099"))
        vm.refresh()
        // The only synchronous state write in refresh() is the Loading gate — so if the gate is
        // wrong, Loading is observable right here, before the scheduler runs the reload.
        assertFalse(
            "refresh over content must not flash the spinner",
            vm.state.value is PlantListUiState.Loading,
        )
        assertTrue("old content stays visible while reloading", vm.state.value is PlantListUiState.Content)
        advanceUntilIdle()
        val state = vm.state.value
        assertTrue(state is PlantListUiState.Content)
        assertEquals(2, (state as PlantListUiState.Content).plants.size)
    }

    @Test
    fun refreshAfterErrorShowsLoadingThenResult() = runTest(dispatcher) {
        val repo = MutablePlantsRepo().apply { failure = RuntimeException("boom") }
        val vm = PlantListViewModel(repo, reminderSync(repo))
        advanceUntilIdle()
        assertTrue(vm.state.value is PlantListUiState.Error)

        repo.failure = null
        vm.refresh()
        assertTrue(
            "spinner allowed when there is nothing useful on screen",
            vm.state.value is PlantListUiState.Loading,
        )
        advanceUntilIdle()
        assertTrue(vm.state.value is PlantListUiState.Content)
    }
}
