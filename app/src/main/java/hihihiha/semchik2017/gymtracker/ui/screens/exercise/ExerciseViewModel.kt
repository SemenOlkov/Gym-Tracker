package hihihiha.semchik2017.gymtracker.ui.screens.exercise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hihihiha.semchik2017.gymtracker.data.model.*
import hihihiha.semchik2017.gymtracker.domain.repository.GymRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExerciseViewModel @Inject constructor(
    private val repository: GymRepository
) : ViewModel() {

    val allExercises: StateFlow<List<Exercise>> = repository.getAllExercises()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createExercise(name: String, isWeighted: Boolean, laterality: Laterality, muscleGroups: String, instructions: String) {
        viewModelScope.launch {
            val exercise = Exercise(
                name = name,
                isWeighted = isWeighted,
                progressionType = ProgressionType.INCREASE,
                laterality = laterality,
                isCustom = true,
                muscleGroups = muscleGroups,
                instructions = instructions
            )
            repository.insertExercise(exercise)
        }
    }

    fun deleteExercise(exercise: Exercise) {
        viewModelScope.launch {
            repository.deleteExercise(exercise)
        }
    }
}
