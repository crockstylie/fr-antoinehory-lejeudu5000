package fr.antoinehory.lejeudu5000.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import fr.antoinehory.lejeudu5000.ui.feature_home.HomeScreen
import fr.antoinehory.lejeudu5000.ui.feature_game.GameScreen // Import décommenté
import fr.antoinehory.lejeudu5000.ui.feature_settings.SettingsScreen
import fr.antoinehory.lejeudu5000.ui.feature_info.InfoScreen

/**
 * Defines the navigation routes for the application.
 * Using a sealed class or object for routes is a common practice for type safety
 * and to centralize route definitions.
 */
object AppDestinations {
    const val HOME_ROUTE = "home"
    const val GAME_ROUTE = "game"
    const val SETTINGS_ROUTE = "settings"
    const val INFO_ROUTE = "info"
}

/**
 * Composable function that sets up the main navigation graph for the application.
 * It uses a NavHost to define all possible navigation paths.
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = AppDestinations.HOME_ROUTE) {
        composable(AppDestinations.HOME_ROUTE) {
            HomeScreen(navController = navController)
        }
        composable(AppDestinations.GAME_ROUTE) {
            GameScreen(navController = navController) // Appel à GameScreen décommenté et configuré
        }
        composable(AppDestinations.SETTINGS_ROUTE) {
            SettingsScreen(navController = navController)
        }
        composable(AppDestinations.INFO_ROUTE) {
            InfoScreen(navController = navController)
        }
    }
}
