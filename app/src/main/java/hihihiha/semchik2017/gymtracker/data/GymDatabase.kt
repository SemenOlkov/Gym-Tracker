package hihihiha.semchik2017.gymtracker.data

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import hihihiha.semchik2017.gymtracker.R
import hihihiha.semchik2017.gymtracker.data.dao.GymDao
import hihihiha.semchik2017.gymtracker.data.dao.NutritionDao
import hihihiha.semchik2017.gymtracker.data.dao.BackupDao
import hihihiha.semchik2017.gymtracker.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Database(
    entities = [
        Exercise::class, Workout::class, WorkoutExercise::class, ExerciseSet::class, BodyWeight::class,
        Product::class, NutritionDay::class, NutritionEntry::class
    ],
    version = 7,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class GymDatabase : RoomDatabase() {
    abstract fun gymDao(): GymDao
    abstract fun nutritionDao(): NutritionDao
    abstract fun backupDao(): BackupDao

    companion object {
        @Volatile
        private var INSTANCE: GymDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): GymDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GymDatabase::class.java,
                    "gym_database"
                )
                .addCallback(GymDatabaseCallback(context.applicationContext, scope))
                .addMigrations(MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
                .build()
                INSTANCE = instance
                instance
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE exercises ADD COLUMN projectileCount INTEGER NOT NULL DEFAULT 1")
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `products` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `name` TEXT NOT NULL, 
                        `calories` REAL NOT NULL, 
                        `proteins` REAL NOT NULL, 
                        `fats` REAL NOT NULL, 
                        `carbs` REAL NOT NULL
                    )
                """)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `nutrition_days` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `date` INTEGER NOT NULL, 
                        `isClosed` INTEGER NOT NULL
                    )
                """)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `nutrition_entries` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `dayId` INTEGER NOT NULL, 
                        `productId` INTEGER NOT NULL, 
                        `weight` REAL NOT NULL, 
                        `timestamp` INTEGER NOT NULL,
                        FOREIGN KEY(`dayId`) REFERENCES `nutrition_days`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(`productId`) REFERENCES `products`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """)
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Exercise Sets
                db.execSQL("ALTER TABLE exercise_sets RENAME COLUMN weight TO weightKg")
                db.execSQL("ALTER TABLE exercise_sets ADD COLUMN weightLb REAL")
                db.execSQL("UPDATE exercise_sets SET weightLb = weightKg * 2.20462")

                // Body Weights
                db.execSQL("ALTER TABLE body_weights RENAME COLUMN weight TO weightKg")
                db.execSQL("ALTER TABLE body_weights ADD COLUMN weightLb REAL NOT NULL DEFAULT 0.0")
                db.execSQL("UPDATE body_weights SET weightLb = weightKg * 2.20462")

                // Exercises
                db.execSQL("ALTER TABLE exercises ADD COLUMN defaultWeightStepLb REAL NOT NULL DEFAULT 5.0")
            }
        }
    }

    private class GymDatabaseCallback(
        private val context: Context,
        private val scope: CoroutineScope
    ) : Callback() {
        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    val gymDao = database.gymDao()
                    if (gymDao.getAllExercises().first().isEmpty()) {
                        populateDatabase(gymDao)
                    }
                }
            }
        }

        suspend fun populateDatabase(gymDao: GymDao) {
            val standardExercises = listOf(
                Exercise(
                    name = context.getString(R.string.ex_bench_press),
                    isWeighted = true,
                    progressionType = ProgressionType.INCREASE,
                    laterality = Laterality.BILATERAL,
                    isCustom = false,
                    projectileCount = 1,
                    defaultWeightStep = 2.5,
                    defaultWeightStepLb = 5.0,
                    muscleGroups = context.getString(R.string.ex_bench_press_muscles),
                    instructions = context.getString(R.string.ex_bench_press_instr)
                ),
                Exercise(
                    name = context.getString(R.string.ex_dumbell_fly),
                    isWeighted = true,
                    progressionType = ProgressionType.INCREASE,
                    laterality = Laterality.BILATERAL,
                    isCustom = false,
                    projectileCount = 2,
                    defaultWeightStep = 2.5,
                    defaultWeightStepLb = 5.0,
                    muscleGroups = context.getString(R.string.ex_dumbell_fly_muscles),
                    instructions = context.getString(R.string.ex_dumbell_fly_instr)
                ),
                Exercise(
                    name = context.getString(R.string.ex_gravitron_pullup),
                    isWeighted = true,
                    progressionType = ProgressionType.DECREASE,
                    laterality = Laterality.BILATERAL,
                    isCustom = false,
                    muscleGroups = context.getString(R.string.ex_gravitron_pullup_muscles),
                    instructions = context.getString(R.string.ex_gravitron_pullup_instr)
                ),
                Exercise(
                    name = context.getString(R.string.ex_gravitron_dip),
                    isWeighted = true,
                    progressionType = ProgressionType.DECREASE,
                    laterality = Laterality.BILATERAL,
                    isCustom = false,
                    muscleGroups = context.getString(R.string.ex_gravitron_dip_muscles),
                    instructions = context.getString(R.string.ex_gravitron_dip_instr)
                ),
                Exercise(
                    name = context.getString(R.string.ex_pec_deck),
                    isWeighted = true,
                    progressionType = ProgressionType.INCREASE,
                    laterality = Laterality.BILATERAL,
                    isCustom = false,
                    muscleGroups = context.getString(R.string.ex_pec_deck_muscles),
                    instructions = context.getString(R.string.ex_pec_deck_instr)
                ),
                Exercise(
                    name = context.getString(R.string.ex_wrist_curl),
                    isWeighted = true,
                    progressionType = ProgressionType.INCREASE,
                    laterality = Laterality.BILATERAL,
                    isCustom = false,
                    muscleGroups = context.getString(R.string.ex_wrist_curl_muscles),
                    instructions = context.getString(R.string.ex_wrist_curl_instr)
                ),
                Exercise(
                    name = context.getString(R.string.ex_rev_wrist_curl),
                    isWeighted = true,
                    progressionType = ProgressionType.INCREASE,
                    laterality = Laterality.BILATERAL,
                    isCustom = false,
                    muscleGroups = context.getString(R.string.ex_rev_wrist_curl_muscles),
                    instructions = context.getString(R.string.ex_rev_wrist_curl_instr)
                ),
                Exercise(
                    name = context.getString(R.string.ex_rev_bicep_curl),
                    isWeighted = true,
                    progressionType = ProgressionType.INCREASE,
                    laterality = Laterality.BILATERAL,
                    isCustom = false,
                    muscleGroups = context.getString(R.string.ex_rev_bicep_curl_muscles),
                    instructions = context.getString(R.string.ex_rev_bicep_curl_instr)
                ),
                Exercise(
                    name = context.getString(R.string.ex_scott_curl),
                    isWeighted = true,
                    progressionType = ProgressionType.INCREASE,
                    laterality = Laterality.BILATERAL,
                    isCustom = false,
                    muscleGroups = context.getString(R.string.ex_scott_curl_muscles),
                    instructions = context.getString(R.string.ex_scott_curl_instr)
                ),
                Exercise(
                    name = context.getString(R.string.ex_tricep_pushdown),
                    isWeighted = true,
                    progressionType = ProgressionType.INCREASE,
                    laterality = Laterality.BILATERAL,
                    isCustom = false,
                    muscleGroups = context.getString(R.string.ex_tricep_pushdown_muscles),
                    instructions = context.getString(R.string.ex_tricep_pushdown_instr)
                ),
                Exercise(
                    name = context.getString(R.string.ex_upper_abs),
                    isWeighted = false,
                    progressionType = ProgressionType.INCREASE,
                    laterality = Laterality.BILATERAL,
                    isCustom = false,
                    projectileCount = 0,
                    defaultWeightStep = 0.0,
                    defaultWeightStepLb = 0.0,
                    muscleGroups = context.getString(R.string.ex_upper_abs_muscles),
                    instructions = context.getString(R.string.ex_upper_abs_instr)
                ),
                Exercise(
                    name = context.getString(R.string.ex_lower_abs),
                    isWeighted = false,
                    progressionType = ProgressionType.INCREASE,
                    laterality = Laterality.BILATERAL,
                    isCustom = false,
                    projectileCount = 0,
                    defaultWeightStep = 0.0,
                    defaultWeightStepLb = 0.0,
                    muscleGroups = context.getString(R.string.ex_lower_abs_muscles),
                    instructions = context.getString(R.string.ex_lower_abs_instr)
                ),
                Exercise(
                    name = context.getString(R.string.ex_hack_squat_back),
                    isWeighted = true,
                    progressionType = ProgressionType.INCREASE,
                    laterality = Laterality.BILATERAL,
                    isCustom = false,
                    muscleGroups = context.getString(R.string.ex_hack_squat_back_muscles),
                    instructions = context.getString(R.string.ex_hack_squat_back_instr)
                ),
                Exercise(
                    name = context.getString(R.string.ex_hack_squat_front),
                    isWeighted = true,
                    progressionType = ProgressionType.INCREASE,
                    laterality = Laterality.BILATERAL,
                    isCustom = false,
                    muscleGroups = context.getString(R.string.ex_hack_squat_front_muscles),
                    instructions = context.getString(R.string.ex_hack_squat_front_instr)
                ),
                Exercise(
                    name = context.getString(R.string.ex_horiz_row),
                    isWeighted = true,
                    progressionType = ProgressionType.INCREASE,
                    laterality = Laterality.BILATERAL,
                    isCustom = false,
                    muscleGroups = context.getString(R.string.ex_horiz_row_muscles),
                    instructions = context.getString(R.string.ex_horiz_row_instr)
                ),
                Exercise(
                    name = context.getString(R.string.ex_lat_pulldown),
                    isWeighted = true,
                    progressionType = ProgressionType.INCREASE,
                    laterality = Laterality.BILATERAL,
                    isCustom = false,
                    muscleGroups = context.getString(R.string.ex_lat_pulldown_muscles),
                    instructions = context.getString(R.string.ex_lat_pulldown_instr)
                ),
                Exercise(
                    name = context.getString(R.string.ex_leg_press),
                    isWeighted = true,
                    progressionType = ProgressionType.INCREASE,
                    laterality = Laterality.BILATERAL,
                    isCustom = false,
                    muscleGroups = context.getString(R.string.ex_leg_press_muscles),
                    instructions = context.getString(R.string.ex_leg_press_instr)
                ),
                Exercise(
                    name = context.getString(R.string.ex_one_leg_press),
                    isWeighted = true,
                    progressionType = ProgressionType.INCREASE,
                    laterality = Laterality.UNILATERAL,
                    isCustom = false,
                    muscleGroups = context.getString(R.string.ex_one_leg_press_muscles),
                    instructions = context.getString(R.string.ex_one_leg_press_instr)
                ),
                Exercise(
                    name = context.getString(R.string.ex_leg_extension),
                    isWeighted = true,
                    progressionType = ProgressionType.INCREASE,
                    laterality = Laterality.BILATERAL,
                    isCustom = false,
                    muscleGroups = context.getString(R.string.ex_leg_extension_muscles),
                    instructions = context.getString(R.string.ex_leg_extension_instr)
                ),
                Exercise(
                    name = context.getString(R.string.ex_one_leg_extension),
                    isWeighted = true,
                    progressionType = ProgressionType.INCREASE,
                    laterality = Laterality.UNILATERAL,
                    isCustom = false,
                    muscleGroups = context.getString(R.string.ex_one_leg_extension_muscles),
                    instructions = context.getString(R.string.ex_one_leg_extension_instr)
                )
            )
            standardExercises.forEach { gymDao.insertExercise(it) }
        }
    }
}

class Converters {
    @TypeConverter
    fun fromProgressionType(value: ProgressionType) = value.name

    @TypeConverter
    fun toProgressionType(value: String) = ProgressionType.valueOf(value)

    @TypeConverter
    fun fromLaterality(value: Laterality) = value.name

    @TypeConverter
    fun toLaterality(value: String) = Laterality.valueOf(value)

    @TypeConverter
    fun fromSetSide(value: SetSide) = value.name

    @TypeConverter
    fun toSetSide(value: String) = SetSide.valueOf(value)
}
