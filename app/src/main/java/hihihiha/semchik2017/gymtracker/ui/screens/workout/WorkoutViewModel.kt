package hihihiha.semchik2017.gymtracker.ui.screens.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hihihiha.semchik2017.gymtracker.data.model.Workout
import hihihiha.semchik2017.gymtracker.domain.repository.GymRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val repository: GymRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkoutListUiState(isLoading = true))
    val uiState: StateFlow<WorkoutListUiState> = _uiState.asStateFlow()

    init {
        loadWorkouts()
    }

    private fun loadWorkouts() {
        viewModelScope.launch {
            repository.getAllWorkouts()
                .catch { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message)
                }
                .collect { workouts ->
                    val grouped = workouts.groupBy { workout ->
                        val cal = Calendar.getInstance().apply { timeInMillis = workout.date }
                        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)
                    }
                    _uiState.value = _uiState.value.copy(isLoading = false, groupedWorkouts = grouped, errorMessage = null)
                }
        }
    }

    fun createWorkout(name: String) {
        viewModelScope.launch {
            try {
                val workout = Workout(
                    date = System.currentTimeMillis(),
                    name = name.ifBlank { null }
                )
                repository.insertWorkout(workout)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun deleteWorkout(workout: Workout) {
        viewModelScope.launch {
            try {
                repository.deleteWorkout(workout)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }
}
