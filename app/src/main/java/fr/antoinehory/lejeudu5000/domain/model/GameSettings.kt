package fr.antoinehory.lejeudu5000.domain.model

/**
 * Represents the configurable rules for a game session.
 *
 * @property openingScoreThreshold The minimum score required to open.
 * @property victoryScore The target score to win the game.
 * @property mustWinOnExactScore If true, the player must hit the victory score exactly.
 * @property cancelOpponentScoreOnMatch If true, matching an opponent's score cancels it.
 * @property allowFiftyPointScores If true, scores ending in 50 are allowed.
 * @property useThreeLivesRule If true, the three consecutive busts rule is active.
 * @property allowStealOnPass If true, players can pass their turn to the next player.
 */
data class GameSettings(
    val openingScoreThreshold: Int = 500,
    val victoryScore: Int = 5000,
    val mustWinOnExactScore: Boolean = false,
    val cancelOpponentScoreOnMatch: Boolean = false,
    val allowFiftyPointScores: Boolean = false,
    val useThreeLivesRule: Boolean = false,
    val allowStealOnPass: Boolean = false
)