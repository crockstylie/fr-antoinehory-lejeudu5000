package fr.antoinehory.lejeudu5000.ui.feature_game

import androidx.compose.runtime.Immutable
import java.util.UUID // Import UUID

/**
 * Represents the complete UI state for the game screen.
 * KDoc in English as requested.
 *
 * @property currentDice List of [DiceUi] objects representing the dice on the screen.
 * @property canRoll Boolean indicating if the player is allowed to roll the dice.
 * @property canBank Boolean indicating if the player is allowed to bank their current turn score.
 * @property turnScore The score accumulated by the player in the current turn.
 * @property totalScore The player's total score in the game.
 * @property gameMessage A message to be displayed to the player (e.g., instructions, errors, game events).
 * @property isGameOver Boolean indicating if the game has ended.
 * @property activePlayerName The name or identifier of the currently active player.
 * @property isTurnOver Boolean indicating if the current player's turn is over (e.g., after busting or banking).
 * @property selectedDiceScore The score calculated for the currently selected dice by the player.
 * @property potentialScoreAfterRoll The potential score calculated by the engine after a roll (before player selection).
 */
@Immutable
data class GameUiState(
    val currentDice: List<DiceUi> = List(5) { DiceUi(id = UUID.randomUUID(), value = 1) }, // Changed to use UUID
    val canRoll: Boolean = true,
    val canBank: Boolean = false,
    val turnScore: Int = 0,
    val totalScore: Int = 0,
    val gameMessage: String = "Welcome! Roll the dice to start.",
    val isGameOver: Boolean = false,
    val activePlayerName: String = "Player 1", // Placeholder, could be dynamic
    val isTurnOver: Boolean = false,
    val selectedDiceScore: Int = 0,
    val potentialScoreAfterRoll: Int = 0
) {
    companion object {
        /**
         * Represents the initial, default state for the game screen.
         * KDoc in English as requested.
         */
        val INITIAL = GameUiState()
    }
}
