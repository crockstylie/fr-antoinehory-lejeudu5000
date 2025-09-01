package fr.antoinehory.lejeudu5000.domain.usecase

import fr.antoinehory.lejeudu5000.domain.model.GameSettings
import fr.antoinehory.lejeudu5000.domain.model.GameState
import fr.antoinehory.lejeudu5000.domain.model.Player
import fr.antoinehory.lejeudu5000.domain.model.TurnData
import fr.antoinehory.lejeudu5000.domain.model.Dice
import javax.inject.Inject

/**
 * Use case for finalizing a player's turn.
 *
 * This use case handles the logic when a player's turn ends, either by choice to bank their score
 * or due to other game conditions (e.g., a bust, though bust handling might prevent score banking).
 * It updates the player's total score, checks game rules like opening threshold and win conditions,
 * and prepares the game state for the next player.
 */
class FinalizeTurnUseCase @Inject constructor() {

    /**
     * Executes the turn finalization logic.
     *
     * @param currentGameState The state of the game at the end of the current player's turn.
     * @param gameSettings The current game settings.
     * @return The new [GameState] prepared for the next player or the end of the game.
     */
    operator fun invoke(
        currentGameState: GameState,
        gameSettings: GameSettings
    ): GameState {
        val currentPlayer = currentGameState.getCurrentPlayer()
        val accumulatedTurnScore = currentGameState.turnData.currentTurnScore
        var newPlayerTotalScore = currentPlayer.totalScore
        var playerActuallyOpenedOrBanked = false

        // 1. Check Opening Score Threshold
        // A player must achieve the opening score in a single turn to "open".
        // If they haven't opened, and their current turn score doesn't meet the threshold, they score 0 for the turn.
        if (currentPlayer.totalScore == 0) { // Player has not opened yet
            if (accumulatedTurnScore >= gameSettings.openingScoreThreshold) {
                newPlayerTotalScore += accumulatedTurnScore
                playerActuallyOpenedOrBanked = true
                // TODO: Consider a Player.hasOpened flag if needed beyond totalScore > 0
            } else {
                // Failed to open, scores 0 for this turn.
                // Game message should indicate this. (Handled by ViewModel based on state changes)
                playerActuallyOpenedOrBanked = false
            }
        } else { // Player has already opened
            if (accumulatedTurnScore > 0) { // Can only bank a positive score
                newPlayerTotalScore += accumulatedTurnScore
                playerActuallyOpenedOrBanked = true
            }
        }

        // 2. Apply "Must Win on Exact Score"
        if (gameSettings.mustWinOnExactScore && newPlayerTotalScore > gameSettings.victoryScore) {
            // Player exceeded victory score and must be exact; they bust this turn's score.
            // Revert to their score before this turn.
            newPlayerTotalScore = currentPlayer.totalScore
            playerActuallyOpenedOrBanked = false // Effectively, the bank failed or was nullified.
            // Game message should indicate this.
        }

        // TODO: 3. Apply "Cancel Opponent Score on Match"
        // This would require iterating through other players and comparing newPlayerTotalScore.
        // If a match is found, that opponent's score might be reset based on rules.
        // This is a complex interaction and should be handled carefully.

        // Update player object
        val updatedPlayers = currentGameState.players.mapIndexed { index, player ->
            if (index == currentGameState.currentPlayerIndex) {
                player.copy(totalScore = newPlayerTotalScore)
            } else {
                player
            }
        }

        // 4. Check Win Condition
        var isGameOver = currentGameState.isGameOver
        if (!isGameOver) { // Only check if game not already over
            val winningPlayer = updatedPlayers[currentGameState.currentPlayerIndex]
            if (gameSettings.mustWinOnExactScore) {
                if (winningPlayer.totalScore == gameSettings.victoryScore) {
                    isGameOver = true
                }
            } else {
                if (winningPlayer.totalScore >= gameSettings.victoryScore) {
                    isGameOver = true
                }
            }
        }

        // 5. Prepare for Next Player
        val nextPlayerIndex = if (!isGameOver) {
            (currentGameState.currentPlayerIndex + 1) % updatedPlayers.size
        } else {
            currentGameState.currentPlayerIndex // Winner remains current if game is over
        }

        // Reset TurnData for the next turn
        val nextTurnData = TurnData(
            diceOnTable = List(5) { Dice(value = 1, isAvailable = true) }, // Fresh dice
            currentTurnScore = 0 // Reset turn score
        )

        return currentGameState.copy(
            players = updatedPlayers,
            currentPlayerIndex = nextPlayerIndex,
            turnData = nextTurnData,
            isGameOver = isGameOver
            // Consider adding a field in GameState to pass a specific message key for UI
            // e.g., lastEvent = GameEvent.PlayerOpened | GameEvent.PlayerBustedOpening | GameEvent.PlayerWon
        )
    }
}
