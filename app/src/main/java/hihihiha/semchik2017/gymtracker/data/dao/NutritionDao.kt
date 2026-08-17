package hihihiha.semchik2017.gymtracker.data.dao

import androidx.room.*
import hihihiha.semchik2017.gymtracker.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NutritionDao {
    @Query("SELECT * FROM nutrition_days ORDER BY date DESC")
    fun getAllNutritionDays(): Flow<List<NutritionDay>>

    @Query("SELECT * FROM nutrition_days WHERE date = :date LIMIT 1")
    suspend fun getNutritionDayByDate(date: Long): NutritionDay?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNutritionDay(day: NutritionDay): Long

    @Update
    suspend fun updateNutritionDay(day: NutritionDay)

    @Query("""
        SELECT e.id as entryId, p.id as productId, p.name as productName, e.weight,
        (p.calories * e.weight / 100.0) as calories,
        (p.proteins * e.weight / 100.0) as proteins,
        (p.fats * e.weight / 100.0) as fats,
        (p.carbs * e.weight / 100.0) as carbs,
        e.timestamp
        FROM nutrition_entries e
        JOIN products p ON e.productId = p.id
        WHERE e.dayId = :dayId
        ORDER BY e.timestamp ASC
    """)
    fun getEntriesForDay(dayId: Long): Flow<List<NutritionEntryWithProduct>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNutritionEntry(entry: NutritionEntry)

    @Query("DELETE FROM nutrition_entries WHERE id = :id")
    suspend fun deleteNutritionEntry(id: Long)

    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<Product>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long

    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%'")
    suspend fun searchProducts(query: String): List<Product>
}
