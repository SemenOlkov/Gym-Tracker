package hihihiha.semchik2017.gymtracker.data.repository

import androidx.room.withTransaction
import hihihiha.semchik2017.gymtracker.data.GymDatabase
import hihihiha.semchik2017.gymtracker.data.dao.BackupDao
import hihihiha.semchik2017.gymtracker.data.model.AppBackup
import hihihiha.semchik2017.gymtracker.domain.repository.BackupRepository
import javax.inject.Inject

class BackupRepositoryImpl @Inject constructor(
    private val database: GymDatabase,
    private val backupDao: BackupDao
) : BackupRepository {

    override suspend fun createBackup(): AppBackup {
        return AppBackup(
            version = 7,
            timestamp = System.currentTimeMillis(),
            exercises = backupDao.getAllExercises(),
            workouts = backupDao.getAllWorkouts(),
            workoutExercises = backupDao.getAllWorkoutExercises(),
            exerciseSets = backupDao.getAllExerciseSets(),
            bodyWeights = backupDao.getAllBodyWeights(),
            products = backupDao.getAllProducts(),
            nutritionDays = backupDao.getAllNutritionDays(),
            nutritionEntries = backupDao.getAllNutritionEntries()
        )
    }

    override suspend fun restoreBackup(backup: AppBackup) {
        database.withTransaction {
            database.clearAllTables()
            
            backupDao.insertExercises(backup.exercises)
            backupDao.insertWorkouts(backup.workouts)
            backupDao.insertWorkoutExercises(backup.workoutExercises)
            backupDao.insertExerciseSets(backup.exerciseSets)
            backupDao.insertBodyWeights(backup.bodyWeights)
            backupDao.insertProducts(backup.products)
            backupDao.insertNutritionDays(backup.nutritionDays)
            backupDao.insertNutritionEntries(backup.nutritionEntries)
        }
    }
}
