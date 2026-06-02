package dev.plantapp.android

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dagger.hilt.android.AndroidEntryPoint
import dev.plantapp.data.settings.SettingsStore
import dev.plantapp.designsystem.PlantAppTheme
import dev.plantapp.feature.inventory.AddPlantScreen
import dev.plantapp.feature.inventory.AddPlantViewModel
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
        setContent { PlantAppTheme { PlantAppNavHost(startDestination = start) } }
    }
}

private object Routes {
    const val SIGN_IN = "signin"
    const val LIST = "plants"
    const val ADD = "plants/add"
    const val DETAIL = "plants/{plantId}"
    fun detail(plantId: String) = "plants/$plantId"
}

@Composable
fun PlantAppNavHost(startDestination: String = Routes.LIST) {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = startDestination) {
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
            AddPlantScreen(
                profiles = profiles,
                gardenSpaces = gardenSpaces,
                containers = containers,
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
