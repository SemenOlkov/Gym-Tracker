package hihihiha.semchik2017.gymtracker.domain.repository

import hihihiha.semchik2017.gymtracker.data.model.*
import kotlinx.coroutines.flow.Flow

interface NutritionRepository {
    fun getAllNutritionDays(): Flow<List<NutritionDay>>
    suspend fun getNutritionDayByDate(date: Long): NutritionDay?
    suspend fun insertNutritionDay(day: NutritionDay): Long
    suspend fun updateNutritionDay(day: NutritionDay)
    
    fun getEntriesForDay(dayId: Long): Flow<List<NutritionEntryWithProduct>>
    suspend fun insertNutritionEntry(entry: NutritionEntry)
    suspend fun deleteNutritionEntry(id: Long)
    
    fun getAllProducts(): Flow<List<Product>>
    suspend fun insertProduct(product: Product): Long
    suspend fun searchProducts(query: String): List<Product>
}
