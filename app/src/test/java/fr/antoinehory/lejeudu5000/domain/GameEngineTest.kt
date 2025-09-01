package fr.antoinehory.lejeudu5000.domain

import fr.antoinehory.lejeudu5000.domain.model.Dice
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Unit tests for the [GameEngine.calculateScore] method.
 * These tests verify the scoring logic based on the provided game rules for "Le jeu du 5000".
 * The expected hierarchy for combinations is: Five 1s > Suite > Full House > Brelan (Three of a Kind) > Single 1s and 5s.
 */
class GameEngineTest {

    private lateinit var gameEngine: GameEngine

    /**
     * Sets up the test environment before each test.
     * Initializes the [GameEngine] instance.
     */
    @BeforeEach
    fun setUp() {
        // Assuming GameEngine has a constructor that might take game settings or dependencies.
        // For pure calculateScore testing, a simple instance should suffice if calculateScore is self-contained.
        // If GameSettings are needed by calculateScore (e.g. for variants affecting base scores),
        // they should be mocked or provided here. For now, assuming direct rule implementation.
        gameEngine = GameEngine()
    }

    /**
     * Helper function to create a list of [Dice] objects from integer values.
     * Assumes the [Dice] data class primarily uses the [value] for scoring calculations.
     *
     * @param values The integer face values of the dice.
     * @return A list of [Dice] objects.
     */
    private fun diceList(vararg values: Int): List<Dice> {
        return values.map { Dice(value = it) } // Simplified Dice creation for scoring tests
    }

    @Nested
    @DisplayName("0. Empty or No Scoring Dice")
    inner class EmptyOrNoScoringDiceTests {
        @Test
        @DisplayName("calculateScore with an empty list should return 0")
        fun emptyList_returnsZero() {
            assertEquals(0, gameEngine.calculateScore(emptyList()))
        }

        @Test
        @DisplayName("calculateScore with non-scoring dice only (2,3,4,6) should return 0")
        fun nonScoringDice_returnsZero() {
            assertEquals(0, gameEngine.calculateScore(diceList(2, 3, 4, 6, 2)))
        }

        @Test
        @DisplayName("calculateScore with a single non-scoring die (e.g., 2) should return 0")
        fun singleNonScoringDie_returnsZero() {
            assertEquals(0, gameEngine.calculateScore(diceList(2)))
        }
    }

    @Nested
    @DisplayName("1. Single Dice Scoring")
    inner class SingleDiceScoringTests {
        @Test
        @DisplayName("Single 1 should score 100 points")
        fun singleOne_scores100() {
            assertEquals(100, gameEngine.calculateScore(diceList(1)))
        }

        @Test
        @DisplayName("Single 5 should score 50 points")
        fun singleFive_scores50() {
            assertEquals(50, gameEngine.calculateScore(diceList(5)))
        }

        @Test
        @DisplayName("Multiple mixed single scoring dice (1s and 5s) should sum up correctly")
        fun multipleSingleScoringDice_sumCorrectly() {
            assertEquals(150, gameEngine.calculateScore(diceList(1, 5)))
            assertEquals(200, gameEngine.calculateScore(diceList(1, 5, 5, 2))) // 100 + 50 + 50 + 0
            assertEquals(200, gameEngine.calculateScore(diceList(1, 1, 4)))    // 100 + 100 + 0
        }
    }

