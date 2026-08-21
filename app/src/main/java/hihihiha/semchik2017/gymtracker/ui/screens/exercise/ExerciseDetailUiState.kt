package hihihiha.semchik2017.gymtracker.ui.screens.exercise

import hihihiha.semchik2017.gymtracker.data.dao.PRResult
import hihihiha.semchik2017.gymtracker.data.model.Exercise
import hihihiha.semchik2017.gymtracker.data.model.SetSide
import hihihiha.semchik2017.gymtracker.domain.usecase.ExerciseStatPoint
import hihihiha.semchik2017.gymtracker.domain.usecase.RecommendationResult

data class ExerciseDetailUiState(
    val isLoading: Boolean = false,
    val exercise: Exercise? = null,
    val recommendation: RecommendationResult = RecommendationResult.NoData,
    val stats: List<ExerciseStatPoint> = emptyList(),
    val prs: Map<SetSide, PRResult> = emptyMap(),
    val unit: String = "kg",
    val lastWorkoutDate: Long? = null,
    val errorMessage: String? = null
)
