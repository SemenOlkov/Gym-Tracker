package hihihiha.semchik2017.gymtracker.ui.screens.exercise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hihihiha.semchik2017.gymtracker.data.dao.PRResult
import hihihiha.semchik2017.gymtracker.data.model.*
import hihihiha.semchik2017.gymtracker.domain.repository.GymRepository
import hihihiha.semchik2017.gymtracker.domain.usecase.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExerciseDetailViewModel @Inject constructor(
    private val repository: GymRepository,
    private val getWeightRecommendationUseCase: GetWeightRecommendationUseCase,
    private val getExerciseStatsUseCase: GetExerciseStatsUseCase
) : ViewModel() {

    private val _exercise = MutableStateFlow<Exercise?>(null)
    val exercise: StateFlow<Exercise?> = _exercise.asStateFlow()

    private val _recommendation = MutableStateFlow<RecommendationResult>(RecommendationResult.NoData)
    val recommendation: StateFlow<RecommendationResult> = _recommendation.asStateFlow()

    private val _stats = MutableStateFlow<List<ExerciseStatPoint>>(emptyList())
    val stats: StateFlow<List<ExerciseStatPoint>> = _stats.asStateFlow()

    private val _prs = MutableStateFlow<Map<SetSide, PRResult>>(emptyMap())
    val prs: StateFlow<Map<SetSide, PRResult>> = _prs.asStateFlow()

    private val _lastWorkoutDate = MutableStateFlow<Long?>(null)
    val lastWorkoutDate: StateFlow<Long?> = _lastWorkoutDate.asStateFlow()

    fun loadExerciseDetail(exerciseId: Long) {
        viewModelScope.launch {
            val ex = repository.getAllExercises().firstOrNull()?.find { it.id == exerciseId }
            _exercise.value = ex
            
            if (ex != null) {
                _recommendation.value = getWeightRecommendationUseCase(ex.id, ex, System.currentTimeMillis())
                _stats.value = getExerciseStatsUseCase(ex.id)
                
                val prMap = mutableMapOf<SetSide, PRResult>()
                if (ex.laterality == Laterality.BILATERAL) {
                    repository.getPersonalRecord(ex.id, SetSide.BOTH)?.let { prMap[SetSide.BOTH] = it }
                } else {
                    repository.getPersonalRecord(ex.id, SetSide.LEFT)?.let { prMap[SetSide.LEFT] = it }
                    repository.getPersonalRecord(ex.id, SetSide.RIGHT)?.let { prMap[SetSide.RIGHT] = it }
                }
                _prs.value = prMap
                
                val lastWorkout = repository.getLastWorkoutExerciseWithSets(ex.id, System.currentTimeMillis())
                _lastWorkoutDate.value = lastWorkout?.let {
                    repository.getWorkoutWithExercises(it.workoutExercise.workoutId).workout.date
                }
            }
        }
    }
}
