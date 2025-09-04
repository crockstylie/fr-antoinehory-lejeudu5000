package fr.antoinehory.lejeudu5000.domain.model

import java.util.UUID

/**
 * Represents a player in the game.
 * KDoc in English as requested.
 *
 * @property id A unique identifier for the player.
 * @property name The name of the player.
 * @property totalScore The player's total banked score across all turns.
 * @property scoreHistory A list of [ScoreHistory] objects, tracking each time the player successfully banked points.
 *                        This is crucial for opening thresholds and game rules like "Score Cancellation".
 * @property lives The number of lives remaining for the current score streak, for the "3 Lives" rule.
 */
data class Player(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val totalScore: Int = 0,
    val scoreHistory: List<ScoreHistory> = emptyList(), // Changed from List<Int>
    val lives: Int = 3
)