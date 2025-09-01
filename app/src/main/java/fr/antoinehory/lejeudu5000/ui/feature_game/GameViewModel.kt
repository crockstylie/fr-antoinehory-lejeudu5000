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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
            initialValue = GameSettings() // Default settings, will be updated by the flow
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
            turnData = TurnData(diceOnTable = List(5) { Dice(value = 1, isAvailable = true) }),
            isGameOver = false
        )
    }

    private val _domainGameState = MutableStateFlow(createInitialDomainGameState())
    private val _uiState = MutableStateFlow(mapDomainToUiState(_domainGameState.value))

    /**
     * The UI state for the game screen, observed by the Composable.
     */
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private fun hasPlayerOpened(player: Player): Boolean {
        return player.scoreHistory.isNotEmpty()
    }

    /**
     * Maps the domain [GameState] to a [GameUiState] suitable for the UI.
     * This is typically called when a turn starts or the game state significantly changes.
     * @param domainState The current [GameState] from the domain layer.
     * @return The corresponding [GameUiState].
     */
    private fun mapDomainToUiState(domainState: GameState): GameUiState {
        if (domainState.isGameOver) {
            val winner = domainState.players.maxByOrNull { it.totalScore } // Simple winner logic
            return GameUiState(
                currentDice = domainState.turnData.diceOnTable.mapIndexed { index, domainDice ->
                    mapDomainToUiDice(domainDice, index, false) // Not relevant if game over
                },
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
        val gameMessage = "It's ${currentPlayer.name}'s turn!"

        // At the start of a turn (or after banking), player can always roll if dice are available.
        // Player cannot bank yet as no score has been made in this new turn segment.
        return GameUiState(
            currentDice = currentDomainTurnData.diceOnTable.mapIndexed { index, domainDice ->
                val canThisDieBeHeld = gameEngine.calculateScore(listOf(domainDice)) > 0 && domainDice.isAvailable
                mapDomainToUiDice(domainDice, index, canThisDieBeHeld)
            },
            canRoll = !domainState.isGameOver && currentDomainTurnData.diceOnTable.any { it.isAvailable },
            canBank = false, // Cannot bank at the very start of a turn segment
            turnScore = currentDomainTurnData.currentTurnScore,
            totalScore = currentPlayer.totalScore,
            selectedDiceScore = 0,
            potentialScoreAfterRoll = 0, // Will be updated after a roll
            isTurnOver = false, // A new turn segment is not over yet
            gameMessage = gameMessage,
            activePlayerName = currentPlayer.name,
            isGameOver = domainState.isGameOver
        )
    }

    /**
     * Handles the action of rolling the dice.
     */
    fun rollDice() {
        if (_domainGameState.value.isGameOver || !_uiState.value.canRoll) return

        viewModelScope.launch {
            val currentDomainState = _domainGameState.value
            val newDomainStateAfterRoll = gameEngine.rollDice(currentDomainState) // GameEngine handles dice to roll
            _domainGameState.value = newDomainStateAfterRoll

            val potentialScoreOfAvailableDice = gameEngine.calculateScore(newDomainStateAfterRoll.turnData.diceOnTable.filter { it.isAvailable })
            val isBust = potentialScoreOfAvailableDice == 0 && newDomainStateAfterRoll.turnData.diceOnTable.any{it.isAvailable} // Bust only if rolled dice score 0

            _uiState.update { currentState ->
                val currentPlayer = newDomainStateAfterRoll.getCurrentPlayer()
                val playerHasOpened = hasPlayerOpened(currentPlayer)
                val openingThreshold = gameSettings.value.openingScoreThreshold

                val accumulatedScoreSoFar = newDomainStateAfterRoll.turnData.currentTurnScore // Score from previous keeps in this turn

                // Player can bank if:
                // 1. Not a bust.
                // 2. EITHER they have an accumulated score from previous keeps in this turn
                //    OR the current roll itself has a potential score.
                // 3. If they haven't opened, the total potential bankable score (accumulated + potential of current roll) meets the threshold.
                val canBankAfterRoll = !isBust &&
                        (accumulatedScoreSoFar > 0 || potentialScoreOfAvailableDice > 0) &&
                        (playerHasOpened || (accumulatedScoreSoFar + potentialScoreOfAvailableDice) >= openingThreshold)


                currentState.copy(
                    currentDice = newDomainStateAfterRoll.turnData.diceOnTable.mapIndexed { index, domainDice ->
                        val canThisDieBeHeld = gameEngine.calculateScore(listOf(domainDice)) > 0 && domainDice.isAvailable
                        mapDomainToUiDice(domainDice, index, canThisDieBeHeld)
                    },
                    potentialScoreAfterRoll = potentialScoreOfAvailableDice,
                    turnScore = accumulatedScoreSoFar, // Turn score from keeps, doesn't include current roll's potential yet
                    totalScore = currentPlayer.totalScore,
                    selectedDiceScore = 0, // Reset on new roll
                    canRoll = !isBust && newDomainStateAfterRoll.turnData.diceOnTable.any { it.isAvailable }, // Can roll if not a bust and dice are available
                    canBank = canBankAfterRoll,
                    gameMessage = when {
                        isBust && newDomainStateAfterRoll.turnData.diceOnTable.all { it.isAvailable } -> "Bust! No points this roll. Turn over." // Bust on first roll segment
                        isBust -> "Bust! No points with remaining dice. Turn over." // Bust on subsequent roll segment
                        else -> "Select dice to keep or roll again."
                    },
                    isTurnOver = isBust,
                    activePlayerName = currentPlayer.name
                )
            }
            if (isBust) {
                // If bust, automatically end turn.
                // The score to finalize is what was accumulated BEFORE this busting roll.
                val stateToFinalize = newDomainStateAfterRoll.copy(
                    turnData = newDomainStateAfterRoll.turnData.copy(
                        currentTurnScore = currentDomainState.turnData.currentTurnScore // Score before this bust
                    )
                )
                endTurnAndBankAccumulatedScore(stateToFinalize)
            }
        }
    }

    /**
     * Handles selection or deselection of a die by the player.
     * @param diceId The ID of the die being selected/deselected.
     */
    fun selectDice(diceId: Int) {
        if (_domainGameState.value.isGameOver) return

        viewModelScope.launch {
            val currentUiDiceList = _uiState.value.currentDice
            val domainDiceList = _domainGameState.value.turnData.diceOnTable

            val targetUiDie = currentUiDiceList.find { it.id == diceId }
            val correspondingDomainDie = domainDiceList.getOrNull(diceId)

            if (targetUiDie == null || correspondingDomainDie == null || !correspondingDomainDie.isAvailable) {
                // Cannot select a die that is not available (e.g., already scored in a previous segment of this turn)
                return@launch
            }

            val updatedDiceUiList = currentUiDiceList.map {
                if (it.id == diceId) it.copy(isSelected = !it.isSelected) else it
            }

            val selectedUiDiceForScoring = updatedDiceUiList.filter { it.isSelected }
            val currentSelectedScore = gameEngine.calculateScore(selectedUiDiceForScoring.map { mapUiToDomainDice(it) })

            _uiState.update { currentState ->
                val currentPlayer = _domainGameState.value.getCurrentPlayer()
                val playerHasOpened = hasPlayerOpened(currentPlayer)
                val openingThreshold = gameSettings.value.openingScoreThreshold
                val accumulatedTurnScore = _domainGameState.value.turnData.currentTurnScore

                // Player can bank if:
                // 1. The current selection scores points (currentSelectedScore > 0)
                //    OR they already have an accumulated score from previous keeps in this turn.
                // 2. If they haven't opened, the total bankable score (accumulated + currentSelected) meets threshold.
                val canBankAfterSelection = (currentSelectedScore > 0 || accumulatedTurnScore > 0) &&
                        (playerHasOpened || (accumulatedTurnScore + currentSelectedScore) >= openingThreshold)

                // Player can roll if:
                // 1. No dice are selected (player might be deselecting).
                // 2. OR, the current selection scores points AND there are unselected available dice remaining.
                // 3. OR, the current selection scores 0 (invalid selection, player should be able to roll if they unselect all or fix selection).
                val canRollAfterSelection = selectedUiDiceForScoring.isEmpty() ||
                        (currentSelectedScore > 0 && updatedDiceUiList.any { uiDice -> !uiDice.isSelected && (domainDiceList.getOrNull(uiDice.id)?.isAvailable == true) }) ||
                        currentSelectedScore == 0 // Allows rolling if selection is invalid (scores 0)

                currentState.copy(
                    currentDice = updatedDiceUiList,
                    selectedDiceScore = currentSelectedScore,
                    canBank = canBankAfterSelection,
                    canRoll = canRollAfterSelection
                    // gameMessage might be updated here too, e.g., "Invalid selection" if currentSelectedScore is 0 but dice are selected.
                )
            }
        }
    }

    /**
     * Player chooses to keep the currently selected scoring dice and continue their turn.
     * The score of these dice is added to the turn's accumulated score.
     * The kept dice become unavailable for the next roll in this turn.
     * If all dice are scored, they all become available again.
     */
    fun keepSelectedDiceAndContinueTurn() {
        if (_domainGameState.value.isGameOver || _uiState.value.selectedDiceScore == 0) {
            // Cannot keep dice that don't score or if game is over.
            // Consider updating gameMessage for invalid keep attempt.
            if (_uiState.value.selectedDiceScore == 0) {
                _uiState.update { it.copy(gameMessage = "Invalid selection. These dice don't score.") }
            }
            return
        }

        viewModelScope.launch {
            val currentSelectedDiceValue = _uiState.value.selectedDiceScore
            val currentDomainState = _domainGameState.value
            val selectedUiDice = _uiState.value.currentDice.filter { it.isSelected }

            val newDiceForDomain = currentDomainState.turnData.diceOnTable.mapIndexed { index, oldDomainDice ->
                if (selectedUiDice.any { it.id == index }) {
                    oldDomainDice.copy(isAvailable = false) // Mark selected dice as unavailable
                } else {
                    oldDomainDice
                }
            }

            val allDiceNowScoredAndUnavailable = newDiceForDomain.all { !it.isAvailable }
            val finalDiceForNextSegment = if (allDiceNowScoredAndUnavailable) {
                newDiceForDomain.map { it.copy(isAvailable = true) } // All dice become available again
            } else {
                newDiceForDomain
            }

            val updatedTurnData = currentDomainState.turnData.copy(
                diceOnTable = finalDiceForNextSegment,
                currentTurnScore = currentDomainState.turnData.currentTurnScore + currentSelectedDiceValue
            )

            _domainGameState.update { it.copy(turnData = updatedTurnData) }

            _uiState.update { currentState ->
                val currentPlayer = currentDomainState.getCurrentPlayer() // Name won't change mid-turn
                val playerHasOpened = hasPlayerOpened(currentPlayer)
                val openingThreshold = gameSettings.value.openingScoreThreshold

                // Player can bank if the new accumulated turn score is > 0 and meets opening criteria.
                val canBankAfterKeeping = updatedTurnData.currentTurnScore > 0 &&
                        (playerHasOpened || updatedTurnData.currentTurnScore >= openingThreshold)

                // Player can roll if there are available dice for the next segment.
                val canRollAfterKeeping = finalDiceForNextSegment.any { d -> d.isAvailable }

                currentState.copy(
                    currentDice = finalDiceForNextSegment.mapIndexed { index, domainDice ->
                        mapDomainToUiDice(domainDice, index, domainDice.isAvailable && gameEngine.calculateScore(listOf(domainDice)) > 0)
                    }.sortedBy { d -> d.id },
                    turnScore = updatedTurnData.currentTurnScore,
                    selectedDiceScore = 0, // Reset after keeping
                    canRoll = canRollAfterKeeping,
                    canBank = canBankAfterKeeping,
                    gameMessage = "Score kept: $currentSelectedDiceValue. Total turn score: ${updatedTurnData.currentTurnScore}. Roll again or bank.",
                    potentialScoreAfterRoll = 0 // Reset, will be set by next roll
                )
            }
        }
    }

    /**
     * Player chooses to end their turn and bank their accumulated turn score.
     * Uses [FinalizeTurnUseCase] to apply game rules (opening, win condition) and switch player.
     * Can be called directly by UI or internally (e.g., after a bust).
     * @param gameStateAtTurnEnd Optional: The specific [GameState] to finalize. If null, uses current `_domainGameState.value`.
     */
    fun endTurnAndBankAccumulatedScore(gameStateAtTurnEnd: GameState? = null) {
         if (_domainGameState.value.isGameOver && gameStateAtTurnEnd == null) return

        viewModelScope.launch {
            val stateToFinalize = gameStateAtTurnEnd ?: _domainGameState.value
            // Ensure the turnScore to finalize is what's in stateToFinalize.turnData.currentTurnScore
            val newDomainState = finalizeTurnUseCase(stateToFinalize, gameSettings.value)
            _domainGameState.value = newDomainState
            _uiState.value = mapDomainToUiState(newDomainState) // Update UI with the state for the new turn/player or game over
        }
    }


    /**
     * Maps a domain [Dice] object to a UI [DiceUi] object.
     * @param domainDice The [Dice] from the domain layer.
     * @param id The unique identifier for the UI representation.
     * @param canBeHeldPrecalculated Indicates if this die can be held based on a pre-calculation.
     * @return The corresponding [DiceUi].
     */
    private fun mapDomainToUiDice(domainDice: Dice, id: Int, canBeHeldPrecalculated: Boolean): DiceUi {
        return DiceUi(
            id = id,
            value = domainDice.value,
            isSelected = false, // Selection is transient for a roll segment; UI will manage actual selections
            canBeHeld = canBeHeldPrecalculated,
            isScored = !domainDice.isAvailable // isScored means it has been used in current turn
        )
    }

    /**
     * Maps a UI [DiceUi] object back to a domain [Dice] object.
     * Primarily used for sending selected dice to the game engine for score calculation.
     * @param diceUi The [DiceUi] from the UI layer.
     * @return The corresponding domain [Dice].
     */
    private fun mapUiToDomainDice(diceUi: DiceUi): Dice {
        // For score calculation, the domain Dice is considered "available" in that context.
        // Its actual persisted isAvailable state is managed in _domainGameState.
        return Dice(value = diceUi.value, isAvailable = true)
    }

    /**
     * Resets the game to its initial state for a new game.
     */
    fun newGame() {
        val newInitialDomainState = createInitialDomainGameState()
        _domainGameState.value = newInitialDomainState
        _uiState.value = mapDomainToUiState(newInitialDomainState)
    }
}
