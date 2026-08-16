package hihihiha.semchik2017.gymtracker.ui.screens.workout

import hihihiha.semchik2017.gymtracker.data.model.Workout

data class WorkoutListUiState(
    val isLoading: Boolean = false,
    val groupedWorkouts: Map<String, List<Workout>> = emptyMap(),
    val errorMessage: String? = null
)
