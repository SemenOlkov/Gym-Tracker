package hihihiha.semchik2017.gymtracker.ui.screens.nutrition

import hihihiha.semchik2017.gymtracker.data.model.NutritionDay
import hihihiha.semchik2017.gymtracker.data.model.NutritionEntryWithProduct

data class NutritionListUiState(
    val isLoading: Boolean = false,
    val days: List<NutritionDayWithSummary> = emptyList(),
    val selectedPeriod: NutritionPeriod = NutritionPeriod.WEEK,
    val errorMessage: String? = null
)

enum class NutritionPeriod {
    WEEK, MONTH, YEAR
}

data class NutritionDayWithSummary(
    val day: NutritionDay,
    val totalCalories: Double = 0.0,
    val totalProteins: Double = 0.0,
    val totalFats: Double = 0.0,
    val totalCarbs: Double = 0.0
)
