package hihihiha.semchik2017.gymtracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "workouts")
@Serializable
data class Workout(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long,
    val name: String? = null,
    val isCompleted: Boolean = false
)
