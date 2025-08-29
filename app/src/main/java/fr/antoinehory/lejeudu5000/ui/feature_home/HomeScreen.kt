package fr.antoinehory.lejeudu5000.ui.feature_home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import fr.antoinehory.lejeudu5000.R // Import R class
import fr.antoinehory.lejeudu5000.ui.navigation.AppDestinations
import fr.antoinehory.lejeudu5000.ui.theme.LeJeuDu5000Theme

/**
 * Composable function for the Home screen.
 * It serves as the main entry point of the application, allowing users to
 * start a new game, resume a game, or navigate to settings and info screens.
 *
 * @param navController The NavController used for navigation.
 */
@Composable
fun HomeScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = { navController.navigate(AppDestinations.GAME_ROUTE) },
            modifier = Modifier.widthIn(min = 200.dp)
        ) {
            Text(stringResource(id = R.string.home_screen_button_new_game))
        }
        Spacer(modifier = Modifier.height(16.dp))

        // TODO: Implement logic to conditionally navigate or change text for "Reprendre la Partie"
        //  based on whether a game is in progress. (P2 - Low Priority)
        Button(
            onClick = { navController.navigate(AppDestinations.GAME_ROUTE) },
            modifier = Modifier.widthIn(min = 200.dp)
        ) {
            Text(stringResource(id = R.string.home_screen_button_resume_game))
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { navController.navigate(AppDestinations.SETTINGS_ROUTE) },
            modifier = Modifier.widthIn(min = 200.dp)
        ) {
            Text(stringResource(id = R.string.home_screen_button_settings))
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { navController.navigate(AppDestinations.INFO_ROUTE) },
            modifier = Modifier.widthIn(min = 200.dp)
        ) {
            Text(stringResource(id = R.string.home_screen_button_info))
        }
    }
}

/**
 * Preview function for the HomeScreen.
 * It provides a design-time view of the HomeScreen.
 */
@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    LeJeuDu5000Theme {
        HomeScreen(navController = rememberNavController())
    }
}
