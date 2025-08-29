package fr.antoinehory.lejeudu5000.domain.model

/**
 * Represents the state of the current player's turn.
 *
 * @property diceOnTable The list of all dice and their current state for this turn.
 * @property currentTurnScore The score accumulated so far in this turn (not yet banked).
 * @property hasOpened Indicates if the player has met the opening score threshold for this game.
 */
data class TurnData(
    val diceOnTable: List<Dice> = List(5) { Dice(value = 1) }, // Default to 5 dice
    val currentTurnScore: Int = 0,
    val hasOpened: Boolean = false
)