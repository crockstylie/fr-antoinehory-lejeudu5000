package fr.antoinehory.lejeudu5000.domain.model

/**
 * Represents the entire state of a game session at a specific moment.
 * This object is the single source of truth for the game logic.
 *
 * @property players A list of all players in the game.
 * @property currentPlayerIndex The index of the player whose turn it is.
 * @property turnData The state of the current turn.
 * @property isGameOver Indicates if the game has concluded.
 * @property winner The player who won the game, if any.
 * @property gameSettings The configured rules for this game session.
 */
data class GameState(
    val players: List<Player>,
    val currentPlayerIndex: Int = 0,
    val turnData: TurnData = TurnData(),
    val isGameOver: Boolean = false,
    val winner: Player? = null,
    // val gameSettings: GameSettings // We will define this class later
) {
    /**
     * @return The [Player] object for the current turn.
     */
    fun getCurrentPlayer(): Player = players[currentPlayerIndex]
}