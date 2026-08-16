package hihihiha.semchik2017.gymtracker.ui.screens.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hihihiha.semchik2017.gymtracker.data.model.*
import hihihiha.semchik2017.gymtracker.domain.repository.GymRepository
import hihihiha.semchik2017.gymtracker.domain.usecase.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class WorkoutDetailViewModel @Inject constructor(
    private val repository: GymRepository,
    private val getWeightRecommendationUseCase: GetWeightRecommendationUseCase,
    val calculateStatsUseCase: CalculateStatsUseCase
) : ViewModel() {

    private val _workoutWithExercises = MutableStateFlow<WorkoutWithExercises?>(null)
    val workoutWithExercises: StateFlow<WorkoutWithExercises?> = _workoutWithExercises.asStateFlow()

    private val _recommendations = MutableStateFlow<Map<Long, RecommendationResult>>(emptyMap())
    val recommendations: StateFlow<Map<Long, RecommendationResult>> = _recommendations.asStateFlow()

    private val _timerSeconds = MutableStateFlow(0)
    val timerSeconds: StateFlow<Int> = _timerSeconds.asStateFlow()

    private val _allExercises = repository.getAllExercises()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allExercises: StateFlow<List<Exercise>> = _allExercises

    private var timerJob: Job? = null

    fun startTimer() {
        timerJob?.cancel()
        _timerSeconds.value = 180
        timerJob = viewModelScope.launch {
            while (_timerSeconds.value > 0) {
                delay(1000)
                _timerSeconds.value -= 1
            }
        }
    }

    fun stopTimer() {
        timerJob?.cancel()
        _timerSeconds.value = 0
    }

    fun loadWorkout(workoutId: Long) {
        viewModelScope.launch {
            val data = repository.getWorkoutWithExercises(workoutId)
            _workoutWithExercises.value = data
            
            val newRecs = mutableMapOf<Long, RecommendationResult>()
            data.exercises.forEach { workoutEx ->
                val rec = getWeightRecommendationUseCase(
                    workoutEx.exercise.id, 
                    workoutEx.exercise,
                    data.workout.date
                )
                newRecs[workoutEx.workoutExercise.id] = rec
            }
            _recommendations.value = newRecs
        }
    }

    fun updateWorkoutName(newName: String) {
        val workout = _workoutWithExercises.value?.workout ?: return
        if (workout.isCompleted) return
        viewModelScope.launch {
            repository.updateWorkout(workout.copy(name = newName))
            loadWorkout(workout.id)
        }
    }

    fun completeWorkout() {
        val workout = _workoutWithExercises.value?.workout ?: return
        if (workout.isCompleted) return
        viewModelScope.launch {
            repository.updateWorkout(workout.copy(isCompleted = true))
            loadWorkout(workout.id)
        }
    }

    fun addExerciseToWorkout(exercise: Exercise) {
        val workoutWithEx = _workoutWithExercises.value ?: return
        val workoutId = workoutWithEx.workout.id
        viewModelScope.launch {
            val orderIndex = workoutWithEx.exercises.size
            val workoutExerciseId = repository.insertWorkoutExercise(
                WorkoutExercise(workoutId = workoutId, exerciseId = exercise.id, orderIndex = orderIndex)
            )
            
            val recommendation = getWeightRecommendationUseCase(exercise.id, exercise, workoutWithEx.workout.date)
            
            if (exercise.laterality == Laterality.BILATERAL) {
                val weight = (recommendation as? RecommendationResult.Bilateral)?.weight
                repository.insertSet(ExerciseSet(workoutExerciseId = workoutExerciseId, setNumber = 1, weight = weight, reps = 10, side = SetSide.BOTH))
            } else {
                val res = recommendation as? RecommendationResult.Unilateral
                repository.insertSet(ExerciseSet(workoutExerciseId = workoutExerciseId, setNumber = 1, weight = res?.leftWeight, reps = 10, side = SetSide.LEFT))
                repository.insertSet(ExerciseSet(workoutExerciseId = workoutExerciseId, setNumber = 1, weight = res?.rightWeight, reps = 10, side = SetSide.RIGHT))
            }
            loadWorkout(workoutId)
        }
    }

    fun updateSet(set: ExerciseSet) {
        viewModelScope.launch {
            repository.updateSet(set)
            _workoutWithExercises.value?.workout?.id?.let { loadWorkout(it) }
        }
    }

    fun completeSet(set: ExerciseSet) {
        viewModelScope.launch {
            repository.updateSet(set.copy(isCompleted = true))
            startTimer()
            _workoutWithExercises.value?.workout?.id?.let { loadWorkout(it) }
        }
    }

    fun deleteSet(set: ExerciseSet) {
        viewModelScope.launch {
            repository.deleteSet(set)
            _workoutWithExercises.value?.workout?.id?.let { loadWorkout(it) }
        }
    }
    
    fun addSet(workoutExerciseId: Long, side: SetSide) {
         viewModelScope.launch {
            val currentSets = _workoutWithExercises.value?.exercises?.find { it.workoutExercise.id == workoutExerciseId }?.sets ?: emptyList()
            val nextNumber = (currentSets.filter { it.side == side }.maxOfOrNull { it.setNumber } ?: 0) + 1
            repository.insertSet(
                ExerciseSet(
                    workoutExerciseId = workoutExerciseId,
                    setNumber = nextNumber,
                    weight = currentSets.lastOrNull { it.side == side }?.weight,
                    reps = currentSets.lastOrNull { it.side == side }?.reps ?: 10,
                    side = side
                )
            )
            _workoutWithExercises.value?.workout?.id?.let { loadWorkout(it) }
        }
    }
}
