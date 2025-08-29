package fr.antoinehory.lejeudu5000.domain

import fr.antoinehory.lejeudu5000.domain.model.GameState
import fr.antoinehory.lejeudu5000.domain.model.Dice
import javax.inject.Inject
import kotlin.random.Random

/**
 * The core logic engine for the "5000" dice game.
 * This class is stateless and operates solely on the provided [GameState].
 * It is responsible for applying game rules and returning a new, updated state.
 * It has no dependency on the Android framework, making it highly testable.
 *
 * KDoc in English as requested.
 */
class GameEngine @Inject constructor() {

    /**
     * Executes the "roll dice" action for the current player.
     * It rolls only the dice that are marked as available in the current turn state.
     *
     * @param currentState The current state of the game.
     * @return A new [GameState] with the dice values updated after the roll.
     */
    fun rollDice(currentState: GameState): GameState {
        val currentTurn = currentState.turnData
        val availableDiceCount = currentTurn.diceOnTable.count { it.isAvailable }

        // If all dice were used, the player gets a full new set of 5 dice to roll.
        val diceToRoll = if (availableDiceCount == 0) {
            List(5) { Dice(value = Random.nextInt(1, 7)) }
        } else {
            // Otherwise, only roll the available dice and keep the selected ones.
            currentTurn.diceOnTable.map { die ->
                if (die.isAvailable) {
                    die.copy(value = Random.nextInt(1, 7))
                } else {
                    die
                }
            }
        }

        // The game state is immutable. We return a new instance with the updated data.
        return currentState.copy(
            turnData = currentTurn.copy(
                diceOnTable = diceToRoll
            )
        )
    }

    // --- Other game actions will be added here ---
    // fun selectDice(currentState: GameState, diceToSelect: List<Dice>): GameState { ... }
    // fun bankScore(currentState: GameState): GameState { ... }
    // fun calculateScore(dice: List<Dice>): Int { ... }
}