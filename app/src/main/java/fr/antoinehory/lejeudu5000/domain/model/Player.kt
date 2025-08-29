package fr.antoinehory.lejeudu5000.domain.model

import java.util.UUID

/**
 * Represents a player in the game.
 *
 * @property id A unique identifier for the player.
 * @property name The name of the player.
 * @property totalScore The player's total banked score across all turns.
 * @property scoreHistory A list of all scores the player has successfully banked.
 * @property lives The number of lives remaining for the current score streak, for the "3 Lives" rule.
 */
data class Player(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val totalScore: Int = 0,
    val scoreHistory: List<Int> = emptyList(),
    val lives: Int = 3
)