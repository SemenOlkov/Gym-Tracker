package hihihiha.semchik2017.gymtracker.ui.screens.nutrition

import hihihiha.semchik2017.gymtracker.data.model.*

data class NutritionDetailUiState(
    val isLoading: Boolean = false,
    val day: NutritionDay? = null,
    val entries: List<NutritionEntryWithProduct> = emptyList(),
    val searchResults: List<Product> = emptyList(),
    val errorMessage: String? = null
)
