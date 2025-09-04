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
     * ✅ Amélioration : Gestion d'erreurs robuste
     */
    private fun loadSettings() {
        viewModelScope.launch {
            settingsRepository.getGameSettings()
                .onStart { 
                    _uiState.update { it.copy(isLoading = true, error = null) }
                }
                .catch { exception ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            error = "Erreur lors du chargement des paramètres: ${exception.message}"
                        ) 
                    }
                }
                .collect { gameSettings ->
                    _uiState.update { 
                        it.copy(
                            gameSettings = gameSettings,
                            isLoading = false,
                            error = null
                        ) 
                    }
                }
        }
    }

    /**
     * ✅ Fonction utilitaire pour éviter la duplication de code
     */
    private fun updateSetting(updateAction: suspend () -> Unit) {
        viewModelScope.launch(dispatchers.io) {
            try {
                updateAction()
                // Effacer l'erreur précédente si l'opération réussit
                _uiState.update { it.copy(error = null) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Erreur lors de la sauvegarde: ${e.message}") 
                }
            }
        }
    }

    /**
     * Efface le message d'erreur
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    // ✅ Toutes les fonctions update utilisent maintenant la fonction utilitaire
    fun onOpeningScoreThresholdChanged(threshold: Int) = updateSetting {
        settingsRepository.updateOpeningScoreThreshold(threshold)
    }

    fun onVictoryScoreChanged(score: Int) = updateSetting {
        settingsRepository.updateVictoryScore(score)
    }

    fun onMustWinOnExactScoreChanged(mustBeExact: Boolean) = updateSetting {
        settingsRepository.updateMustWinOnExactScore(mustBeExact)
    }

    fun onCancelOpponentScoreOnMatchChanged(cancelOnMatch: Boolean) = updateSetting {
        settingsRepository.updateCancelOpponentScoreOnMatch(cancelOnMatch)
    }

    fun onAllowFiftyPointScoresChanged(allowFifty: Boolean) = updateSetting {
        settingsRepository.updateAllowFiftyPointScores(allowFifty)
    }

    fun onUseThreeLivesRuleChanged(useThreeLives: Boolean) = updateSetting {
        settingsRepository.updateUseThreeLivesRule(useThreeLives)
    }

    fun onAllowStealOnPassChanged(allowSteal: Boolean) = updateSetting {
        settingsRepository.updateAllowStealOnPass(allowSteal)
    }
}