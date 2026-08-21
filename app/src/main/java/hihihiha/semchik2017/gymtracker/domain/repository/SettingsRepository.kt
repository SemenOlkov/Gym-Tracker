package hihihiha.semchik2017.gymtracker.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val restTime: Flow<Int>
    val weightUnit: Flow<String>
    val themeMode: Flow<String>
    val dynamicColors: Flow<Boolean>

    suspend fun updateRestTime(seconds: Int)
    suspend fun updateWeightUnit(unit: String)
    suspend fun updateThemeMode(mode: String)
    suspend fun updateDynamicColors(enabled: Boolean)
}
