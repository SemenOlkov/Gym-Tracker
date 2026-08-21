package hihihiha.semchik2017.gymtracker.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(
    tableName = "workout_exercises",
    foreignKeys = [
        ForeignKey(
            entity = Workout::class,
            parentColumns = ["id"],
            childColumns = ["workoutId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
@Serializable
data class WorkoutExercise(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workoutId: Long,
    val exerciseId: Long,
    val orderIndex: Int
)
