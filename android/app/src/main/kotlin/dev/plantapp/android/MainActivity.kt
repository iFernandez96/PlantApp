package dev.plantapp.android

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Balcony
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dagger.hilt.android.AndroidEntryPoint
import dev.plantapp.data.settings.SettingsStore
import dev.plantapp.designsystem.PlantAppBackground
import dev.plantapp.designsystem.PlantAppTheme
import dev.plantapp.feature.inventory.AddPlantViewModel
import dev.plantapp.feature.inventory.addplant.AddPlantWizard
import dev.plantapp.feature.inventory.NotificationPermission
import dev.plantapp.feature.inventory.PlantDetailScreen
import dev.plantapp.feature.inventory.PlantDetailViewModel
import dev.plantapp.feature.inventory.PlantListScreen
import dev.plantapp.feature.inventory.PlantListViewModel
import dev.plantapp.feature.inventory.SignInScreen
import dev.plantapp.feature.inventory.SignInViewModel
import androidx.compose.runtime.LaunchedEffect
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var settings: SettingsStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val start = if (settings.tokenBlocking() != null) Routes.LIST else Routes.SIGN_IN
        setContent { PlantAppTheme { PlantAppBackground { PlantAppNavHost(startDestination = start) } } }
    }
}

private object Routes {
    const val SIGN_IN = "signin"
    const val LIST = "plants"
    const val ADD = "plants/add"
    const val DETAIL = "plants/{plantId}"
    const val TODAY = "today"
    const val SPACES = "spaces"
    fun detail(plantId: String) = "plants/$plantId"
}

private data class BottomTab(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val testTag: String,
)

private val BOTTOM_TABS = listOf(
    BottomTab(Routes.TODAY, "Today", Icons.Filled.WbSunny, "tab_today"),
    BottomTab(Routes.LIST, "My Garden", Icons.Filled.LocalFlorist, "tab_garden"),
    BottomTab(Routes.SPACES, "Spaces", Icons.Filled.Balcony, "tab_spaces"),
)

@Composable
fun PlantAppNavHost(startDestination: String = Routes.LIST) {
    val nav = rememberNavController()
    val backStackEntry by nav.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    Scaffold(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        bottomBar = {
            // Hearth shell: the bar shows only on the three top-level tabs, never on sign-in,
            // the add-plant wizard, or plant detail.
            if (currentRoute != null && BOTTOM_TABS.any { it.route == currentRoute }) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    for (tab in BOTTOM_TABS) {
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = {
                                nav.navigate(tab.route) {
                                    launchSingleTop = true
                                    restoreState = true
                                    popUpTo(Routes.LIST) { saveState = true }
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                            alwaysShowLabel = true,
                            modifier = Modifier.testTag(tab.testTag),
                        )
                    }
                }
            }
        },
    ) { padding ->
        PlantAppNavGraph(nav, startDestination, Modifier.padding(padding))
    }
}

@Composable
private fun PlantAppNavGraph(
    nav: androidx.navigation.NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier,
) {
    NavHost(navController = nav, startDestination = startDestination, modifier = modifier) {
        composable(Routes.TODAY) { TodayPlaceholderScreen() }
        composable(Routes.SPACES) { SpacesPlaceholderScreen() }
        composable(Routes.SIGN_IN) {
            val vm: SignInViewModel = hiltViewModel()
            val state by vm.state.collectAsState()
            SignInScreen(
                codeSent = state.codeSent,
                error = state.error,
                onRequestCode = vm::requestCode,
                onVerify = { email, code ->
                    vm.verify(email, code) {
                        nav.navigate(Routes.LIST) {
                            popUpTo(Routes.SIGN_IN) { inclusive = true }
                        }
                    }
                },
            )
        }
        composable(Routes.LIST) {
            val vm: PlantListViewModel = hiltViewModel()
            val state by vm.state.collectAsState()
            // Slice 3: ask for POST_NOTIFICATIONS once (Android 13+) so scheduled local reminders
            // can show. The Worker also guards on the live permission, so the result is a no-op here.
            val context = LocalContext.current
            val launcher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission(),
            ) {}
            LaunchedEffect(Unit) {
                // Re-runs on every visit (restoreState keeps the VM alive across tabs), so the
                // list is fresh whenever the user looks at it; the VM refreshes quietly.
                vm.refresh()
                val granted = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) == PackageManager.PERMISSION_GRANTED
                if (NotificationPermission.shouldRequest(Build.VERSION.SDK_INT, granted)) {
                    launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
            PlantListScreen(
                state = state,
                onAddClick = { nav.navigate(Routes.ADD) },
                onPlantClick = { id -> nav.navigate(Routes.detail(id)) },
            )
        }
        composable(Routes.ADD) {
            val vm: AddPlantViewModel = hiltViewModel()
            val profiles by vm.profiles.collectAsState()
            val gardenSpaces by vm.gardenSpaces.collectAsState()
            val containers by vm.containers.collectAsState()
            val error by vm.error.collectAsState()
            AddPlantWizard(
                profiles = profiles,
                gardenSpaces = gardenSpaces,
                containers = containers,
                error = error,
                onCreateGardenSpace = vm::createGardenSpace,
                onCreateContainer = vm::createContainer,
                onSubmit = { form ->
                    vm.submit(form) { newId ->
                        nav.navigate(Routes.detail(newId)) {
                            popUpTo(Routes.LIST)
                        }
                    }
                },
                onCancel = { nav.popBackStack() },
            )
        }
        composable(
            route = Routes.DETAIL,
            arguments = listOf(navArgument("plantId") { type = NavType.StringType }),
        ) { entry ->
            val plantId = entry.arguments?.getString("plantId").orEmpty()
            val vm: PlantDetailViewModel = hiltViewModel()
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
