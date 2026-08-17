package hihihiha.semchik2017.gymtracker.ui.screens.nutrition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hihihiha.semchik2017.gymtracker.data.model.*
import hihihiha.semchik2017.gymtracker.domain.repository.NutritionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NutritionDetailViewModel @Inject constructor(
    private val repository: NutritionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NutritionDetailUiState(isLoading = true))
    val uiState: StateFlow<NutritionDetailUiState> = _uiState.asStateFlow()

    fun loadDayDetails(dayId: Long) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                val allDays = repository.getAllNutritionDays().first()
                val day = allDays.find { it.id == dayId }
                
                if (day != null) {
                    repository.getEntriesForDay(dayId)
                        .catch { e ->
                            _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message)
                        }
                        .collect { entries ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                day = day,
                                entries = entries,
                                errorMessage = null
                            )
                        }
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "День не найден")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message)
            }
        }
    }

    fun searchProducts(query: String) {
        viewModelScope.launch {
            val results = repository.searchProducts(query)
            _uiState.value = _uiState.value.copy(searchResults = results)
        }
    }

    fun addEntry(productId: Long, weight: Double) {
        val day = _uiState.value.day ?: return
        if (day.isClosed) return
        
        viewModelScope.launch {
            try {
                repository.insertNutritionEntry(
                    NutritionEntry(
                        dayId = day.id,
                        productId = productId,
                        weight = weight,
                        timestamp = System.currentTimeMillis()
                    )
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun createAndAddProduct(name: String, cal: Double, pro: Double, fat: Double, carb: Double, weight: Double) {
        val day = _uiState.value.day ?: return
        if (day.isClosed) return
        
        viewModelScope.launch {
            try {
                val productId = repository.insertProduct(
                    Product(name = name, calories = cal, proteins = pro, fats = fat, carbs = carb)
                )
                addEntry(productId, weight)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun deleteEntry(entryId: Long) {
        val day = _uiState.value.day ?: return
        if (day.isClosed) return
        
        viewModelScope.launch {
            try {
                repository.deleteNutritionEntry(entryId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }
}
