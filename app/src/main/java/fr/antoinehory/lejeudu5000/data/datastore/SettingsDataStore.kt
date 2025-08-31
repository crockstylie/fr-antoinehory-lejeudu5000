package fr.antoinehory.lejeudu5000.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import fr.antoinehory.lejeudu5000.domain.model.GameSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Extension property to create the DataStore instance
private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "game_settings")

@Singleton
class SettingsDataStore @Inject constructor(
    // We'll inject Context using Hilt, typically from AppModule
    // For now, let's assume it will be provided.
    // In a real Hilt setup, you'd inject @ApplicationContext context: Context
    private val context: Context
) {

    // Define Preferences Keys
    private object PreferencesKeys {
        val OPENING_SCORE_THRESHOLD = intPreferencesKey("opening_score_threshold")
        val VICTORY_SCORE = intPreferencesKey("victory_score")
        val MUST_WIN_ON_EXACT_SCORE = booleanPreferencesKey("must_win_on_exact_score")
        val CANCEL_OPPONENT_SCORE_ON_MATCH = booleanPreferencesKey("cancel_opponent_score_on_match")
        val ALLOW_FIFTY_POINT_SCORES = booleanPreferencesKey("allow_fifty_point_scores")
        val USE_THREE_LIVES_RULE = booleanPreferencesKey("use_three_lives_rule")
        val ALLOW_STEAL_ON_PASS = booleanPreferencesKey("allow_steal_on_pass")
    }

    val gameSettingsFlow: Flow<GameSettings> = context.settingsDataStore.data
        .map { preferences ->
            GameSettings(
                openingScoreThreshold = preferences[PreferencesKeys.OPENING_SCORE_THRESHOLD] ?: 500,
                victoryScore = preferences[PreferencesKeys.VICTORY_SCORE] ?: 5000,
                mustWinOnExactScore = preferences[PreferencesKeys.MUST_WIN_ON_EXACT_SCORE] ?: false,
                cancelOpponentScoreOnMatch = preferences[PreferencesKeys.CANCEL_OPPONENT_SCORE_ON_MATCH] ?: false,
                allowFiftyPointScores = preferences[PreferencesKeys.ALLOW_FIFTY_POINT_SCORES] ?: false,
                useThreeLivesRule = preferences[PreferencesKeys.USE_THREE_LIVES_RULE] ?: false,
                allowStealOnPass = preferences[PreferencesKeys.ALLOW_STEAL_ON_PASS] ?: false
            )
        }

    suspend fun updateOpeningScoreThreshold(threshold: Int) {
        context.settingsDataStore.edit { preferences ->
            preferences[PreferencesKeys.OPENING_SCORE_THRESHOLD] = threshold
        }
    }

    suspend fun updateVictoryScore(score: Int) {
        context.settingsDataStore.edit { preferences ->
            preferences[PreferencesKeys.VICTORY_SCORE] = score
        }
    }

    suspend fun updateMustWinOnExactScore(mustBeExact: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[PreferencesKeys.MUST_WIN_ON_EXACT_SCORE] = mustBeExact
        }
    }

    suspend fun updateCancelOpponentScoreOnMatch(cancelOnMatch: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[PreferencesKeys.CANCEL_OPPONENT_SCORE_ON_MATCH] = cancelOnMatch
        }
    }

    suspend fun updateAllowFiftyPointScores(allowFifty: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[PreferencesKeys.ALLOW_FIFTY_POINT_SCORES] = allowFifty
        }
    }

    suspend fun updateUseThreeLivesRule(useThreeLives: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[PreferencesKeys.USE_THREE_LIVES_RULE] = useThreeLives
        }
    }

    suspend fun updateAllowStealOnPass(allowSteal: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[PreferencesKeys.ALLOW_STEAL_ON_PASS] = allowSteal
        }
    }
}
