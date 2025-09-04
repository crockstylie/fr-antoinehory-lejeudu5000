package fr.antoinehory.lejeudu5000.domain.model

import java.util.UUID

/**
 * Represents a single score entry in a player's history.
 * This is used to track scores banked by a player during the game,
 * which is essential for rules like "Score Cancellation" and "3 Lives Rule".
 * KDoc in English as requested.
 *
 * @property entryId A unique identifier for this score history entry.
 * @property recordedScore The total score of the player AFTER this specific score was banked.
 *                         This value is what `hasPlayerOpened` should primarily check against for opening thresholds,
 *                         and what would be reverted to for "Score Cancellation" or "3 Lives Rule".
 * @property timestamp The time at which this score was recorded, for chronological ordering.
 */
data class ScoreHistory(
    val entryId: UUID = UUID.randomUUID(),
    val recordedScore: Int,
    val timestamp: Long = System.currentTimeMillis()
)
