package hihihiha.semchik2017.gymtracker.domain.repository

import hihihiha.semchik2017.gymtracker.data.dao.PRResult
import hihihiha.semchik2017.gymtracker.data.model.*
import kotlinx.coroutines.flow.Flow

interface GymRepository {
    fun getAllExercises(): Flow<List<Exercise>>
    suspend fun getExerciseById(id: Long): Exercise?
    suspend fun insertExercise(exercise: Exercise): Long
    suspend fun deleteExercise(exercise: Exercise)

    fun getAllWorkouts(): Flow<List<Workout>>
    suspend fun getWorkoutWithExercises(workoutId: Long): WorkoutWithExercises
    suspend fun insertWorkout(workout: Workout): Long
    suspend fun updateWorkout(workout: Workout)
    suspend fun deleteWorkout(workout: Workout)

    suspend fun insertWorkoutExercise(workoutExercise: WorkoutExercise): Long
    suspend fun deleteWorkoutExercise(id: Long)

    suspend fun insertSet(set: ExerciseSet): Long
    suspend fun updateSet(set: ExerciseSet)
    suspend fun deleteSet(set: ExerciseSet)
    fun getSetsForExercise(workoutExerciseId: Long): Flow<List<ExerciseSet>>

    fun getAllBodyWeights(): Flow<List<BodyWeight>>
    suspend fun insertBodyWeight(bodyWeight: BodyWeight): Long
    suspend fun deleteBodyWeight(bodyWeight: BodyWeight)
    suspend fun getLatestBodyWeight(): Double?

    suspend fun getExerciseSetHistory(exerciseId: Long): List<ExerciseSetWithDate>
    suspend fun getLastWorkoutExerciseWithSets(exerciseId: Long, beforeDate: Long): WorkoutExerciseWithSets?
    suspend fun getPersonalRecord(exerciseId: Long, side: SetSide = SetSide.BOTH): PRResult?
}
