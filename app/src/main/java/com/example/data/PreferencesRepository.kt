package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.decoDataStore: DataStore<Preferences> by preferencesDataStore(name = "deco_lab_prefs")

data class UserProgressState(
    val isDarkTheme: Boolean = true,
    val bestScores: Map<Int, Int> = emptyMap(),
    val attemptsCount: Int = 0
) {
    val totalBestScore: Int get() = (1..5).sumOf { bestScores[it] ?: 0 }
    val maxTotalScore: Int get() = 25
}

class PreferencesRepository(private val context: Context) {

    private object Keys {
        val DARK_THEME = booleanPreferencesKey("is_dark_theme")
        val ATTEMPTS_COUNT = intPreferencesKey("attempts_count")
        fun unitScore(unitId: Int) = intPreferencesKey("unit_score_$unitId")
    }

    val progressFlow: Flow<UserProgressState> = context.decoDataStore.data.map { prefs ->
        val scores = (1..5).associateWith { unitId ->
            prefs[Keys.unitScore(unitId)] ?: 0
        }
        UserProgressState(
            isDarkTheme = prefs[Keys.DARK_THEME] ?: true,
            bestScores = scores,
            attemptsCount = prefs[Keys.ATTEMPTS_COUNT] ?: 0
        )
    }

    suspend fun setDarkTheme(isDark: Boolean) {
        context.decoDataStore.edit { prefs ->
            prefs[Keys.DARK_THEME] = isDark
        }
    }

    suspend fun saveQuizScore(unitId: Int, score: Int) {
        context.decoDataStore.edit { prefs ->
            val currentBest = prefs[Keys.unitScore(unitId)] ?: 0
            if (score > currentBest) {
                prefs[Keys.unitScore(unitId)] = score
            }
            prefs[Keys.ATTEMPTS_COUNT] = (prefs[Keys.ATTEMPTS_COUNT] ?: 0) + 1
        }
    }

    suspend fun resetAllScores() {
        context.decoDataStore.edit { prefs ->
            (1..5).forEach { unitId ->
                prefs[Keys.unitScore(unitId)] = 0
            }
            prefs[Keys.ATTEMPTS_COUNT] = 0
        }
    }
}
