package hihihiha.semchik2017.gymtracker.ui.screens.weight

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hihihiha.semchik2017.gymtracker.data.model.BodyWeight
import hihihiha.semchik2017.gymtracker.domain.repository.GymRepository
import hihihiha.semchik2017.gymtracker.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeightViewModel @Inject constructor(
    private val repository: GymRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeightUiState(isLoading = true))
    val uiState: StateFlow<WeightUiState> = _uiState.asStateFlow()

    init {
        loadWeightHistory()
    }

    private fun loadWeightHistory() {
        viewModelScope.launch {
            combine(
                repository.getAllBodyWeights(),
                settingsRepository.weightUnit
            ) { history, unit ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false, 
                    weightHistory = history, 
                    unit = unit,
                    errorMessage = null
                )
            }.catch { e ->
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message)
            }.collect()
        }
    }

    fun addWeight(value: Double) {
        viewModelScope.launch {
            try {
                val unit = settingsRepository.weightUnit.first()
                val wKg = if (unit == "lb") value / 2.20462 else value
                val wLb = if (unit == "lb") value else value * 2.20462
                repository.insertBodyWeight(BodyWeight(date = System.currentTimeMillis(), weightKg = wKg, weightLb = wLb))
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun deleteWeight(bodyWeight: BodyWeight) {
        viewModelScope.launch {
            try {
                repository.deleteBodyWeight(bodyWeight)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }
}
