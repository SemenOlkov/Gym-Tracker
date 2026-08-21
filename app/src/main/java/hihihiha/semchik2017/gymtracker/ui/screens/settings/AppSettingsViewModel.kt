package hihihiha.semchik2017.gymtracker.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hihihiha.semchik2017.gymtracker.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppSettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppSettingsUiState())
    val uiState: StateFlow<AppSettingsUiState> = combine(
        settingsRepository.restTime,
        settingsRepository.weightUnit,
        settingsRepository.themeMode,
        settingsRepository.dynamicColors
    ) { restTime, weightUnit, themeMode, dynamicColors ->
        AppSettingsUiState(
            restTime = restTime,
            weightUnit = weightUnit,
            themeMode = themeMode,
            dynamicColors = dynamicColors
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettingsUiState())

    fun updateRestTime(seconds: Int) {
        viewModelScope.launch { settingsRepository.updateRestTime(seconds) }
    }

    fun updateWeightUnit(unit: String) {
        viewModelScope.launch { settingsRepository.updateWeightUnit(unit) }
    }

    fun updateThemeMode(mode: String) {
        viewModelScope.launch { settingsRepository.updateThemeMode(mode) }
    }

    fun updateDynamicColors(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.updateDynamicColors(enabled) }
    }
}

data class AppSettingsUiState(
    val restTime: Int = 180,
    val weightUnit: String = "kg",
    val themeMode: String = "system",
    val dynamicColors: Boolean = true
)
