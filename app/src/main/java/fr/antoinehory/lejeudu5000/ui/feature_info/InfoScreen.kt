package fr.antoinehory.lejeudu5000.ui.feature_info

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
import fr.antoinehory.lejeudu5000.R // Import R class
import fr.antoinehory.lejeudu5000.ui.common.CommonTopAppBar
import fr.antoinehory.lejeudu5000.ui.theme.LeJeuDu5000Theme

/**
 * Composable function for the Info screen.
 * It displays information about the application and the developer.
 *
 * @param navController The NavController used for navigation.
 */
@OptIn(ExperimentalMaterial3Api::class) // Required for Scaffold and TopAppBar
@Composable
fun InfoScreen(navController: NavController) {
    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = stringResource(id = R.string.info_screen_title),
                navController = navController
                // showBackButton = true // Uncomment if a back button is preferred over home
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding), // Apply padding from the Scaffold
            contentAlignment = Alignment.Center
        ) {
            Text(stringResource(id = R.string.info_screen_placeholder_content))
            // TODO: Implement actual info content (app version, developer info, links, etc.). (P2 - Medium Priority)
        }
    }
}

/**
 * Preview function for the InfoScreen.
 * It provides a design-time view of the InfoScreen.
 */
@Preview(showBackground = true)
@Composable
fun InfoScreenPreview() {
    LeJeuDu5000Theme {
        InfoScreen(navController = rememberNavController())
    }
}

