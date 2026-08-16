package hihihiha.semchik2017.gymtracker.data

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import hihihiha.semchik2017.gymtracker.data.dao.GymDao
import hihihiha.semchik2017.gymtracker.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Database(
    entities = [Exercise::class, Workout::class, WorkoutExercise::class, ExerciseSet::class, BodyWeight::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class GymDatabase : RoomDatabase() {
    abstract fun gymDao(): GymDao

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
                .fallbackToDestructiveMigration()
                .addCallback(GymDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class GymDatabaseCallback(
        private val scope: CoroutineScope
    ) : Callback() {
        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    val gymDao = database.gymDao()
                    // Only populate if table is empty
                    if (gymDao.getAllExercises().first().isEmpty()) {
                        populateDatabase(gymDao)
                    }
                }
            }
        }

        suspend fun populateDatabase(gymDao: GymDao) {
            val standardExercises = listOf(
                Exercise(
                    name = "Жим лежа",
                    isWeighted = true,
                    progressionType = ProgressionType.INCREASE,
                    laterality = Laterality.BILATERAL,
                    isCustom = false,
                    muscleGroups = "Грудь, Трицепс, Передняя дельта",
                    instructions = "Лягте на скамью, возьмитесь за штангу хватом чуть шире плеч. Опустите штангу до касания груди и мощно выжмите вверх."
                ),
                Exercise(
                    name = "Разведение гантелей лежа",
                    isWeighted = true,
                    progressionType = ProgressionType.INCREASE,
                    laterality = Laterality.BILATERAL,
                    isCustom = false,
                    muscleGroups = "Грудь (изоляция)",
                    instructions = "Лёжа на скамье, разведите руки с гантелями в стороны, слегка согнув их в локтях. Сведите гантели над собой за счёт сокращения грудных мышц."
                ),
                Exercise(
                    name = "Подтягивания на гравитроне",
                    isWeighted = true,
                    progressionType = ProgressionType.DECREASE,
                    laterality = Laterality.BILATERAL,
                    isCustom = false,
                    muscleGroups = "Широчайшие, Бицепс",
                    instructions = "Встаньте на платформу, возьмитесь за перекладину. Подтянитесь, стараясь коснуться грудью перекладины. Чем больше вес противовеса, тем легче."
                ),
                Exercise(
                    name = "Отжимания на брусьях на гравитроне",
                    isWeighted = true,
                    progressionType = ProgressionType.DECREASE,
                    laterality = Laterality.BILATERAL,
                    isCustom = false,
                    muscleGroups = "Трицепс, Грудь, Плечи",
                    instructions = "Упритесь руками в брусья, колени на платформу. Опуститесь до угла 90 градусов в локтях и выжмите себя вверх."
                ),
                Exercise(
                    name = "Сведение в пэк деке (бабочка) на грудь",
                    isWeighted = true,
                    progressionType = ProgressionType.INCREASE,
                    laterality = Laterality.BILATERAL,
                    isCustom = false,
                    muscleGroups = "Грудь",
                    instructions = "Сядьте в тренажер, упритесь предплечьями или кистями в подушки. Сведите руки перед собой, максимально напрягая грудь."
                ),
                Exercise(
                    name = "Сгибание кистей со штангой",
                    isWeighted = true,
                    progressionType = ProgressionType.INCREASE,
                    laterality = Laterality.BILATERAL,
                    isCustom = false,
                    muscleGroups = "Предплечья (внутренняя часть)",
                    instructions = "Положите предплечья на бедра или скамью ладонями вверх. Сгибайте только кисти, поднимая штангу."
                ),
                Exercise(
                    name = "Сгибание кистей со штангой обратным хватом",
                    isWeighted = true,
                    progressionType = ProgressionType.INCREASE,
                    laterality = Laterality.BILATERAL,
                    isCustom = false,
                    muscleGroups = "Предплечья (внешняя часть)",
                    instructions = "Положите предплечья на скамью ладонями вниз. Разгибайте кисти вверх, поднимая штангу."
                ),
                Exercise(
                    name = "Подъем на бицепс обратным хватом",
                    isWeighted = true,
                    progressionType = ProgressionType.INCREASE,
                    laterality = Laterality.BILATERAL,
                    isCustom = false,
                    muscleGroups = "Бицепс, Брахиалис, Предплечья",
                    instructions = "Возьмите штангу хватом сверху (ладони вниз). Поднимайте штангу к плечам, не раскачиваясь."
                ),
                Exercise(
                    name = "Подъем на бицепс на скамье Скотта",
                    isWeighted = true,
                    progressionType = ProgressionType.INCREASE,
                    laterality = Laterality.BILATERAL,
                    isCustom = false,
                    muscleGroups = "Бицепс (нижняя часть)",
                    instructions = "Расположите руки на подушке скамьи Скотта. Медленно опускайте штангу и подконтрольно поднимайте вверх."
                ),
                Exercise(
                    name = "Тяга верхнего блока на трицепс",
                    isWeighted = true,
                    progressionType = ProgressionType.INCREASE,
                    laterality = Laterality.BILATERAL,
                    isCustom = false,
                    muscleGroups = "Трицепс",
                    instructions = "Возьмитесь за рукоять верхнего блока. Прижмите локти к корпусу и разгибайте руки до полного выпрямления."
                ),
                Exercise(
                    name = "Верхний пресс",
                    isWeighted = false,
                    progressionType = ProgressionType.INCREASE,
                    laterality = Laterality.BILATERAL,
                    isCustom = false,
                    muscleGroups = "Пресс (верхняя часть)",
                    instructions = "Лёжа на полу, ноги согнуты. Поднимайте только лопатки, максимально скручивая пресс."
                ),
                Exercise(
                    name = "Нижний пресс",
                    isWeighted = false,
                    progressionType = ProgressionType.INCREASE,
                    laterality = Laterality.BILATERAL,
                    isCustom = false,
                    muscleGroups = "Пресс (нижняя часть)",
                    instructions = "Лёжа на полу, поднимайте ноги до вертикального положения, слегка отрывая таз от пола."
                ),
                Exercise(
                    name = "Приседания в хаке спиной к тренажеру",
                    isWeighted = true,
                    progressionType = ProgressionType.INCREASE,
                    laterality = Laterality.BILATERAL,
                    isCustom = false,
                    muscleGroups = "Квадрицепс, Ягодицы",
                    instructions = "Плотно прижмитесь спиной к спинке, стопы на платформе. Плавно приседайте и мощно вставайте."
                ),
                Exercise(
                    name = "Приседание в хаке лицом к тренажеру",
                    isWeighted = true,
                    progressionType = ProgressionType.INCREASE,
                    laterality = Laterality.BILATERAL,
                    isCustom = false,
                    muscleGroups = "Ягодицы, Бицепс бедра",
                    instructions = "Упритесь грудью в спинку, стопы широко на платформе. Отводите таз назад при приседании."
                ),
                Exercise(
                    name = "Тяга горизонтального блока",
                    isWeighted = true,
                    progressionType = ProgressionType.INCREASE,
                    laterality = Laterality.BILATERAL,
                    isCustom = false,
                    muscleGroups = "Спина (ширина и толщина)",
                    instructions = "Сядьте, упритесь ногами. Тяните рукоять к животу, сводя лопатки. Не отклоняйтесь сильно назад."
                ),
                Exercise(
                    name = "Тяга вертикального блока",
                    isWeighted = true,
                    progressionType = ProgressionType.INCREASE,
                    laterality = Laterality.BILATERAL,
                    isCustom = false,
                    muscleGroups = "Широчайшие",
                    instructions = "Возьмитесь за рукоять широким хватом. Тяните её к верхней части груди, сводя лопатки."
                ),
                Exercise(
                    name = "Жим ногами",
                    isWeighted = true,
                    progressionType = ProgressionType.INCREASE,
                    laterality = Laterality.BILATERAL,
                    isCustom = false,
                    muscleGroups = "Ноги (все мышцы)",
                    instructions = "Выжимайте платформу ногами, не выпрямляя колени до конца в верхней точке."
                ),
                Exercise(
                    name = "Жим одной ногой",
                    isWeighted = true,
                    progressionType = ProgressionType.INCREASE,
                    laterality = Laterality.UNILATERAL,
                    isCustom = false,
                    muscleGroups = "Квадрицепс, Ягодицы",
                    instructions = "Выполняйте жим поочередно каждой ногой для устранения дисбаланса."
                ),
                Exercise(
                    name = "Разгибание ног",
                    isWeighted = true,
                    progressionType = ProgressionType.INCREASE,
                    laterality = Laterality.BILATERAL,
                    isCustom = false,
                    muscleGroups = "Квадрицепс",
                    instructions = "Сядьте в тренажер, разгибайте ноги до полного сокращения квадрицепсов."
                ),
                Exercise(
                    name = "Разгибание одной ногой",
                    isWeighted = true,
                    progressionType = ProgressionType.INCREASE,
                    laterality = Laterality.UNILATERAL,
                    isCustom = false,
                    muscleGroups = "Квадрицепс",
                    instructions = "Разгибайте ноги по очереди для более глубокой проработки."
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
