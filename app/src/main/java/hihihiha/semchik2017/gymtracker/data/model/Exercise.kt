package hihihiha.semchik2017.gymtracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val isWeighted: Boolean,
    val progressionType: ProgressionType,
    val laterality: Laterality,
    val isCustom: Boolean,
    val projectileCount: Int = 1,
    val defaultWeightStep: Double = 2.5,
    val muscleGroups: String = "",
    val instructions: String = ""
)
