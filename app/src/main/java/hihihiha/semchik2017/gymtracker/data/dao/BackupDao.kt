package hihihiha.semchik2017.gymtracker.data.dao

import androidx.room.*
import hihihiha.semchik2017.gymtracker.data.model.*

@Dao
interface BackupDao {
    @Query("SELECT * FROM exercises")
    suspend fun getAllExercises(): List<Exercise>

    @Query("SELECT * FROM workouts")
    suspend fun getAllWorkouts(): List<Workout>

    @Query("SELECT * FROM workout_exercises")
    suspend fun getAllWorkoutExercises(): List<WorkoutExercise>

    @Query("SELECT * FROM exercise_sets")
    suspend fun getAllExerciseSets(): List<ExerciseSet>

    @Query("SELECT * FROM body_weights")
    suspend fun getAllBodyWeights(): List<BodyWeight>

    @Query("SELECT * FROM products")
    suspend fun getAllProducts(): List<Product>

    @Query("SELECT * FROM nutrition_days")
    suspend fun getAllNutritionDays(): List<NutritionDay>

    @Query("SELECT * FROM nutrition_entries")
    suspend fun getAllNutritionEntries(): List<NutritionEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(items: List<Exercise>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkouts(items: List<Workout>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutExercises(items: List<WorkoutExercise>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExerciseSets(items: List<ExerciseSet>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBodyWeights(items: List<BodyWeight>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(items: List<Product>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNutritionDays(items: List<NutritionDay>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNutritionEntries(items: List<NutritionEntry>)
}
