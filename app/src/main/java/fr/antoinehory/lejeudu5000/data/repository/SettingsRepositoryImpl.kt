package fr.antoinehory.lejeudu5000.data.repository

import fr.antoinehory.lejeudu5000.data.datastore.SettingsDataStore
import fr.antoinehory.lejeudu5000.domain.model.GameSettings
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [SettingsRepository] that uses [SettingsDataStore]
 * to manage game settings.
 *
 * @param settingsDataStore The data store for game settings.
 */
@Singleton // This repository will be a singleton
class SettingsRepositoryImpl @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : SettingsRepository {

    /**
     * Retrieves a [Flow] of the current [GameSettings].
     * Delegates to [SettingsDataStore.gameSettingsFlow].
     *
     * @return A [Flow] of [GameSettings].
     */
    override fun getGameSettings(): Flow<GameSettings> {
        return settingsDataStore.gameSettingsFlow
    }

    /**
     * Updates the opening score threshold in the data store.
     *
     * @param threshold The new opening score threshold.
     */
    override suspend fun updateOpeningScoreThreshold(threshold: Int) {
        settingsDataStore.updateOpeningScoreThreshold(threshold)
    }

    /**
     * Updates the victory score in the data store.
     *
     * @param score The new victory score.
     */
    override suspend fun updateVictoryScore(score: Int) {
        settingsDataStore.updateVictoryScore(score)
    }

    /**
     * Updates the 'must win on exact score' setting in the data store.
     *
     * @param mustBeExact True if victory must be on exact score, false otherwise.
     */
    override suspend fun updateMustWinOnExactScore(mustBeExact: Boolean) {
        settingsDataStore.updateMustWinOnExactScore(mustBeExact)
    }

    /**
     * Updates the 'cancel opponent score on match' setting in the data store.
     *
     * @param cancelOnMatch True if opponent's score should be cancelled on match, false otherwise.
     */
    override suspend fun updateCancelOpponentScoreOnMatch(cancelOnMatch: Boolean) {
        settingsDataStore.updateCancelOpponentScoreOnMatch(cancelOnMatch)
    }

    /**
     * Updates the 'allow fifty point scores' setting in the data store.
     *
     * @param allowFifty True if scores ending in 50 are allowed, false otherwise.
     */
    override suspend fun updateAllowFiftyPointScores(allowFifty: Boolean) {
        settingsDataStore.updateAllowFiftyPointScores(allowFifty)
    }

    /**
     * Updates the 'use three lives rule' setting in the data store.
     *
     * @param useThreeLives True if the three lives rule is active, false otherwise.
     */
    override suspend fun updateUseThreeLivesRule(useThreeLives: Boolean) {
        settingsDataStore.updateUseThreeLivesRule(useThreeLives)
    }

    /**
     * Updates the 'allow steal on pass' setting in the data store.
     *
     * @param allowSteal True if stealing score on pass is allowed, false otherwise.
     */
    override suspend fun updateAllowStealOnPass(allowSteal: Boolean) {
        settingsDataStore.updateAllowStealOnPass(allowSteal)
    }
}

