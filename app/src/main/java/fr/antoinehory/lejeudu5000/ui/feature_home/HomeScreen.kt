package fr.antoinehory.lejeudu5000.ui.feature_home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import fr.antoinehory.lejeudu5000.R
import fr.antoinehory.lejeudu5000.ui.navigation.AppDestinations
import fr.antoinehory.lejeudu5000.ui.theme.LeJeuDu5000Theme

/**
 * Composable function for the Home screen.
 * ✅ Amélioration : Maintenant avec gestion de l'état "partie en cours"
 */
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel() // ✅ Injecté avec Hilt
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isLoading) {
        // Écran de chargement simple
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
        }
        return
    }

    HomeContent(
        uiState = uiState,
        onNewGame = { 
            viewModel.onNewGameStarted()
            navController.navigate(AppDestinations.GAME_ROUTE) 
        },
        onResumeGame = { 
            navController.navigate(AppDestinations.GAME_ROUTE) 
        },
        onSettings = { 
            navController.navigate(AppDestinations.SETTINGS_ROUTE) 
        },
        onInfo = { 
            navController.navigate(AppDestinations.INFO_ROUTE) 
        }
    )
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    onNewGame: () -> Unit,
    onResumeGame: () -> Unit,
    onSettings: () -> Unit,
    onInfo: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ✅ Bouton "Nouvelle partie" toujours disponible
        Button(
            onClick = onNewGame,
            modifier = Modifier.widthIn(min = 200.dp)
        ) {
            Text(stringResource(id = R.string.home_screen_button_new_game))
        }
        Spacer(modifier = Modifier.height(16.dp))

        // ✅ Bouton "Reprendre" seulement si une partie est en cours
        if (uiState.hasGameInProgress) {
            Button(
                onClick = onResumeGame,
                modifier = Modifier.widthIn(min = 200.dp)
            ) {
                Text(stringResource(id = R.string.home_screen_button_resume_game))
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(
            onClick = onSettings,
            modifier = Modifier.widthIn(min = 200.dp)
        ) {
            Text(stringResource(id = R.string.home_screen_button_settings))
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onInfo,
            modifier = Modifier.widthIn(min = 200.dp)
        ) {
            Text(stringResource(id = R.string.home_screen_button_info))
        }
    }
}

@Preview(showBackground = true, name = "Home Screen - No Game")
@Composable
fun HomeScreenNoGamePreview() {
    LeJeuDu5000Theme {
        HomeContent(
            uiState = HomeUiState(hasGameInProgress = false, isLoading = false),
            onNewGame = {},
            onResumeGame = {},
            onSettings = {},
            onInfo = {}
        )
    }
}

@Preview(showBackground = true, name = "Home Screen - Game In Progress")
@Composable
fun HomeScreenWithGamePreview() {
    LeJeuDu5000Theme {
        HomeContent(
            uiState = HomeUiState(hasGameInProgress = true, isLoading = false),
            onNewGame = {},
            onResumeGame = {},
            onSettings = {},
            onInfo = {}
        )
    }
}