package hihihiha.semchik2017.gymtracker.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import hihihiha.semchik2017.gymtracker.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {

    private object PreferencesKeys {
        val REST_TIME = intPreferencesKey("rest_time")
        val WEIGHT_UNIT = stringPreferencesKey("weight_unit")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLORS = booleanPreferencesKey("dynamic_colors")
    }

    override val restTime: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.REST_TIME] ?: 180
    }

    override val weightUnit: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.WEIGHT_UNIT] ?: "kg"
    }

    override val themeMode: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.THEME_MODE] ?: "system"
    }

    override val dynamicColors: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DYNAMIC_COLORS] ?: true
    }

    override suspend fun updateRestTime(seconds: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.REST_TIME] = seconds
        }
    }

    override suspend fun updateWeightUnit(unit: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.WEIGHT_UNIT] = unit
        }
    }

    override suspend fun updateThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode
        }
    }

    override suspend fun updateDynamicColors(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DYNAMIC_COLORS] = enabled
        }
    }
}
