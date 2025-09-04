package fr.antoinehory.lejeudu5000.ui.feature_home

/**
 * UI State pour l'écran d'accueil
 */
data class HomeUiState(
    val hasGameInProgress: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null
)