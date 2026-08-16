package hihihiha.semchik2017.gymtracker.ui.screens.exercise

import hihihiha.semchik2017.gymtracker.data.model.Exercise

data class ExerciseListUiState(
    val isLoading: Boolean = false,
    val exercises: List<Exercise> = emptyList(),
    val errorMessage: String? = null
)
