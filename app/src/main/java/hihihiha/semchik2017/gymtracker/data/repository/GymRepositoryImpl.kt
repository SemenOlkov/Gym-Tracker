package hihihiha.semchik2017.gymtracker.data.repository

import hihihiha.semchik2017.gymtracker.data.dao.GymDao
import hihihiha.semchik2017.gymtracker.data.model.*
import hihihiha.semchik2017.gymtracker.domain.repository.GymRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GymRepositoryImpl @Inject constructor(
    private val gymDao: GymDao
) : GymRepository {
    
    private fun <T> wrap(action: suspend () -> T): suspend () -> T = action // Placeholder for global error wrapping if needed

    override fun getAllExercises(): Flow<List<Exercise>> = gymDao.getAllExercises()
    
    override suspend fun insertExercise(exercise: Exercise) = try {
        gymDao.insertExercise(exercise)
    } catch (e: Exception) {
        -1L
    }

    override suspend fun deleteExercise(exercise: Exercise) {
        try { gymDao.deleteExercise(exercise) } catch (e: Exception) {}
    }

    override fun getAllWorkouts(): Flow<List<Workout>> = gymDao.getAllWorkouts()
    
    override suspend fun getWorkoutWithExercises(workoutId: Long) = gymDao.getWorkoutWithExercises(workoutId)
    
    override suspend fun insertWorkout(workout: Workout) = try {
        gymDao.insertWorkout(workout)
    } catch (e: Exception) {
        -1L
    }

    override suspend fun updateWorkout(workout: Workout) {
        try { gymDao.updateWorkout(workout) } catch (e: Exception) {}
    }

    override suspend fun deleteWorkout(workout: Workout) {
        try { gymDao.deleteWorkout(workout) } catch (e: Exception) {}
    }

    override suspend fun insertWorkoutExercise(workoutExercise: WorkoutExercise) = try {
        gymDao.insertWorkoutExercise(workoutExercise)
    } catch (e: Exception) {
        -1L
    }

    override suspend fun deleteWorkoutExercise(id: Long) {
        try { gymDao.deleteWorkoutExercise(id) } catch (e: Exception) {}
    }

    override suspend fun insertSet(set: ExerciseSet) = try {
        gymDao.insertSet(set)
    } catch (e: Exception) {
        -1L
    }

    override suspend fun updateSet(set: ExerciseSet) {
        try { gymDao.updateSet(set) } catch (e: Exception) {}
    }

    override suspend fun deleteSet(set: ExerciseSet) {
        try { gymDao.deleteSet(set) } catch (e: Exception) {}
    }

    override fun getSetsForExercise(workoutExerciseId: Long) = gymDao.getSetsForExercise(workoutExerciseId)

    override fun getAllBodyWeights(): Flow<List<BodyWeight>> = gymDao.getAllBodyWeights()
    
    override suspend fun insertBodyWeight(bodyWeight: BodyWeight) = try {
        gymDao.insertBodyWeight(bodyWeight)
    } catch (e: Exception) {
        -1L
    }

    override suspend fun deleteBodyWeight(bodyWeight: BodyWeight) {
        try { gymDao.deleteBodyWeight(bodyWeight) } catch (e: Exception) {}
    }

    override suspend fun getExerciseSetHistory(exerciseId: Long) = gymDao.getExerciseSetHistory(exerciseId)
    
    override suspend fun getLastWorkoutExerciseWithSets(exerciseId: Long, beforeDate: Long) = 
        gymDao.getLastWorkoutExerciseWithSets(exerciseId, beforeDate)
        
    override suspend fun getPersonalRecord(exerciseId: Long, side: SetSide) = 
        gymDao.getPersonalRecord(exerciseId, side)
}
