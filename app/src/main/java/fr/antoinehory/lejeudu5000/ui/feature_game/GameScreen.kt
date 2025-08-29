package fr.antoinehory.lejeudu5000.ui.feature_game

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
// import androidx.hilt.navigation.compose.hiltViewModel // TODO: P0 - Uncomment when GameViewModel is integrated with Hilt
import fr.antoinehory.lejeudu5000.R // Import R class
import fr.antoinehory.lejeudu5000.ui.common.CommonTopAppBar
import fr.antoinehory.lejeudu5000.ui.theme.LeJeuDu5000Theme

/**
 * Composable function for the Game screen.
 * This is where the main gameplay takes place.
 *
 * @param navController The NavController used for navigation.
 * // @param viewModel The GameViewModel, typically injected by Hilt. // TODO: P0 - Add when GameViewModel is ready and integrated
 */
@OptIn(ExperimentalMaterial3Api::class) // Required for Scaffold and TopAppBar
@Composable
fun GameScreen(
    navController: NavController
    // viewModel: GameViewModel = hiltViewModel() // TODO: P0 - Uncomment and integrate when GameViewModel is ready
) {
    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = stringResource(id = R.string.game_screen_title),
                navController = navController,
                showBackButton = true // A back button is usually preferred on a game screen to navigate to Home or previous screen.
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding), // Apply padding from the Scaffold
            contentAlignment = Alignment.Center
        ) {
            Text(stringResource(id = R.string.game_screen_placeholder_content))
            // TODO: P0 - Implement the core game UI (dice display, score tracking, player actions) according to game rules. This is a top priority.
        }
    }
}

/**
 * Preview function for the GameScreen.
 * It provides a design-time view of the GameScreen.
 */
@Preview(showBackground = true)
@Composable
fun GameScreenPreview() {
    LeJeuDu5000Theme {
        // Using a dummy NavController for preview purposes.
        GameScreen(navController = rememberNavController())
    }
}

