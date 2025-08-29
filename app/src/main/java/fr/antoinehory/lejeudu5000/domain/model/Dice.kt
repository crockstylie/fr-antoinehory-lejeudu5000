package fr.antoinehory.lejeudu5000.domain.model

import java.util.UUID

/**
 * Represents a single six-sided die in the game.
 * KDoc in English as requested.
 *
 * @property id A unique identifier for the die instance, useful for UI state management.
 * @property value The face value of the die, from 1 to 6.
 * @property isAvailable Indicates if the die can be rolled in the next throw.
 * @property isSelected Indicates if the player has selected this die in the current turn.
 */
data class Dice(
    val id: UUID = UUID.randomUUID(),
    val value: Int,
    val isAvailable: Boolean = true,
    val isSelected: Boolean = false
)