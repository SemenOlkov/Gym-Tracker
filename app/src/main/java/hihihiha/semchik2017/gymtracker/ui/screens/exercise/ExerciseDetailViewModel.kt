package hihihiha.semchik2017.gymtracker.ui.screens.exercise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hihihiha.semchik2017.gymtracker.data.dao.PRResult
import hihihiha.semchik2017.gymtracker.data.model.*
import hihihiha.semchik2017.gymtracker.domain.repository.GymRepository
import hihihiha.semchik2017.gymtracker.domain.repository.SettingsRepository
import hihihiha.semchik2017.gymtracker.domain.usecase.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExerciseDetailViewModel @Inject constructor(
    private val repository: GymRepository,
    private val settingsRepository: SettingsRepository,
    private val getWeightRecommendationUseCase: GetWeightRecommendationUseCase,
    private val getExerciseStatsUseCase: GetExerciseStatsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExerciseDetailUiState(isLoading = true))
    val uiState: StateFlow<ExerciseDetailUiState> = _uiState.asStateFlow()

    fun loadExerciseDetail(exerciseId: Long) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                val unit = settingsRepository.weightUnit.first()
                val ex = repository.getAllExercises().firstOrNull()?.find { it.id == exerciseId }
                
                if (ex != null) {
                    val recommendation = getWeightRecommendationUseCase(ex.id, ex, System.currentTimeMillis())
                    val stats = getExerciseStatsUseCase(ex.id)
                    
                    val prMap = mutableMapOf<SetSide, PRResult>()
                    if (ex.laterality == Laterality.BILATERAL) {
                        repository.getPersonalRecord(ex.id, SetSide.BOTH)?.let { prMap[SetSide.BOTH] = it }
                    } else {
                        repository.getPersonalRecord(ex.id, SetSide.LEFT)?.let { prMap[SetSide.LEFT] = it }
                        repository.getPersonalRecord(ex.id, SetSide.RIGHT)?.let { prMap[SetSide.RIGHT] = it }
                    }
                    
                    val lastWorkout = repository.getLastWorkoutExerciseWithSets(ex.id, System.currentTimeMillis())
                    val lastDate = lastWorkout?.let {
                        repository.getWorkoutWithExercises(it.workoutExercise.workoutId).workout.date
                    }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        exercise = ex,
                        recommendation = recommendation,
                        stats = stats,
                        prs = prMap,
                        unit = unit,
                        lastWorkoutDate = lastDate,
                        errorMessage = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Упражнение не найдено")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message)
            }
        }
    }
}
