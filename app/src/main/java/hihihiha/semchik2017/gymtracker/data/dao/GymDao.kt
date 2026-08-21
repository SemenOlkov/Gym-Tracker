package hihihiha.semchik2017.gymtracker.data.dao

import androidx.room.*
import hihihiha.semchik2017.gymtracker.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GymDao {
    // Exercises
    @Query("SELECT * FROM exercises")
    fun getAllExercises(): Flow<List<Exercise>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: Exercise): Long

    @Delete
    suspend fun deleteExercise(exercise: Exercise)

    // Workouts
    @Query("SELECT * FROM workouts ORDER BY date DESC")
    fun getAllWorkouts(): Flow<List<Workout>>

    @Transaction
    @Query("SELECT * FROM workouts WHERE id = :workoutId")
    suspend fun getWorkoutWithExercises(workoutId: Long): WorkoutWithExercises

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: Workout): Long

    @Update
    suspend fun updateWorkout(workout: Workout)

    @Delete
    suspend fun deleteWorkout(workout: Workout)

    // WorkoutExercises
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutExercise(workoutExercise: WorkoutExercise): Long

    @Query("DELETE FROM workout_exercises WHERE id = :id")
    suspend fun deleteWorkoutExercise(id: Long)

    // ExerciseSets
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSet(set: ExerciseSet): Long

    @Update
    suspend fun updateSet(set: ExerciseSet)

    @Delete
    suspend fun deleteSet(set: ExerciseSet)

    @Query("SELECT * FROM exercise_sets WHERE workoutExerciseId = :workoutExerciseId")
    fun getSetsForExercise(workoutExerciseId: Long): Flow<List<ExerciseSet>>

    // Body Weight
    @Query("SELECT * FROM body_weights ORDER BY date DESC")
    fun getAllBodyWeights(): Flow<List<BodyWeight>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBodyWeight(bodyWeight: BodyWeight): Long

    @Delete
    suspend fun deleteBodyWeight(bodyWeight: BodyWeight)

    @Query("SELECT weightKg FROM body_weights ORDER BY date DESC LIMIT 1")
    suspend fun getLatestBodyWeight(): Double?

    // Analytics and Recommendations
    @Query("""
        SELECT w.date, s.weightKg as weightKg, s.weightLb, s.reps, s.side FROM exercise_sets s
        INNER JOIN workout_exercises we ON s.workoutExerciseId = we.id
        INNER JOIN workouts w ON we.workoutId = w.id
        WHERE we.exerciseId = :exerciseId AND w.isCompleted = 1
        ORDER BY w.date ASC, s.setNumber ASC
    """)
    suspend fun getExerciseSetHistory(exerciseId: Long): List<ExerciseSetWithDate>

    @Transaction
    @Query("""
        SELECT we.* FROM workout_exercises we
        INNER JOIN workouts w ON we.workoutId = w.id
        WHERE we.exerciseId = :exerciseId AND w.date < :beforeDate AND w.isCompleted = 1
        ORDER BY w.date DESC LIMIT 1
    """)
    suspend fun getLastWorkoutExerciseWithSets(exerciseId: Long, beforeDate: Long): WorkoutExerciseWithSets?

    @Query("SELECT * FROM exercises WHERE id = :id")
    suspend fun getExerciseById(id: Long): Exercise?

    @Query("""
        SELECT s.weightKg as weightKg, s.weightLb as weightLb, w.date FROM exercise_sets s
        INNER JOIN workout_exercises we ON s.workoutExerciseId = we.id
        INNER JOIN workouts w ON we.workoutId = w.id
        INNER JOIN exercises e ON we.exerciseId = e.id
        WHERE we.exerciseId = :exerciseId AND s.weightKg IS NOT NULL AND s.side = :side AND w.isCompleted = 1
        ORDER BY 
            CASE WHEN e.progressionType = 'INCREASE' THEN s.weightKg END DESC,
            CASE WHEN e.progressionType = 'DECREASE' THEN s.weightKg END ASC,
            w.date DESC LIMIT 1
    """)
    suspend fun getPersonalRecord(exerciseId: Long, side: SetSide = SetSide.BOTH): PRResult?
}

data class PRResult(
    val weightKg: Double,
    val weightLb: Double,
    val date: Long
)
