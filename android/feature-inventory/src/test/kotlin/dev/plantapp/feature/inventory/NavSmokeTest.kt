package dev.plantapp.feature.inventory

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Backlog (2): a fast, deterministic JVM/Robolectric NavHost smoke. Drives a real
 *  NavController over the actual screens + ViewModels with the repositories faked — no Hilt,
 *  no emulator, no backend. Exercises the gated journey: sign-in → list → detail → accept. */
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w411dp-h2000dp")
class NavSmokeTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        // ViewModels launch on viewModelScope (Dispatchers.Main). Unconfined runs each launch
        // eagerly inline — the fakes never suspend, so state settles synchronously even when a
        // navigation creates the next screen's ViewModel mid-recomposition. (StandardTestDispatcher
        // would defer those launches past advanceUntilIdle, racing the NavHost recomposition.)
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /** Let Compose recompose with the state the (eagerly-run) ViewModel coroutines produced. */
    private fun idle() {
        composeRule.waitForIdle()
    }

    // mirrors MainActivity's nav graph — keep route strings/callbacks in sync if MainActivity changes
    @Composable
    private fun SmokeNavHost(repo: FakeInventoryRepository, auth: FakeAuthRepository) {
        val nav = rememberNavController()
        NavHost(navController = nav, startDestination = "signin") {
            composable("signin") {
                val vm = remember { SignInViewModel(auth) }
                val state by vm.state.collectAsState()
                SignInScreen(
                    codeSent = state.codeSent,
                    error = state.error,
                    onRequestCode = vm::requestCode,
                    onVerify = { email, code ->
                        vm.verify(email, code) {
                            nav.navigate("plants") { popUpTo("signin") { inclusive = true } }
                        }
                    },
                )
            }
            composable("plants") {
                val vm = remember { PlantListViewModel(repo) }
                val state by vm.state.collectAsState()
                PlantListScreen(
                    state = state,
                    onAddClick = { nav.navigate("plants/add") },
                    onPlantClick = { id -> nav.navigate("plants/$id") },
                )
            }
            composable("plants/add") {
                val vm = remember { AddPlantViewModel(repo) }
                val profiles by vm.profiles.collectAsState()
                val gardenSpaces by vm.gardenSpaces.collectAsState()
                val containers by vm.containers.collectAsState()
                AddPlantScreen(
                    profiles = profiles,
                    gardenSpaces = gardenSpaces,
                    containers = containers,
                    onCreateGardenSpace = vm::createGardenSpace,
                    onCreateContainer = vm::createContainer,
                    onSubmit = { form ->
                        vm.submit(form) { id -> nav.navigate("plants/$id") { popUpTo("plants") } }
                    },
                    onCancel = { nav.popBackStack() },
                )
            }
            composable(
                route = "plants/{plantId}",
                arguments = listOf(navArgument("plantId") { type = NavType.StringType }),
            ) { entry ->
                val plantId = entry.arguments?.getString("plantId").orEmpty()
                val vm = remember { PlantDetailViewModel(repo) }
                LaunchedEffect(plantId) { vm.loadFor(plantId) }
                val state by vm.state.collectAsState()
                PlantDetailScreen(
                    state = state,
                    onAccept = { kind -> vm.accept(plantId, kind) },
                    onBack = { nav.popBackStack() },
                )
            }
        }
    }

    private fun signIn() {
        composeRule.onNodeWithTag(InventoryTestTags.FIELD_SIGNIN_EMAIL).performTextInput("owner@example.test")
        composeRule.onNodeWithTag(InventoryTestTags.SIGNIN_SEND_CODE_BUTTON).performClick()
        idle()
        composeRule.onNodeWithTag(InventoryTestTags.FIELD_SIGNIN_CODE).performTextInput("123456")
        composeRule.onNodeWithTag(InventoryTestTags.SIGNIN_VERIFY_BUTTON).performClick()
        idle()
    }

    @Test
    fun `signed-out user signs in, sees the plant list`() {
        val repo = FakeInventoryRepository()
        val auth = FakeAuthRepository()
        composeRule.setContent { SmokeNavHost(repo, auth) }
        idle()

        // Starts on sign-in.
        composeRule.onNodeWithTag(InventoryTestTags.FIELD_SIGNIN_EMAIL).assertIsDisplayed()

        signIn()

        assertTrue("verifyOtp should have run", auth.verified)
        composeRule.onNodeWithTag(InventoryTestTags.PLANT_LIST).assertIsDisplayed()
        composeRule.onNodeWithText(repo.plant.nickname!!).assertIsDisplayed()
    }

    @Test
    fun `accept an advisory from the detail screen`() {
        val repo = FakeInventoryRepository()
        val auth = FakeAuthRepository()
        composeRule.setContent { SmokeNavHost(repo, auth) }
        idle()

        signIn()

        // List → tap the plant → detail.
        composeRule.onNodeWithText(repo.plant.nickname!!).performClick()
        idle()

        // The container-size advisory shows an Accept button; tapping it accepts via the repo.
        val acceptTag = InventoryTestTags.ADVISORY_ACCEPT_BUTTON_PREFIX + "container-size"
        composeRule.onNodeWithTag(acceptTag).assertIsDisplayed().performClick()
        idle()

        assertEquals(repo.plant.id to "container-size", repo.lastAccept)
    }
}
