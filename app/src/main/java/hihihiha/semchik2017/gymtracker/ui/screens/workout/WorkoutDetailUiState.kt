package hihihiha.semchik2017.gymtracker.ui.screens.workout

import hihihiha.semchik2017.gymtracker.data.model.WorkoutWithExercises
import hihihiha.semchik2017.gymtracker.domain.usecase.RecommendationResult

data class WorkoutDetailUiState(
    val isLoading: Boolean = false,
    val workoutWithExercises: WorkoutWithExercises? = null,
    val recommendations: Map<Long, RecommendationResult> = emptyMap(),
    val bodyWeight: Double = 0.0,
    val unit: String = "kg",
    val errorMessage: String? = null
)
