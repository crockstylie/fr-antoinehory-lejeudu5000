package fr.antoinehory.lejeudu5000.ui.feature_home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Home screen.
 * Manages the state of whether a game is currently in progress.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    // TODO: Injecter un GameStateRepository quand tu l'auras créé
    // private val gameStateRepository: GameStateRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        checkGameInProgress()
    }

    /**
     * Vérifie s'il y a une partie en cours
     */
    private fun checkGameInProgress() {
        viewModelScope.launch {
            // TODO: Implémenter la logique pour vérifier si une partie est sauvegardée
            // val hasGameInProgress = gameStateRepository.hasGameInProgress()
            val hasGameInProgress = false // Temporaire
            
            _uiState.value = _uiState.value.copy(
                hasGameInProgress = hasGameInProgress,
                isLoading = false
            )
        }
    }

    /**
     * Marque qu'une nouvelle partie a commencé
     */
    fun onNewGameStarted() {
        _uiState.value = _uiState.value.copy(hasGameInProgress = true)
    }

    /**
     * Marque qu'une partie s'est terminée
     */
    fun onGameEnded() {
        _uiState.value = _uiState.value.copy(hasGameInProgress = false)
    }
}