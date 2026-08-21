package hihihiha.semchik2017.gymtracker.ui.screens.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hihihiha.semchik2017.gymtracker.data.model.*
import hihihiha.semchik2017.gymtracker.domain.repository.GymRepository
import hihihiha.semchik2017.gymtracker.domain.repository.SettingsRepository
import hihihiha.semchik2017.gymtracker.domain.usecase.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class WorkoutDetailViewModel @Inject constructor(
    private val repository: GymRepository,
    private val settingsRepository: SettingsRepository,
    private val getWeightRecommendationUseCase: GetWeightRecommendationUseCase,
    val calculateStatsUseCase: CalculateStatsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkoutDetailUiState(isLoading = true))
    val uiState: StateFlow<WorkoutDetailUiState> = _uiState.asStateFlow()

    private val _timerSeconds = MutableStateFlow(0)
    val timerSeconds: StateFlow<Int> = _timerSeconds.asStateFlow()

    private val _allExercises = repository.getAllExercises()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allExercises: StateFlow<List<Exercise>> = _allExercises

    private var timerJob: Job? = null

    fun startTimer() {
        timerJob?.cancel()
        viewModelScope.launch {
            val seconds = settingsRepository.restTime.first()
            _timerSeconds.value = seconds
            timerJob = viewModelScope.launch {
                while (_timerSeconds.value > 0) {
                    delay(1000)
                    _timerSeconds.value -= 1
                }
            }
        }
    }

    fun stopTimer() {
        timerJob?.cancel()
        _timerSeconds.value = 0
    }

    fun loadWorkout(workoutId: Long, quiet: Boolean = false) {
        viewModelScope.launch {
            try {
                if (!quiet) _uiState.value = _uiState.value.copy(isLoading = true)
                val data = repository.getWorkoutWithExercises(workoutId)
                
                val newRecs = mutableMapOf<Long, RecommendationResult>()
                // Point 3: Parallel pre-fetching of recommendations
                data.exercises.map { workoutEx ->
                    async {
                        val rec = getWeightRecommendationUseCase(
                            workoutEx.exercise.id, 
                            workoutEx.exercise,
                            data.workout.date
                        )
                        workoutEx.workoutExercise.id to rec
                    }
                }.awaitAll().forEach { (id, rec) ->
                    newRecs[id] = rec
                }
                
                val bodyWeight = repository.getLatestBodyWeight() ?: 0.0
                val unit = settingsRepository.weightUnit.first()

                _uiState.value = _uiState.value.copy(
                    isLoading = false, 
                    workoutWithExercises = data,
                    recommendations = newRecs,
                    bodyWeight = bodyWeight,
                    unit = unit,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message)
            }
        }
    }

    fun updateWorkoutName(newName: String) {
        val workout = _uiState.value.workoutWithExercises?.workout ?: return
        if (workout.isCompleted) return
        viewModelScope.launch {
            try {
                repository.updateWorkout(workout.copy(name = newName))
                loadWorkout(workout.id, quiet = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun completeWorkout() {
        val workout = _uiState.value.workoutWithExercises?.workout ?: return
        if (workout.isCompleted) return
        viewModelScope.launch {
            try {
                repository.updateWorkout(workout.copy(isCompleted = true))
                loadWorkout(workout.id, quiet = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun addExerciseToWorkout(exercise: Exercise) {
        val workoutWithEx = _uiState.value.workoutWithExercises ?: return
        val workoutId = workoutWithEx.workout.id
        viewModelScope.launch {
            try {
                val orderIndex = workoutWithEx.exercises.size
                val workoutExerciseId = repository.insertWorkoutExercise(
                    WorkoutExercise(workoutId = workoutId, exerciseId = exercise.id, orderIndex = orderIndex)
                )
                
                val recommendation = getWeightRecommendationUseCase(exercise.id, exercise, workoutWithEx.workout.date)
                
                if (exercise.laterality == Laterality.BILATERAL) {
                    val weight = (recommendation as? RecommendationResult.Bilateral)?.weight
                    val unit = _uiState.value.unit
                    val wKg = if (unit == "lb") weight?.let { it / 2.20462 } else weight
                    val wLb = if (unit == "lb") weight else weight?.let { it * 2.20462 }
                    repository.insertSet(ExerciseSet(workoutExerciseId = workoutExerciseId, setNumber = 1, weightKg = wKg, weightLb = wLb, reps = 10, side = SetSide.BOTH))
                } else {
                    val res = recommendation as? RecommendationResult.Unilateral
                    val unit = _uiState.value.unit
                    
                    val lKg = if (unit == "lb") res?.leftWeight?.let { it / 2.20462 } else res?.leftWeight
                    val lLb = if (unit == "lb") res?.leftWeight else res?.leftWeight?.let { it * 2.20462 }
                    
                    val rKg = if (unit == "lb") res?.rightWeight?.let { it / 2.20462 } else res?.rightWeight
                    val rLb = if (unit == "lb") res?.rightWeight else res?.rightWeight?.let { it * 2.20462 }

                    repository.insertSet(ExerciseSet(workoutExerciseId = workoutExerciseId, setNumber = 1, weightKg = lKg, weightLb = lLb, reps = 10, side = SetSide.LEFT))
                    repository.insertSet(ExerciseSet(workoutExerciseId = workoutExerciseId, setNumber = 1, weightKg = rKg, weightLb = rLb, reps = 10, side = SetSide.RIGHT))
                }
                loadWorkout(workoutId, quiet = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun updateSet(set: ExerciseSet) {
        viewModelScope.launch {
            try {
                repository.updateSet(set)
                _uiState.value.workoutWithExercises?.workout?.id?.let { loadWorkout(it, quiet = true) }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun completeSet(set: ExerciseSet) {
        viewModelScope.launch {
            try {
                repository.updateSet(set.copy(isCompleted = true))
                startTimer()
                _uiState.value.workoutWithExercises?.workout?.id?.let { loadWorkout(it, quiet = true) }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun deleteSet(set: ExerciseSet) {
        viewModelScope.launch {
            try {
                repository.deleteSet(set)
                _uiState.value.workoutWithExercises?.workout?.id?.let { loadWorkout(it, quiet = true) }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }
    
    fun addSet(workoutExerciseId: Long, side: SetSide) {
         viewModelScope.launch {
            try {
                val currentSets = _uiState.value.workoutWithExercises?.exercises?.find { it.workoutExercise.id == workoutExerciseId }?.sets ?: emptyList()
                val nextNumber = (currentSets.filter { it.side == side }.maxOfOrNull { it.setNumber } ?: 0) + 1
                val lastSet = currentSets.lastOrNull { it.side == side }
                repository.insertSet(
                    ExerciseSet(
                        workoutExerciseId = workoutExerciseId,
                        setNumber = nextNumber,
                        weightKg = lastSet?.weightKg,
                        weightLb = lastSet?.weightLb,
                        reps = lastSet?.reps ?: 10,
                        side = side
                    )
                )
                _uiState.value.workoutWithExercises?.workout?.id?.let { loadWorkout(it, quiet = true) }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }
}
