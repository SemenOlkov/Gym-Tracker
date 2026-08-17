package hihihiha.semchik2017.gymtracker.ui.screens.exercise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hihihiha.semchik2017.gymtracker.data.model.*
import hihihiha.semchik2017.gymtracker.domain.repository.GymRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExerciseViewModel @Inject constructor(
    private val repository: GymRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExerciseListUiState(isLoading = true))
    val uiState: StateFlow<ExerciseListUiState> = _uiState.asStateFlow()

    init {
        loadExercises()
    }

    private fun loadExercises() {
        viewModelScope.launch {
            repository.getAllExercises()
                .catch { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message)
                }
                .collect { exercises ->
                    _uiState.value = _uiState.value.copy(isLoading = false, exercises = exercises, errorMessage = null)
                }
        }
    }

    fun createExercise(name: String, isWeighted: Boolean, laterality: Laterality, projectileCount: Int, muscleGroups: String, instructions: String) {
        viewModelScope.launch {
            try {
                val exercise = Exercise(
                    name = name,
                    isWeighted = isWeighted,
                    progressionType = ProgressionType.INCREASE,
                    laterality = laterality,
                    isCustom = true,
                    projectileCount = projectileCount,
                    muscleGroups = muscleGroups,
                    instructions = instructions
                )
                repository.insertExercise(exercise)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun deleteExercise(exercise: Exercise) {
        viewModelScope.launch {
            try {
                repository.deleteExercise(exercise)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }
}
