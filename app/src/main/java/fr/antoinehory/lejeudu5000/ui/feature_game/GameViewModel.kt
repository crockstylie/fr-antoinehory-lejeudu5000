package fr.antoinehory.lejeudu5000.ui.feature_game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.antoinehory.lejeudu5000.data.repository.SettingsRepository
import fr.antoinehory.lejeudu5000.domain.GameEngine
import fr.antoinehory.lejeudu5000.domain.model.Dice
import fr.antoinehory.lejeudu5000.domain.model.GameSettings
import fr.antoinehory.lejeudu5000.domain.model.GameState
import fr.antoinehory.lejeudu5000.domain.model.Player
import fr.antoinehory.lejeudu5000.domain.model.TurnData
import fr.antoinehory.lejeudu5000.domain.usecase.FinalizeTurnUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * ViewModel for the game screen.
 * Manages the game state and handles game logic through [GameEngine] and UseCases.
 *
 * @property gameEngine The game engine instance responsible for dice rolling and score calculation.
 * @property settingsRepository Repository to access game settings.
 * @property finalizeTurnUseCase UseCase to handle the logic of finalizing a player's turn.
 */
@HiltViewModel
class GameViewModel @Inject constructor(
    private val gameEngine: GameEngine,
    private val settingsRepository: SettingsRepository,
    private val finalizeTurnUseCase: FinalizeTurnUseCase
) : ViewModel() {

    /**
     * Flow of current game settings.
     */
    private val gameSettings: StateFlow<GameSettings> = settingsRepository.getGameSettings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = GameSettings()
        )

    /**
     * Creates the initial domain [GameState].
     * This might be moved to a UseCase later for more complex initializations.
     * @return The initial [GameState].
     */
    private fun createInitialDomainGameState(): GameState {
        val initialPlayer = Player(id = UUID.randomUUID(), name = "Player 1", totalScore = 0)
        return GameState(
            players = listOf(initialPlayer),
            currentPlayerIndex = 0,
            turnData = TurnData(diceOnTable = List(5) { Dice(id = UUID.randomUUID(), value = 1) }),
            isGameOver = false
        )
    }

    private val _domainGameState = MutableStateFlow(createInitialDomainGameState())
    private val _uiState = MutableStateFlow(mapDomainToUiState(_domainGameState.value))

    /**
     * UI state flow for the game screen.
     */
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private fun hasPlayerOpened(player: Player, currentSettings: GameSettings): Boolean {
        return player.scoreHistory.any { it.recordedScore >= currentSettings.openingScoreThreshold } || player.totalScore >= currentSettings.openingScoreThreshold
    }

    /**
     * Maps the domain [GameState] to a [GameUiState] suitable for the UI.
     * This is typically called when a turn starts or the game state significantly changes.
     * @param domainState The current [GameState] from the domain layer.
     * @return The corresponding [GameUiState].
     */
    private fun mapDomainToUiState(domainState: GameState): GameUiState {
        if (domainState.isGameOver) {
            val winner = domainState.players.maxByOrNull { it.totalScore }
            return GameUiState(
                currentDice = domainState.turnData.diceOnTable.map { domainDice ->
                    mapDomainToUiDice(domainDice, false)
                }.sortedBy { it.id.toString() },
                canRoll = false,
                canBank = false,
                turnScore = 0,
                totalScore = winner?.totalScore ?: 0,
                selectedDiceScore = 0,
                potentialScoreAfterRoll = 0,
                isTurnOver = true,
                gameMessage = "Game Over! Winner: ${winner?.name ?: "N/A"}",
                activePlayerName = winner?.name ?: "N/A",
                isGameOver = true
            )
        }

        val currentDomainTurnData = domainState.turnData
        val currentPlayer = domainState.getCurrentPlayer()
        val gameMessage = "It's ${currentPlayer.name}'s turn."

        return GameUiState(
            currentDice = currentDomainTurnData.diceOnTable.map { domainDice ->
                val canThisDieBeHeld = gameEngine.calculateScore(listOf(domainDice)) > 0 && domainDice.isAvailable
                mapDomainToUiDice(domainDice, canThisDieBeHeld)
            }.sortedBy { it.id.toString() },
            canRoll = !domainState.isGameOver && currentDomainTurnData.diceOnTable.any { it.isAvailable },
            canBank = false,
            turnScore = currentDomainTurnData.currentTurnScore,
            totalScore = currentPlayer.totalScore,
            selectedDiceScore = 0,
            potentialScoreAfterRoll = 0,
            isTurnOver = false,
            gameMessage = gameMessage,
            activePlayerName = currentPlayer.name,
            isGameOver = domainState.isGameOver
        )
    }

    /**
     * Handles the dice roll action.
     */
    fun rollDice() {
        if (_domainGameState.value.isGameOver || !_uiState.value.canRoll) return

        viewModelScope.launch {
            val currentDomainState = _domainGameState.value
            val newDomainStateAfterRoll = gameEngine.rollDice(currentDomainState)
            _domainGameState.value = newDomainStateAfterRoll

            val potentialScoreOfAvailableDice = gameEngine.calculateScore(newDomainStateAfterRoll.turnData.diceOnTable.filter { it.isAvailable })
            val isBust = potentialScoreOfAvailableDice == 0 && newDomainStateAfterRoll.turnData.diceOnTable.any { it.isAvailable }

            _uiState.update {
                val currentPlayer = newDomainStateAfterRoll.getCurrentPlayer()
                val playerHasOpened = hasPlayerOpened(currentPlayer, gameSettings.value)
                val openingThreshold = gameSettings.value.openingScoreThreshold
                val accumulatedScoreSoFar = newDomainStateAfterRoll.turnData.currentTurnScore

                val canBankAfterRoll = !isBust &&
                        (accumulatedScoreSoFar > 0 || potentialScoreOfAvailableDice > 0) &&
                        (playerHasOpened || (accumulatedScoreSoFar + potentialScoreOfAvailableDice) >= openingThreshold)

                mapDomainToUiState(newDomainStateAfterRoll).copy(
                    potentialScoreAfterRoll = potentialScoreOfAvailableDice,
                    canRoll = !isBust && newDomainStateAfterRoll.turnData.diceOnTable.any { it.isAvailable },
                    canBank = canBankAfterRoll,
                    gameMessage = when {
                        isBust && newDomainStateAfterRoll.turnData.diceOnTable.all { it.isAvailable } -> "Bust! No points this roll. Turn over."
                        isBust -> "Bust! No points with remaining dice. Turn over."
                        else -> "Select dice to keep or roll again."
                    },
                    isTurnOver = isBust
                )
            }
            if (isBust) {
                val stateToFinalize = newDomainStateAfterRoll.copy(
                    turnData = newDomainStateAfterRoll.turnData.copy(currentTurnScore = 0)
                )
                endTurnAndBankAccumulatedScore(stateToFinalize)
            }
        }
    }

    /**
     * Handles the selection or deselection of a die.
     * @param diceId The UUID of the die to select/deselect.
     */
    fun selectDice(diceId: UUID) {
        if (_domainGameState.value.isGameOver) return

        viewModelScope.launch {
            val currentUiDiceList = _uiState.value.currentDice
            val domainDiceList = _domainGameState.value.turnData.diceOnTable
            val correspondingDomainDie = domainDiceList.find { it.id == diceId }

            if (correspondingDomainDie == null || !correspondingDomainDie.isAvailable) {
                return@launch
            }

            val updatedDiceUiList = currentUiDiceList.map {
                if (it.id == diceId) it.copy(isSelected = !it.isSelected) else it
            }

            val selectedUiDiceForScoring = updatedDiceUiList.filter { it.isSelected }
            val currentSelectedScore = gameEngine.calculateScore(selectedUiDiceForScoring.map { mapUiToDomainDice(it) })

            _uiState.update { currentState ->
                val currentPlayer = _domainGameState.value.getCurrentPlayer()
                val playerHasOpened = hasPlayerOpened(currentPlayer, gameSettings.value)
                val openingThreshold = gameSettings.value.openingScoreThreshold
                val accumulatedTurnScore = _domainGameState.value.turnData.currentTurnScore

                val canBankAfterSelection = (currentSelectedScore > 0 || accumulatedTurnScore > 0) &&
                        (playerHasOpened || (accumulatedTurnScore + currentSelectedScore) >= openingThreshold)

                val canRollAfterSelection = selectedUiDiceForScoring.isEmpty() ||
                        (currentSelectedScore > 0 && updatedDiceUiList.any { uiDice ->
                            !uiDice.isSelected && (domainDiceList.find { it.id == uiDice.id }?.isAvailable == true)
                        }) ||
                        (currentSelectedScore == 0 && selectedUiDiceForScoring.isNotEmpty())

                currentState.copy(
                    currentDice = updatedDiceUiList.sortedBy { it.id.toString() },
                    selectedDiceScore = currentSelectedScore,
                    canBank = canBankAfterSelection,
                    canRoll = canRollAfterSelection
                )
            }
        }
    }

    /**
     * Handles the action of keeping selected dice and continuing the turn.
     */
    fun keepSelectedDiceAndContinueTurn() {
        viewModelScope.launch {
            if (_domainGameState.value.isGameOver) return@launch

            if (_uiState.value.selectedDiceScore == 0) {
                val message = if (_uiState.value.currentDice.any { it.isSelected }) {
                    "Invalid selection. These dice don't score."
                } else {
                    "Invalid action: selected score is 0. Select scoring dice."
                }
                _uiState.update { it.copy(gameMessage = message) }
                return@launch
            }

            val currentSelectedDiceValue = _uiState.value.selectedDiceScore
            val currentDomainState = _domainGameState.value // Immutable snapshot

            val selectedDomainDiceIds = _uiState.value.currentDice.filter { it.isSelected }.map { it.id }.toSet()

            val diceKeptInDomain = currentDomainState.turnData.diceOnTable.map {
                if (it.id in selectedDomainDiceIds) it.copy(isAvailable = false) else it
            }

            val allDiceNowKeptAndUnavailable = diceKeptInDomain.all { !it.isAvailable }
            val accumulatedScoreAfterKeep = currentDomainState.turnData.currentTurnScore + currentSelectedDiceValue

            val nextTurnData = if (allDiceNowKeptAndUnavailable) {
                val tempStateForFullRoll = currentDomainState.copy(
                    turnData = TurnData(
                        diceOnTable = diceKeptInDomain, // All dice marked !isAvailable
                        currentTurnScore = accumulatedScoreAfterKeep
                    )
                )
                val stateAfterFullRoll = gameEngine.rollDice(tempStateForFullRoll)
                stateAfterFullRoll.turnData.copy(currentTurnScore = accumulatedScoreAfterKeep)
            } else {
                currentDomainState.turnData.copy(
                    diceOnTable = diceKeptInDomain,
                    currentTurnScore = accumulatedScoreAfterKeep
                )
            }

            val newDomainState = currentDomainState.copy(turnData = nextTurnData)
            _domainGameState.value = newDomainState

            _uiState.update {
                val currentPlayer = newDomainState.getCurrentPlayer() // Use player from new state
                val playerHasOpened = hasPlayerOpened(currentPlayer, gameSettings.value)

                val canBankAfterKeeping = newDomainState.turnData.currentTurnScore > 0 &&
                        (playerHasOpened || newDomainState.turnData.currentTurnScore >= gameSettings.value.openingScoreThreshold)
                
                val canRollAfterKeeping = newDomainState.turnData.diceOnTable.any { it.isAvailable }

                mapDomainToUiState(newDomainState).copy(
                    selectedDiceScore = 0,
                    canRoll = canRollAfterKeeping,
                    canBank = canBankAfterKeeping,
                    gameMessage = "Score kept: $currentSelectedDiceValue. Total turn score: ${newDomainState.turnData.currentTurnScore}. Roll again or bank.",
                    potentialScoreAfterRoll = 0
                )
            }
        }
    }

    /**
     * Handles the action of ending the current turn and banking the accumulated score.
     * @param gameStateAtTurnEnd Optional [GameState] to finalize, used internally after a bust.
     */
    fun endTurnAndBankAccumulatedScore(gameStateAtTurnEnd: GameState? = null) {
        if (_domainGameState.value.isGameOver && gameStateAtTurnEnd == null) return

        viewModelScope.launch {
            val stateBeforeBankAttempt = gameStateAtTurnEnd ?: _domainGameState.value
            val playerBeforeAttempt = stateBeforeBankAttempt.getCurrentPlayer()
            val turnScoreAttempted = stateBeforeBankAttempt.turnData.currentTurnScore
            val currentSettings = gameSettings.value // Capture current settings
            val playerWasOpenBeforeAttempt = hasPlayerOpened(playerBeforeAttempt, currentSettings)

            val newDomainStateAfterFinalize = finalizeTurnUseCase(stateBeforeBankAttempt, currentSettings)
            _domainGameState.value = newDomainStateAfterFinalize

            val playerAfterFinalize = newDomainStateAfterFinalize.getCurrentPlayer()
            var specificMessage: String? = null

            val scoreDidNotIncrease = playerBeforeAttempt.id == playerAfterFinalize.id && playerAfterFinalize.totalScore == playerBeforeAttempt.totalScore

            if (!playerWasOpenBeforeAttempt && turnScoreAttempted > 0 && turnScoreAttempted < currentSettings.openingScoreThreshold) {
                if (scoreDidNotIncrease && newDomainStateAfterFinalize.turnData.currentTurnScore == 0) {
                    specificMessage = "You need ${currentSettings.openingScoreThreshold} points to open. Your score of $turnScoreAttempted was not banked."
                }
            } else if (gameStateAtTurnEnd != null && turnScoreAttempted == 0 && stateBeforeBankAttempt.turnData.diceOnTable.any{it.isAvailable}) {
                 // This condition means it was a bust from rollDice, not just banking a zero score
                specificMessage = "Bust! Score for this turn is lost. It's ${playerAfterFinalize.name}'s turn."
            }
            
            val baseUiState = mapDomainToUiState(newDomainStateAfterFinalize)
            _uiState.value = baseUiState.copy(
                gameMessage = specificMessage ?: baseUiState.gameMessage
            )
        }
    }

    private fun mapDomainToUiDice(domainDice: Dice, canBeHeldPrecalculated: Boolean): DiceUi {
        return DiceUi(
            id = domainDice.id,
            value = domainDice.value,
            isSelected = false,
            canBeHeld = canBeHeldPrecalculated,
            isScored = !domainDice.isAvailable
        )
    }

    /**
     * Maps a UI [DiceUi] object back to a domain [Dice] object.
     * Primarily used for sending selected dice to the game engine for score calculation.
     * @param diceUi The [DiceUi] from the UI layer.
     * @return The corresponding domain [Dice].
     */
    private fun mapUiToDomainDice(diceUi: DiceUi): Dice {
        return Dice(
            id = diceUi.id,
            value = diceUi.value,
            isAvailable = true, 
            isSelected = diceUi.isSelected
        )
    }

    /**
     * Resets the game to its initial state.
     */
    fun newGame() {
        viewModelScope.launch {
            try {
                // Réinitialiser le state du jeu avec les settings actuels
                val initialState = createInitialDomainGameState()
                _domainGameState.value = initialState
                _uiState.value = mapDomainToUiState(initialState)
            } catch (e: Exception) {
                println("⚠️ Erreur lors du newGame: ${e.message}")
                // Fallback vers un état initial minimal
                val fallbackState = createInitialDomainGameState()
                _domainGameState.value = fallbackState
                _uiState.value = mapDomainToUiState(fallbackState)
            }
        }
    }
}
