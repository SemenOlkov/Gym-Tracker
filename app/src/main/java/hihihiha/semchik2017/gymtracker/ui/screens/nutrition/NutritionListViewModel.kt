package hihihiha.semchik2017.gymtracker.ui.screens.nutrition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hihihiha.semchik2017.gymtracker.data.model.NutritionDay
import hihihiha.semchik2017.gymtracker.domain.repository.NutritionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class NutritionListViewModel @Inject constructor(
    private val repository: NutritionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NutritionListUiState(isLoading = true))
    val uiState: StateFlow<NutritionListUiState> = _uiState.asStateFlow()

    init {
        checkAndCreateToday()
        loadNutritionDays()
    }

    private fun checkAndCreateToday() {
        viewModelScope.launch {
            val todayStart = getStartOfDay(System.currentTimeMillis())
            val existing = repository.getNutritionDayByDate(todayStart)
            if (existing == null) {
                repository.insertNutritionDay(NutritionDay(date = todayStart))
            }
            
            // Auto-close past days
            repository.getAllNutritionDays().first().forEach { day ->
                if (!day.isClosed && day.date < todayStart) {
                    repository.updateNutritionDay(day.copy(isClosed = true))
                }
            }
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private fun loadNutritionDays() {
        viewModelScope.launch {
            repository.getAllNutritionDays()
                .flatMapLatest { days ->
                    val flows = days.map { day ->
                        repository.getEntriesForDay(day.id).map { entries ->
                            NutritionDayWithSummary(
                                day = day,
                                totalCalories = entries.sumOf { it.calories },
                                totalProteins = entries.sumOf { it.proteins },
                                totalFats = entries.sumOf { it.fats },
                                totalCarbs = entries.sumOf { it.carbs }
                            )
                        }
                    }
                    if (flows.isEmpty()) flowOf(emptyList())
                    else combine(flows) { it.toList() }
                }
                .catch { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message)
                }
                .collect { summarizedDays ->
                    _uiState.value = _uiState.value.copy(isLoading = false, days = summarizedDays)
                }
        }
    }

    fun setPeriod(period: NutritionPeriod) {
        _uiState.value = _uiState.value.copy(selectedPeriod = period)
    }

    private fun getStartOfDay(timestamp: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
