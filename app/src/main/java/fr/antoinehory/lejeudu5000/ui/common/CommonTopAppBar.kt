package fr.antoinehory.lejeudu5000.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
// import androidx.compose.ui.graphics.Color // Unused import
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import fr.antoinehory.lejeudu5000.R // Import R class for string resources
import fr.antoinehory.lejeudu5000.ui.navigation.AppDestinations

/**
 * A common TopAppBar used across multiple screens in the application.
 *
 * @param title The title to be displayed in the TopAppBar. This should be a string resource.
 * @param navController The NavController for handling navigation actions.
 * @param showBackButton Whether to display a back button (true) or a home button (false) as the navigation icon.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommonTopAppBar(
    title: String, // Titles are passed as parameters, will be handled in each screen
    navController: NavController,
    showBackButton: Boolean = false
) {
    TopAppBar(
        title = { Text(text = title) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
        navigationIcon = {
            if (showBackButton) {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(id = R.string.common_top_app_bar_cd_back)
                    )
                }
            } else {
                 IconButton(onClick = { navController.navigate(AppDestinations.HOME_ROUTE) { popUpTo(AppDestinations.HOME_ROUTE) { inclusive = true } } }) {
                    Icon(
                        imageVector = Icons.Filled.Home,
                        contentDescription = stringResource(id = R.string.common_top_app_bar_cd_home)
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = { navController.navigate(AppDestinations.SETTINGS_ROUTE) }) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = stringResource(id = R.string.common_top_app_bar_cd_settings)
                )
            }
            IconButton(onClick = { navController.navigate(AppDestinations.INFO_ROUTE) }) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = stringResource(id = R.string.common_top_app_bar_cd_info)
                )
            }
        }
    )
}