    @Nested
    @DisplayName("2. Brelan (Three of a Kind) Scoring")
    inner class BrelanScoringTests {
        @Test
        @DisplayName("Three 1s should score 1000 points")
        fun threeOnes_scores1000() {
            assertEquals(1000, gameEngine.calculateScore(diceList(1, 1, 1)))
        }

        @Test
        @DisplayName("Three 2s should score 200 points")
        fun threeTwos_scores200() {
            assertEquals(200, gameEngine.calculateScore(diceList(2, 2, 2)))
        }

        @Test
        @DisplayName("Three 3s should score 300 points")
        fun threeThrees_scores300() {
            assertEquals(300, gameEngine.calculateScore(diceList(3, 3, 3)))
        }

        @Test
        @DisplayName("Three 4s should score 400 points")
        fun threeFours_scores400() {
            assertEquals(400, gameEngine.calculateScore(diceList(4, 4, 4)))
        }

        @Test
        @DisplayName("Three 5s should score 500 points")
        fun threeFives_scores500() {
            assertEquals(500, gameEngine.calculateScore(diceList(5, 5, 5)))
        }

        @Test
        @DisplayName("Three 6s should score 600 points")
        fun threeSixes_scores600() {
            assertEquals(600, gameEngine.calculateScore(diceList(6, 6, 6)))
        }

        @Test
        @DisplayName("Brelan with additional scoring dice should sum correctly")
        fun brelanWithScoringDice_sumsCorrectly() {
            assertEquals(1050, gameEngine.calculateScore(diceList(1, 1, 1, 5)))    // 1000 + 50
            assertEquals(250, gameEngine.calculateScore(diceList(2, 2, 2, 5)))     // 200 + 50
            assertEquals(550, gameEngine.calculateScore(diceList(5, 5, 5, 5)))     // Brelan of 5s (500) + one 5 (50)
            assertEquals(1100, gameEngine.calculateScore(diceList(1, 1, 1, 1)))    // Brelan of 1s (1000) + one 1 (100)
        }

        @Test
        @DisplayName("Brelan with additional non-scoring dice scores only the Brelan")
        fun brelanWithNonScoringDice_scoresBrelan() {
            assertEquals(1000, gameEngine.calculateScore(diceList(1, 1, 1, 2, 3)))
            assertEquals(200, gameEngine.calculateScore(diceList(2, 2, 2, 3, 4)))
        }

        @Test
        @DisplayName("Four of a kind (non-1s) scores as Brelan + remaining die (if it scores)")
        fun fourOfAKind_nonOnes_scoresAsBrelanPlusSingle() {
            assertEquals(200, gameEngine.calculateScore(diceList(2, 2, 2, 2, 3))) // Brelan 2s (200), 2 (0), 3 (0)
            assertEquals(550, gameEngine.calculateScore(diceList(5, 5, 5, 5, 2))) // Brelan 5s (500), 5 (50), 2 (0)
        }
    }

    @Nested
    @DisplayName("3. Full House Scoring (Brelan + Paire)")
    inner class FullHouseScoringTests {
        @Test
        @DisplayName("Full House (Brelan 3s, Paire 2s) should score (2*3*100) = 600 points")
        fun fullHouse_3sAnd2s_scores600() {
            assertEquals(600, gameEngine.calculateScore(diceList(3, 3, 3, 2, 2)))
        }

        @Test
        @DisplayName("Full House (Brelan 2s, Paire 5s) should score (5*2*100) = 1000 points")
        fun fullHouse_2sAnd5s_scores1000() {
            assertEquals(1000, gameEngine.calculateScore(diceList(2, 2, 2, 5, 5)))
        }

        @Test
        @DisplayName("Full House (Brelan 1s, Paire 2s) Exception: Brelan 1s (1000) + Paire 2s (0) = 1000 points")
        fun fullHouse_Brelan1sPaire2s_scores1000() {
            assertEquals(1000, gameEngine.calculateScore(diceList(1, 1, 1, 2, 2)))
        }

        @Test
        @DisplayName("Full House (Brelan 1s, Paire 5s) Exception: Brelan 1s (1000) + Paire 5s (100) = 1100 points")
        fun fullHouse_Brelan1sPaire5s_scores1100() {
            assertEquals(1100, gameEngine.calculateScore(diceList(1, 1, 1, 5, 5)))
        }

        @Test
        @DisplayName("Full House (Brelan 2s, Paire 1s) Exception: Brelan 2s (200) + Paire 1s (200) = 400 points")
        fun fullHouse_Brelan2sPaire1s_scores400() {
            assertEquals(400, gameEngine.calculateScore(diceList(2, 2, 2, 1, 1)))
        }

        @Test
        @DisplayName("Full House (Brelan 5s, Paire 1s) Exception: Brelan 5s (500) + Paire 1s (200) = 700 points")
        fun fullHouse_Brelan5sPaire1s_scores700() {
            assertEquals(700, gameEngine.calculateScore(diceList(5, 5, 5, 1, 1)))
        }
         @Test
        @DisplayName("Full House (Brelan 6s, Paire 4s) should score (4*6*100) = 2400 points")
        fun fullHouse_6sAnd4s_scores2400() {
            assertEquals(2400, gameEngine.calculateScore(diceList(6,6,6,4,4)))
        }
    }

