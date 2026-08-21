package hihihiha.semchik2017.gymtracker.ui.screens.weight

import hihihiha.semchik2017.gymtracker.data.model.BodyWeight

data class WeightUiState(
    val isLoading: Boolean = false,
    val weightHistory: List<BodyWeight> = emptyList(),
    val unit: String = "kg",
    val errorMessage: String? = null
)
