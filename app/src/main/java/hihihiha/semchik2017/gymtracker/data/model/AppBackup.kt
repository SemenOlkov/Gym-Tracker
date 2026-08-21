package hihihiha.semchik2017.gymtracker.data.model

import kotlinx.serialization.Serializable

@Serializable
data class AppBackup(
    val version: Int,
    val timestamp: Long,
    val exercises: List<Exercise>,
    val workouts: List<Workout>,
    val workoutExercises: List<WorkoutExercise>,
    val exerciseSets: List<ExerciseSet>,
    val bodyWeights: List<BodyWeight>,
    val products: List<Product>,
    val nutritionDays: List<NutritionDay>,
    val nutritionEntries: List<NutritionEntry>
)
