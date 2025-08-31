package fr.antoinehory.lejeudu5000.data.repository

import fr.antoinehory.lejeudu5000.domain.model.GameSettings
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing game settings.
 *
 * This interface defines the contract for accessing and modifying game settings,
 * abstracting the data source (e.g., DataStore) from the ViewModel.
 */
interface SettingsRepository {

    /**
     * A [Flow] that emits the current [GameSettings] whenever they change.
     *
     * @return A [Flow] of [GameSettings].
     */
    fun getGameSettings(): Flow<GameSettings>

    /**
     * Updates the opening score threshold.
     *
     * @param threshold The new opening score threshold.
     */
    suspend fun updateOpeningScoreThreshold(threshold: Int)

    /**
     * Updates the victory score.
     *
     * @param score The new victory score.
     */
    suspend fun updateVictoryScore(score: Int)

    /**
     * Updates the 'must win on exact score' setting.
     *
     * @param mustBeExact True if victory must be on exact score, false otherwise.
     */
    suspend fun updateMustWinOnExactScore(mustBeExact: Boolean)

    /**
     * Updates the 'cancel opponent score on match' setting.
     *
     * @param cancelOnMatch True if opponent's score should be cancelled on match, false otherwise.
     */
    suspend fun updateCancelOpponentScoreOnMatch(cancelOnMatch: Boolean)

    /**
     * Updates the 'allow fifty point scores' setting.
     *
     * @param allowFifty True if scores ending in 50 are allowed, false otherwise.
     */
    suspend fun updateAllowFiftyPointScores(allowFifty: Boolean)

    /**
     * Updates the 'use three lives rule' setting.
     *
     * @param useThreeLives True if the three lives rule is active, false otherwise.
     */
    suspend fun updateUseThreeLivesRule(useThreeLives: Boolean)

    /**
     * Updates the 'allow steal on pass' setting.
     *
     * @param allowSteal True if stealing score on pass is allowed, false otherwise.
     */
    suspend fun updateAllowStealOnPass(allowSteal: Boolean)
}

