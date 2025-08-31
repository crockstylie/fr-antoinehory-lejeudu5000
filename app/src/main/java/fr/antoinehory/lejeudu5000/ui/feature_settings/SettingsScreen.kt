package fr.antoinehory.lejeudu5000.ui.feature_settings

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
// import androidx.compose.foundation.layout.widthIn // Pas utilisé
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider // Correction: Divider -> HorizontalDivider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType // Ajout pour menuAnchor
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue // Assurez-vous que cet import est présent
import androidx.compose.runtime.mutableStateOf // Assurez-vous que cet import est présent
import androidx.compose.runtime.remember // Assurez-vous que cet import est présent
import androidx.compose.runtime.setValue // Assurez-vous que cet import est présent
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext // Ajout pour getResourceEntryName
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import fr.antoinehory.lejeudu5000.R
import fr.antoinehory.lejeudu5000.data.repository.SettingsRepository
import fr.antoinehory.lejeudu5000.domain.model.GameSettings
import fr.antoinehory.lejeudu5000.ui.common.CommonTopAppBar
import fr.antoinehory.lejeudu5000.ui.theme.LeJeuDu5000Theme
import fr.antoinehory.lejeudu5000.util.CoroutineDispatchers
// Supprimé: import io.mockk.every // Ne doit pas être dans le code de production
// Supprimé: import io.mockk.mockk // Ne doit pas être dans le code de production
import kotlinx.coroutines.CoroutineDispatcher // Utilisé dans FakeCoroutineDispatchers
import kotlinx.coroutines.Dispatchers // Utilisé dans FakeCoroutineDispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.Flow
import java.util.Locale

/**
 * Composable function for the Settings screen.
 * It displays various game settings options that the user can configure.
 * The screen observes [SettingsUiState] from the [SettingsViewModel] to render
 * the current settings and handles user interactions to update these settings.
 *
 * @param navController The NavController used for navigation, typically for navigating back.
 * @param viewModel The [SettingsViewModel] instance, injected by Hilt, responsible for
 *                  managing the settings' state and logic.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = stringResource(id = R.string.settings_screen_title),
                navController = navController
            )
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.testTag("loadingIndicator")
                    )
                }
            }
            uiState.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.settings_error_loading_generic_message, uiState.error ?: ""),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            else -> {
                SettingsContent(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                    gameSettings = uiState.gameSettings,
                    onOpeningThresholdChanged = viewModel::onOpeningScoreThresholdChanged,
                    onVictoryScoreChanged = viewModel::onVictoryScoreChanged,
                    onMustWinOnExactScoreChanged = viewModel::onMustWinOnExactScoreChanged,
                    onCancelOpponentScoreChanged = viewModel::onCancelOpponentScoreOnMatchChanged,
                    onAllowFiftyPointScoresChanged = viewModel::onAllowFiftyPointScoresChanged,
                    onUseThreeLivesRuleChanged = viewModel::onUseThreeLivesRuleChanged,
                    onAllowStealOnPassChanged = viewModel::onAllowStealOnPassChanged
                )
            }
        }
    }
}

/**
 * Creates a standardized test tag string from a string resource ID.
 * Example: R.string.settings_exact_victory_label -> "setting_item_exact_victory_label"
 *
 * @param labelResId The string resource ID for the setting's label.
 * @return A string suitable for use as a test tag.
 */
@Composable
private fun createItemTestTag(@StringRes labelResId: Int): String {
    val resourceName = LocalContext.current.resources.getResourceEntryName(labelResId)
    return "setting_item_${resourceName.replace("settings_", "").lowercase(Locale.ROOT)}"
}


/**
 * Displays the main content of the Settings screen, listing all configurable game options.
 * This composable is responsible for laying out the various setting items.
 *
 * @param modifier Modifier for this composable.
 * @param gameSettings The current [GameSettings] to display and allow modification for.
 * @param onOpeningThresholdChanged Callback invoked when the opening score threshold selection changes.
 * @param onVictoryScoreChanged Callback invoked when the victory score selection changes.
 * @param onMustWinOnExactScoreChanged Callback invoked when the 'must win on exact score' switch changes.
 * @param onCancelOpponentScoreChanged Callback invoked when the 'cancel opponent score on match' switch changes.
 * @param onAllowFiftyPointScoresChanged Callback invoked when the 'allow fifty point scores' switch changes.
 * @param onUseThreeLivesRuleChanged Callback invoked when the 'use three lives rule' switch changes.
 * @param onAllowStealOnPassChanged Callback invoked when the 'allow steal on pass' switch changes.
 */
