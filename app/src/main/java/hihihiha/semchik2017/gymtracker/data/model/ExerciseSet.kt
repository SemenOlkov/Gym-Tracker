package hihihiha.semchik2017.gymtracker.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(
    tableName = "exercise_sets",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutExercise::class,
            parentColumns = ["id"],
            childColumns = ["workoutExerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
@Serializable
data class ExerciseSet(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workoutExerciseId: Long,
    val setNumber: Int,
    val weightKg: Double?,
    val weightLb: Double?,
    val reps: Int,
    val side: SetSide,
    val isCompleted: Boolean = false,
    val note: String? = null
) {
    // Legacy support or helper
    val weight: Double? get() = weightKg 
}
