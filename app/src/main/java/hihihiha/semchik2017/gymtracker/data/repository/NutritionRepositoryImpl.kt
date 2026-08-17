package hihihiha.semchik2017.gymtracker.data.repository

import hihihiha.semchik2017.gymtracker.data.dao.NutritionDao
import hihihiha.semchik2017.gymtracker.data.model.*
import hihihiha.semchik2017.gymtracker.domain.repository.NutritionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NutritionRepositoryImpl @Inject constructor(
    private val nutritionDao: NutritionDao
) : NutritionRepository {
    override fun getAllNutritionDays() = nutritionDao.getAllNutritionDays()
    override suspend fun getNutritionDayByDate(date: Long) = nutritionDao.getNutritionDayByDate(date)
    override suspend fun insertNutritionDay(day: NutritionDay) = nutritionDao.insertNutritionDay(day)
    override suspend fun updateNutritionDay(day: NutritionDay) = nutritionDao.updateNutritionDay(day)
    
    override fun getEntriesForDay(dayId: Long) = nutritionDao.getEntriesForDay(dayId)
    override suspend fun insertNutritionEntry(entry: NutritionEntry) = nutritionDao.insertNutritionEntry(entry)
    override suspend fun deleteNutritionEntry(id: Long) = nutritionDao.deleteNutritionEntry(id)
    
    override fun getAllProducts() = nutritionDao.getAllProducts()
    override suspend fun insertProduct(product: Product) = nutritionDao.insertProduct(product)
    override suspend fun searchProducts(query: String) = nutritionDao.searchProducts(query)
}