@Composable
private fun SettingsContent(
    modifier: Modifier = Modifier,
    gameSettings: GameSettings,
    onOpeningThresholdChanged: (Int) -> Unit,
    onVictoryScoreChanged: (Int) -> Unit,
    onMustWinOnExactScoreChanged: (Boolean) -> Unit,
    onCancelOpponentScoreChanged: (Boolean) -> Unit,
    onAllowFiftyPointScoresChanged: (Boolean) -> Unit,
    onUseThreeLivesRuleChanged: (Boolean) -> Unit,
    onAllowStealOnPassChanged: (Boolean) -> Unit
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        SettingGroupTitle(title = stringResource(R.string.settings_group_general))
        DropdownSettingItem(
            label = stringResource(R.string.settings_opening_threshold_label),
            selectedValue = gameSettings.openingScoreThreshold,
            options = listOf(500, 750, 1000),
            onOptionSelected = onOpeningThresholdChanged
        )
        SettingDivider()
        DropdownSettingItem(
            label = stringResource(R.string.settings_victory_score_label),
            selectedValue = gameSettings.victoryScore,
            options = listOf(5000, 10000),
            onOptionSelected = onVictoryScoreChanged
        )
        SettingDivider()

        SwitchSettingItem(
            label = stringResource(R.string.settings_exact_victory_label),
            description = stringResource(R.string.settings_exact_victory_desc),
            checked = gameSettings.mustWinOnExactScore,
            onCheckedChange = onMustWinOnExactScoreChanged,
            testTag = createItemTestTag(R.string.settings_exact_victory_label)
        )

        Spacer(modifier = Modifier.height(16.dp))
        SettingGroupTitle(title = stringResource(R.string.settings_group_advanced_rules))

        SwitchSettingItem(
            label = stringResource(R.string.settings_cancel_score_label),
            description = stringResource(R.string.settings_cancel_score_desc),
            checked = gameSettings.cancelOpponentScoreOnMatch,
            onCheckedChange = onCancelOpponentScoreChanged,
            testTag = createItemTestTag(R.string.settings_cancel_score_label)
        )
        SettingDivider()
        SwitchSettingItem(
            label = stringResource(R.string.settings_allow_fifty_pts_label),
            description = stringResource(R.string.settings_allow_fifty_pts_desc),
            checked = gameSettings.allowFiftyPointScores,
            onCheckedChange = onAllowFiftyPointScoresChanged,
            testTag = createItemTestTag(R.string.settings_allow_fifty_pts_label)
        )
        SettingDivider()
        SwitchSettingItem(
            label = stringResource(R.string.settings_three_lives_rule_label),
            description = stringResource(R.string.settings_three_lives_rule_desc),
            checked = gameSettings.useThreeLivesRule,
            onCheckedChange = onUseThreeLivesRuleChanged,
            testTag = createItemTestTag(R.string.settings_three_lives_rule_label)
        )
        SettingDivider()
        SwitchSettingItem(
            label = stringResource(R.string.settings_steal_on_pass_label),
            description = stringResource(R.string.settings_steal_on_pass_desc),
            checked = gameSettings.allowStealOnPass,
            onCheckedChange = onAllowStealOnPassChanged,
            testTag = createItemTestTag(R.string.settings_steal_on_pass_label)
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * A composable for displaying a title for a group of settings.
 * @param title The title string to display.
 * @param modifier Modifier for this composable.
 */
@Composable
private fun SettingGroupTitle(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(top = 16.dp, bottom = 8.dp)
    )
}

/**
 * A reusable composable for a setting item that uses a Switch to toggle a boolean value.
 * @param label The primary text label for the setting.
 * @param description An optional secondary text for a more detailed description of the setting.
 * @param checked The current checked state of the Switch.
 * @param onCheckedChange A callback that is invoked when the Switch is toggled.
 * @param modifier Modifier for this composable.
 * @param enabled Controls the enabled state of the Switch.
 * @param testTag An optional test tag to uniquely identify this setting item for testing.
 */
@Composable
fun SwitchSettingItem(
    label: String,
    description: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    testTag: String? = null
) {
    val rowModifier = if (testTag != null) {
        modifier.testTag(testTag)
    } else {
        modifier
    }
    Row(
        modifier = rowModifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge
            )
            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}

/**
 * A reusable composable for a setting item that uses an ExposedDropdownMenuBox.
 * @param T The type of the options and selected value.
 * @param label The text label for the setting.
 * @param selectedValue The currently selected value.
 * @param options A list of available options for the dropdown.
 * @param optionToString A function to convert an option of type T to a String for display.
 * @param onOptionSelected A callback that is invoked when an option is selected.
 * @param modifier Modifier for this composable.
 * @param enabled Controls the enabled state of the dropdown.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> DropdownSettingItem(
    label: String,
    selectedValue: T,
    options: List<T>,
    optionToString: (T) -> String = { it.toString() },
    onOptionSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) } // CORRECTION: mutableStateof -> mutableStateOf

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(0.6f)
        )
        Spacer(Modifier.width(8.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { if (enabled) expanded = !expanded }, // L'opérateur '!' devrait fonctionner une fois 'expanded' correctement typé
            modifier = Modifier.weight(0.4f).wrapContentWidth(Alignment.End)
        ) {
            OutlinedTextField(
                value = optionToString(selectedValue),
                onValueChange = { /* Read-only */ },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable, enabled = enabled),
                enabled = enabled,
                textStyle = MaterialTheme.typography.bodyLarge,
                singleLine = true,
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(optionToString(option), style = MaterialTheme.typography.bodyLarge) },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

