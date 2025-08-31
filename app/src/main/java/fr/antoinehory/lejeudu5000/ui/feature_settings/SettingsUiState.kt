package fr.antoinehory.lejeudu5000.ui.feature_settings

import fr.antoinehory.lejeudu5000.domain.model.GameSettings

/**
 * Represents the UI state for the Settings screen.
 *
 * @property gameSettings The current game settings to be displayed and modified.
 *                       Defaults to a new [GameSettings] instance with default values.
 * @property isLoading Indicates if the settings are currently being loaded.
 *                     Defaults to `false`.
 * @property error An optional error message to be displayed if loading or saving settings fails.
 *                 Defaults to `null`.
 */
data class SettingsUiState(
    val gameSettings: GameSettings = GameSettings(),
    val isLoading: Boolean = false,
    val error: String? = null
    // TODO: Consider adding specific states for dropdowns if they need to show "loading" or complex states.
    // For now, gameSettings will directly drive the UI.
)