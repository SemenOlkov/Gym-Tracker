package hihihiha.semchik2017.gymtracker.ui.screens.weight

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hihihiha.semchik2017.gymtracker.data.model.BodyWeight
import hihihiha.semchik2017.gymtracker.domain.repository.GymRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeightViewModel @Inject constructor(
    private val repository: GymRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeightUiState(isLoading = true))
    val uiState: StateFlow<WeightUiState> = _uiState.asStateFlow()

    init {
        loadWeightHistory()
    }

    private fun loadWeightHistory() {
        viewModelScope.launch {
            repository.getAllBodyWeights()
                .catch { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message)
                }
                .collect { history ->
                    _uiState.value = _uiState.value.copy(isLoading = false, weightHistory = history, errorMessage = null)
                }
        }
    }

    fun addWeight(weight: Double) {
        viewModelScope.launch {
            try {
                repository.insertBodyWeight(BodyWeight(date = System.currentTimeMillis(), weight = weight))
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