    @Nested
    @DisplayName("4. Suite (Straight) Scoring")
    inner class SuiteScoringTests {
        @Test
        @DisplayName("Suite 1-2-3-4-5 should score 1500 points")
        fun suite1To5_scores1500() {
            assertEquals(1500, gameEngine.calculateScore(diceList(1, 2, 3, 4, 5)))
            assertEquals(1500, gameEngine.calculateScore(diceList(5, 3, 1, 4, 2))) // Order irrelevant
        }

        @Test
        @DisplayName("Suite 2-3-4-5-6 should score 1500 points")
        fun suite2To6_scores1500() {
            assertEquals(1500, gameEngine.calculateScore(diceList(2, 3, 4, 5, 6)))
            assertEquals(1500, gameEngine.calculateScore(diceList(6, 4, 2, 5, 3))) // Order irrelevant
        }

        @Test
        @DisplayName("Non-suite combinations should be scored based on other rules")
        fun nonSuite_scoresNormally() {
            assertEquals(150, gameEngine.calculateScore(diceList(1, 2, 3, 5, 6))) // 1 (100) + 5 (0 from 6) + others (0)
        }
    }

    @Nested
    @DisplayName("5. Five of a Kind Scoring")
    inner class FiveOfAKindScoringTests {
        @Test
        @DisplayName("Five 1s should score 5000 points (special rule)")
        fun fiveOnes_scores5000() {
            assertEquals(5000, gameEngine.calculateScore(diceList(1, 1, 1, 1, 1)))
        }

        @Test
        @DisplayName("Five 2s should score as Brelan of 2s (200) + two 2s (0) = 200 points")
        fun fiveTwos_scores200() {
            // According to provided rules, only five 1s are special.
            // Five 2s would be: Brelan(2,2,2) = 200. Remaining dice: 2, 2 (score 0).
            assertEquals(200, gameEngine.calculateScore(diceList(2, 2, 2, 2, 2)))
        }

        @Test
        @DisplayName("Five 5s should score as Brelan of 5s (500) + two 5s (100) = 600 points")
        fun fiveFives_scores600() {
            // Brelan(5,5,5) = 500. Remaining dice: 5 (50), 5 (50). Total 500 + 50 + 50 = 600.
            assertEquals(600, gameEngine.calculateScore(diceList(5, 5, 5, 5, 5)))
        }

        @Test
        @DisplayName("Five 6s should score as Brelan of 6s (600) + two 6s (0) = 600 points")
        fun fiveSixes_scores600() {
            // Brelan(6,6,6) = 600. Remaining dice: 6 (0), 6 (0). Total 600.
            assertEquals(600, gameEngine.calculateScore(diceList(6, 6, 6, 6, 6)))
        }
    }

    @Nested
    @DisplayName("6. Combination Hierarchy and Complex Scenarios")
    inner class CombinationHierarchyTests {
        @Test
        @DisplayName("Suite (1-5) should take precedence over individual 1s and 5s")
        fun suiteTakesPrecedenceOverSingles() {
            assertEquals(1500, gameEngine.calculateScore(diceList(1, 2, 3, 4, 5)))
        }

        @Test
        @DisplayName("Full House (Brelan 1s, Paire 5s = 1100) should be chosen over Brelan 1s (1000) + two 5s (100)")
        fun fullHousePrecedence() {
            // The score is the same (1100), but logic should identify Full House if present.
            // This test primarily ensures the score is correct.
            // Testing if "Full House" was specifically identified would require deeper inspection of GameEngine state,
            // or if calculateScore returned a structure detailing combinations found.
            assertEquals(1100, gameEngine.calculateScore(diceList(1, 1, 1, 5, 5)))
        }
        
        @Test
        @DisplayName("Five 1s (5000) takes precedence over Full House of 1s or Brelan of 1s")
        fun fiveOnesTotalPrecedence() {
            assertEquals(5000, gameEngine.calculateScore(diceList(1, 1, 1, 1, 1)))
        }

        @Test
        @DisplayName("Dice forming a Brelan and other scoring dice should sum up correctly")
        fun brelanAndOtherScoringDice() {
            // Brelan of 2s (200), one 1 (100), one 5 (50)
            assertEquals(350, gameEngine.calculateScore(diceList(2, 2, 2, 1, 5)))
        }

        @Test
        @DisplayName("Dice forming a Brelan and other non-scoring dice")
        fun brelanAndNonScoringDice() {
            assertEquals(300, gameEngine.calculateScore(diceList(3,3,3,2,4))) // Brelan 3s (300) + 0 + 0
        }
    }
}
