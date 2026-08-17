package hihihiha.semchik2017.gymtracker.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val calories: Double, // per 100g
    val proteins: Double, // per 100g
    val fats: Double,     // per 100g
    val carbs: Double     // per 100g
)

@Entity(tableName = "nutrition_days")
data class NutritionDay(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long, // Start of the day in millis
    val isClosed: Boolean = false
)

@Entity(
    tableName = "nutrition_entries",
    foreignKeys = [
        ForeignKey(
            entity = NutritionDay::class,
            parentColumns = ["id"],
            childColumns = ["dayId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Product::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class NutritionEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dayId: Long,
    val productId: Long,
    val weight: Double, // in grams
    val timestamp: Long
)

data class NutritionEntryWithProduct(
    val entryId: Long,
    val productId: Long,
    val productName: String,
    val weight: Double,
    val calories: Double,
    val proteins: Double,
    val fats: Double,
    val carbs: Double,
    val timestamp: Long
)
