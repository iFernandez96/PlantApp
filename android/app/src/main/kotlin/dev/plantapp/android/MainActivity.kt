package dev.plantapp.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dagger.hilt.android.AndroidEntryPoint
import dev.plantapp.designsystem.PlantAppTheme
import dev.plantapp.feature.inventory.AddPlantScreen
import dev.plantapp.feature.inventory.AddPlantViewModel
import dev.plantapp.feature.inventory.PlantDetailScreen
import dev.plantapp.feature.inventory.PlantDetailViewModel
import dev.plantapp.feature.inventory.PlantListScreen
import dev.plantapp.feature.inventory.PlantListViewModel
import androidx.compose.runtime.LaunchedEffect

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { PlantAppTheme { PlantAppNavHost() } }
    }
}

private object Routes {
    const val LIST = "plants"
    const val ADD = "plants/add"
    const val DETAIL = "plants/{plantId}"
    fun detail(plantId: String) = "plants/$plantId"
}

@Composable
fun PlantAppNavHost() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = Routes.LIST) {
        composable(Routes.LIST) {
            val vm: PlantListViewModel = hiltViewModel()
            val state by vm.state.collectAsState()
            PlantListScreen(
                state = state,
                onAddClick = { nav.navigate(Routes.ADD) },
                onPlantClick = { id -> nav.navigate(Routes.detail(id)) },
            )
        }
        composable(Routes.ADD) {
            val vm: AddPlantViewModel = hiltViewModel()
            AddPlantScreen(
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
            PlantDetailScreen(state = state, onBack = { nav.popBackStack() })
        }
    }
}