/**
 * A simple horizontal divider composable.
 * @param modifier Modifier for this composable.
 */
@Composable
private fun SettingDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier.padding(vertical = 8.dp),
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    )
}

// --- Previews ---

@Preview(showBackground = true, name = "Settings Screen Preview")
@Composable
fun SettingsScreenPreview() {
    LeJeuDu5000Theme {
        // Fake SettingsRepository for preview
        val fakeSettingsRepository = object : SettingsRepository {
            override fun getGameSettings(): Flow<GameSettings> = flowOf(GameSettings())
            override suspend fun updateOpeningScoreThreshold(threshold: Int) {}
            override suspend fun updateVictoryScore(score: Int) {}
            override suspend fun updateMustWinOnExactScore(mustBeExact: Boolean) {}
            override suspend fun updateCancelOpponentScoreOnMatch(cancelOnMatch: Boolean) {}
            override suspend fun updateAllowFiftyPointScores(allowFifty: Boolean) {}
            override suspend fun updateUseThreeLivesRule(useThreeLives: Boolean) {}
            override suspend fun updateAllowStealOnPass(allowSteal: Boolean) {}
        }

        // Fake CoroutineDispatchers for preview
        // CORRECTION: Utiliser une instance directe, ne pas tenter d'override des membres finaux
        val fakeDispatchers = CoroutineDispatchers()
        // Si votre classe CoroutineDispatchers prend des dispatchers en argument constructor pour faciliter les tests/previews:
        // val fakeDispatchers = CoroutineDispatchers(Dispatchers.Unconfined, Dispatchers.Unconfined, Dispatchers.Unconfined)


        val fakeViewModel = SettingsViewModel(fakeSettingsRepository, fakeDispatchers)

        SettingsScreen(
            navController = rememberNavController(),
            viewModel = fakeViewModel
        )
    }
}

@Preview(showBackground = true, name = "SwitchSettingItem Preview")
@Composable
fun SwitchSettingItemPreview() {
    LeJeuDu5000Theme {
        Column(Modifier.padding(16.dp)) {
            SwitchSettingItem(
                label = "Enable Awesome Feature",
                description = "This feature will make the app more awesome and great.",
                checked = true,
                onCheckedChange = {},
                testTag = "preview_switch_1"
            )
            SettingDivider()
            SwitchSettingItem(
                label = "Disable Boring Feature",
                checked = false,
                onCheckedChange = {},
                testTag = "preview_switch_2"
            )
        }
    }
}

@Preview(showBackground = true, name = "DropdownSettingItem Preview")
@Composable
fun DropdownSettingItemPreview() {
    LeJeuDu5000Theme {
        Column(Modifier.padding(16.dp)) {
            DropdownSettingItem(
                label = "Select Difficulty",
                selectedValue = "Normal",
                options = listOf("Easy", "Normal", "Hard"),
                onOptionSelected = {}
            )
            SettingDivider()
            DropdownSettingItem(
                label = "Select Points",
                selectedValue = 1000,
                options = listOf(500, 750, 1000, 1500),
                onOptionSelected = {}
            )
        }
    }
}
