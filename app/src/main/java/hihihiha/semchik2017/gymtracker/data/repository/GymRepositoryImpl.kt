package hihihiha.semchik2017.gymtracker.data.repository

import hihihiha.semchik2017.gymtracker.data.dao.GymDao
import hihihiha.semchik2017.gymtracker.data.model.*
import hihihiha.semchik2017.gymtracker.domain.repository.GymRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GymRepositoryImpl @Inject constructor(
    private val gymDao: GymDao
) : GymRepository {
    override fun getAllExercises(): Flow<List<Exercise>> = gymDao.getAllExercises()
    override suspend fun insertExercise(exercise: Exercise) = gymDao.insertExercise(exercise)
    override suspend fun deleteExercise(exercise: Exercise) = gymDao.deleteExercise(exercise)

    override fun getAllWorkouts(): Flow<List<Workout>> = gymDao.getAllWorkouts()
    override suspend fun getWorkoutWithExercises(workoutId: Long) = gymDao.getWorkoutWithExercises(workoutId)
    override suspend fun insertWorkout(workout: Workout) = gymDao.insertWorkout(workout)
    override suspend fun updateWorkout(workout: Workout) = gymDao.updateWorkout(workout)
    override suspend fun deleteWorkout(workout: Workout) = gymDao.deleteWorkout(workout)

    override suspend fun insertWorkoutExercise(workoutExercise: WorkoutExercise) = gymDao.insertWorkoutExercise(workoutExercise)
    override suspend fun deleteWorkoutExercise(id: Long) = gymDao.deleteWorkoutExercise(id)

    override suspend fun insertSet(set: ExerciseSet) = gymDao.insertSet(set)
    override suspend fun updateSet(set: ExerciseSet) = gymDao.updateSet(set)
    override suspend fun deleteSet(set: ExerciseSet) = gymDao.deleteSet(set)
    override fun getSetsForExercise(workoutExerciseId: Long) = gymDao.getSetsForExercise(workoutExerciseId)

    override fun getAllBodyWeights(): Flow<List<BodyWeight>> = gymDao.getAllBodyWeights()
    override suspend fun insertBodyWeight(bodyWeight: BodyWeight) = gymDao.insertBodyWeight(bodyWeight)
    override suspend fun deleteBodyWeight(bodyWeight: BodyWeight) = gymDao.deleteBodyWeight(bodyWeight)

    override suspend fun getExerciseSetHistory(exerciseId: Long) = gymDao.getExerciseSetHistory(exerciseId)
    override suspend fun getLastWorkoutExerciseWithSets(exerciseId: Long, beforeDate: Long) = gymDao.getLastWorkoutExerciseWithSets(exerciseId, beforeDate)
    override suspend fun getPersonalRecord(exerciseId: Long, side: SetSide) = gymDao.getPersonalRecord(exerciseId, side)
}
