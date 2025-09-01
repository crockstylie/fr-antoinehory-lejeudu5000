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

    /**
     * Calculates the score for a given list of dice based on the game rules.
     * Scoring hierarchy (highest to lowest, mutually exclusive for 5-dice combinations):
     * 1. Five of a Kind (Yam's of 1s)
     * 2. Straights (1-5 or 2-6)
     * 3. Full House (Brelan + Pair) - Complex rules based on test cases.
     * If none of the above 5-dice combinations are met, scores are calculated additively:
     * 4. Three of a Kind (Brelans)
     * 5. Individual 1s and 5s
     * Dice used in a combination are not scored again.
     *
     * @param selectedDice The list of [Dice] objects to calculate score from.
     * @return The calculated score.
     */
    fun calculateScore(selectedDice: List<Dice>): Int {
        if (selectedDice.isEmpty()) return 0

        val diceValues = selectedDice.map { it.value }.sorted()
        val n = diceValues.size
        val counts = diceValues.groupingBy { it }.eachCount().toMutableMap()
        
        var score = 0

        // 1. Five 1s (Yam's of 1s)
        if (n == 5) {
            val firstValue = diceValues[0]
            if (diceValues.all { it == firstValue }) {
                if (firstValue == 1) {
                    return 5000 // Five 1s (As)
                }
                // Other Five of a Kind (e.g., five 2s) will be scored by Brelan + Singles logic below
                // This change is to align with tests like fiveTwos_scores200, fiveFives_scores600
            }
        }

        // 2. Straights (1-5 or 2-6)
        if (n == 5) {
            val uniqueSortedValues = diceValues.distinct()
            if (uniqueSortedValues.size == 5) {
                val isSmallStraight = uniqueSortedValues == listOf(1, 2, 3, 4, 5)
                val isLargeStraight = uniqueSortedValues == listOf(2, 3, 4, 5, 6)
                if (isSmallStraight || isLargeStraight) {
                    return 1500
                }
            }
        }
        
        // 3. Full House (Brelan + Pair) - Logic adjusted to match test cases in GameEngineTest.kt
        if (n == 5 && counts.size == 2 && counts.values.contains(3) && counts.values.contains(2)) {
            var fullHouseScore = 0
            val brelanValue = counts.filterValues { it == 3 }.keys.first()
            val pairValue = counts.filterValues { it == 2 }.keys.first()

            if (brelanValue == 1) { // Brelan of 1s
                fullHouseScore = 1000 // Score for Brelan of 1s
                if (pairValue == 5) { // Exception: Brelan 1s, Paire 5s (1,1,1,5,5)
                    fullHouseScore += 100 // Adds 100 for the pair of 5s (Total 1100)
                }
                // If pairValue is not 5 (e.g., 1,1,1,2,2), pair adds 0 (Total 1000)
            } else { // Brelan is NOT 1s (e.g., 2s, 3s, 4s, 5s, 6s)
                if (pairValue == 1) {
                    // Exception: Brelan Xs, Paire 1s (e.g., 2,2,2,1,1 or 5,5,5,1,1)
                    // Score = Brelan X (X*100) + 200 for the Pair of 1s
                    fullHouseScore = (brelanValue * 100) + 200
                } else {
                    // General multiplicative rule for other Full Houses (e.g., Brelan 3s, Paire 2s or Brelan 2s, Paire 5s)
                    // Score = brelanValue * pairValue * 100
                    // As per test comments like (2*3*100=600) for {3,3,3,2,2}
                    // Or (5*2*100=1000) for {2,2,2,5,5}
                    fullHouseScore = brelanValue * pairValue * 100
                }
            }
            return fullHouseScore // All 5 dice are consumed
        }
        
        // If no 5-dice total-score combinations were found,
        // proceed to score combinations additively.

        // 4. Three of a Kind (Brelans)
        val brelanValuesToTest = listOf(1) + (6 downTo 2).toList()

        for (valueToTest in brelanValuesToTest) {
            if (counts.getOrDefault(valueToTest, 0) >= 3) {
                score += when (valueToTest) {
                    1 -> 1000
                    else -> valueToTest * 100
                }
                counts[valueToTest] = counts.getOrDefault(valueToTest, 0) - 3
                if (counts.getOrDefault(valueToTest, 0) == 0) {
                    counts.remove(valueToTest)
                }
                // Important: If a Brelan is found, remaining dice are scored as singles.
                // No need to break, as multiple Brelans are not standard in 5000 with 5 dice.
                // The dice are consumed from 'counts'.
            }
        }

        // 5. Individual 1s and 5s (from remaining dice in counts map)
        score += counts.getOrDefault(1, 0) * 100
        score += counts.getOrDefault(5, 0) * 50

        return score
    }
}
