package hihihiha.semchik2017.gymtracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "body_weights")
@Serializable
data class BodyWeight(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long,
    val weightKg: Double,
    val weightLb: Double
)
