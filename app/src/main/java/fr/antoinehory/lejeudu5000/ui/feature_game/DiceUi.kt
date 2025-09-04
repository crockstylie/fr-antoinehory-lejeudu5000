package fr.antoinehory.lejeudu5000.ui.feature_game

import androidx.compose.runtime.Immutable
import java.util.UUID // Import a UUID

/**
 * Represents the UI state for a single die in the game.
 * KDoc in English as requested.
 *
 * @property id A unique identifier for the die instance, aligns with domain model.
 * @property value The current face value of the die.
 * @property isSelected Indicates whether the user has selected this die in the current turn.
 * @property canBeHeld Indicates if the die is part of a scoring combination from the latest roll
 *                     and can be held by the player. This is often determined by the GameEngine.
 * @property isScored Indicates if this die has already been used to form part of the turn's score
 *                  and should potentially be visually distinct (e.g., greyed out or moved).
 */
@Immutable
data class DiceUi(
    val id: UUID, // Changed from Int to UUID
    val value: Int,
    val isSelected: Boolean = false,
    val canBeHeld: Boolean = false,
    val isScored: Boolean = false
)