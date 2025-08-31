package fr.antoinehory.lejeudu5000.ui.feature_settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.antoinehory.lejeudu5000.data.repository.SettingsRepository
import fr.antoinehory.lejeudu5000.domain.model.GameSettings
import fr.antoinehory.lejeudu5000.util.CoroutineDispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Settings screen.
 *
 * This ViewModel is responsible for fetching, displaying, and updating game settings
 * by interacting with the [SettingsRepository]. It exposes the settings UI state
 * to the Composable UI via a [StateFlow]. It handles asynchronous operations
 * using [CoroutineDispatchers] and updates the UI state accordingly, including
 * loading and error states.
 *
 * @param settingsRepository The repository for accessing and modifying game settings.
 * @param dispatchers Provides coroutine dispatchers for managing asynchronous operations,
 *                    ensuring that data operations are performed on appropriate threads.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val dispatchers: CoroutineDispatchers
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState(isLoading = true))
    /**
     * The UI state for the Settings screen, observed by the Composable.
     * It emits [SettingsUiState] which reflects the current game settings,
     * loading status, and any errors.
     */
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    /**
     * Loads the current game settings from the repository and updates the UI state.
     * This function is called upon ViewModel initialization. It collects the [GameSettings]
     * Flow from the repository, updating the [uiState] with loading, success, or error states.
     */
    private fun loadSettings() {
        viewModelScope.launch(dispatchers.io) { // Use IO dispatcher for data operations
            settingsRepository.getGameSettings()
                .onStart {
                    _uiState.update { currentState ->
                        currentState.copy(isLoading = true, error = null)
                    }
                }
                .catch { exception ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            // Ensure this string is internationalized (R.string.settings_error_loading)
                            error = "Failed to load settings: ${exception.localizedMessage}"
                        )
                    }
                    // Consider logging the exception for debugging purposes
                    // Log.e("SettingsViewModel", "Error loading settings", exception)
                }
                .collect { gameSettings ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            gameSettings = gameSettings,
                            error = null
                        )
                    }
                }
        }
    }

    /**
     * Updates the opening score threshold setting.
     * Launches a coroutine on the IO dispatcher to update the setting via the repository.
     *
     * @param threshold The new opening score threshold value (e.g., 500, 750, 1000).
     */
    fun onOpeningScoreThresholdChanged(threshold: Int) {
        viewModelScope.launch(dispatchers.io) {
            settingsRepository.updateOpeningScoreThreshold(threshold)
            // The uiState will automatically update due to the Flow collection in loadSettings
        }
    }

    /**
     * Updates the victory score setting.
     * Launches a coroutine on the IO dispatcher to update the setting via the repository.
     *
     * @param score The new victory score value (e.g., 5000, 10000).
     */
    fun onVictoryScoreChanged(score: Int) {
        viewModelScope.launch(dispatchers.io) {
            settingsRepository.updateVictoryScore(score)
        }
    }

    /**
     * Updates the 'must win on exact score' setting.
     * Launches a coroutine on the IO dispatcher to update the setting via the repository.
     *
     * @param mustBeExact True if victory must be on exact score, false otherwise.
     */
    fun onMustWinOnExactScoreChanged(mustBeExact: Boolean) {
        viewModelScope.launch(dispatchers.io) {
            settingsRepository.updateMustWinOnExactScore(mustBeExact)
        }
    }

    /**
     * Updates the 'cancel opponent score on match' setting.
     * Launches a coroutine on the IO dispatcher to update the setting via the repository.
     *
     * @param cancelOnMatch True if opponent's score should be cancelled on match, false otherwise.
     */
    fun onCancelOpponentScoreOnMatchChanged(cancelOnMatch: Boolean) {
        viewModelScope.launch(dispatchers.io) {
            settingsRepository.updateCancelOpponentScoreOnMatch(cancelOnMatch)
        }
    }

    /**
     * Updates the 'allow fifty point scores' setting.
     * Launches a coroutine on the IO dispatcher to update the setting via the repository.
     *
     * @param allowFifty True if scores ending in 50 (e.g., from a single '5' die) are allowed, false otherwise.
     */
    fun onAllowFiftyPointScoresChanged(allowFifty: Boolean) {
        viewModelScope.launch(dispatchers.io) {
            settingsRepository.updateAllowFiftyPointScores(allowFifty)
        }
    }

    /**
     * Updates the 'use three lives rule' setting.
     * Launches a coroutine on the IO dispatcher to update the setting via the repository.
     *
     * @param useThreeLives True if the three lives rule is active, false otherwise.
     */
    fun onUseThreeLivesRuleChanged(useThreeLives: Boolean) {
        viewModelScope.launch(dispatchers.io) {
            settingsRepository.updateUseThreeLivesRule(useThreeLives)
        }
    }

    /**
     * Updates the 'allow steal on pass' setting.
     * Launches a coroutine on the IO dispatcher to update the setting via the repository.
     *
     * @param allowSteal True if stealing score on pass is allowed, false otherwise.
     */
    fun onAllowStealOnPassChanged(allowSteal: Boolean) {
        viewModelScope.launch(dispatchers.io) {
            settingsRepository.updateAllowStealOnPass(allowSteal)
        }
    }
}
