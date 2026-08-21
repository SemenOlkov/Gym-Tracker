package hihihiha.semchik2017.gymtracker.domain.usecase

import hihihiha.semchik2017.gymtracker.data.model.*
import hihihiha.semchik2017.gymtracker.domain.repository.GymRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

class GetExerciseStatsUseCaseTest {
    private lateinit var repository: GymRepository
    private lateinit var calculateStatsUseCase: CalculateStatsUseCase
    private lateinit var useCase: GetExerciseStatsUseCase

    @Before
    fun setup() {
        repository = mock(GymRepository::class.java)
        calculateStatsUseCase = CalculateStatsUseCase()
        useCase = GetExerciseStatsUseCase(repository, calculateStatsUseCase)
    }

    @Test
    fun `test max weight calculation for INCREASE progression`() = runBlocking {
        val exerciseId = 1L
        val exercise = Exercise(id = exerciseId, name = "Bench Press", isWeighted = true, progressionType = ProgressionType.INCREASE, laterality = Laterality.BILATERAL, isCustom = false)
        val history = listOf(
            ExerciseSetWithDate(date = 1000L, weightKg = 80.0, weightLb = 80.0 * 2.20462, reps = 10, side = SetSide.BOTH),
            ExerciseSetWithDate(date = 1000L, weightKg = 100.0, weightLb = 100.0 * 2.20462, reps = 5, side = SetSide.BOTH),
            ExerciseSetWithDate(date = 1000L, weightKg = 90.0, weightLb = 90.0 * 2.20462, reps = 8, side = SetSide.BOTH)
        )

        `when`(repository.getExerciseById(exerciseId)).thenReturn(exercise)
        `when`(repository.getExerciseSetHistory(exerciseId)).thenReturn(history)

        val result = useCase(exerciseId)

        assertEquals(1, result.size)
        assertEquals(100.0, result[0].maxWeightKg, 0.0)
    }

    @Test
    fun `test min weight calculation for DECREASE progression (Gravitron)`() = runBlocking {
        val exerciseId = 2L
        val exercise = Exercise(id = exerciseId, name = "Gravitron", isWeighted = true, progressionType = ProgressionType.DECREASE, laterality = Laterality.BILATERAL, isCustom = false)
        val history = listOf(
            ExerciseSetWithDate(date = 1000L, weightKg = 50.0, weightLb = 50.0 * 2.20462, reps = 10, side = SetSide.BOTH),
            ExerciseSetWithDate(date = 1000L, weightKg = 30.0, weightLb = 30.0 * 2.20462, reps = 5, side = SetSide.BOTH),
            ExerciseSetWithDate(date = 1000L, weightKg = 40.0, weightLb = 40.0 * 2.20462, reps = 8, side = SetSide.BOTH)
        )

        `when`(repository.getExerciseById(exerciseId)).thenReturn(exercise)
        `when`(repository.getExerciseSetHistory(exerciseId)).thenReturn(history)

        val result = useCase(exerciseId)

        assertEquals(1, result.size)
        assertEquals(30.0, result[0].maxWeightKg, 0.0) // 30.0 is the best (minimum) weight
    }

    @Test
    fun `test volume calculation with projectileCount = 2 (Dumbbell Fly)`() = runBlocking {
        val exerciseId = 3L
        val exercise = Exercise(
            id = exerciseId, 
            name = "Dumbbell Fly", 
            isWeighted = true, 
            progressionType = ProgressionType.INCREASE, 
            laterality = Laterality.BILATERAL, 
            isCustom = false,
            projectileCount = 2
        )
        val history = listOf(
            ExerciseSetWithDate(date = 1000L, weightKg = 10.0, weightLb = 10.0 * 2.20462, reps = 10, side = SetSide.BOTH)
        )

        `when`(repository.getExerciseById(exerciseId)).thenReturn(exercise)
        `when`(repository.getExerciseSetHistory(exerciseId)).thenReturn(history)

        val result = useCase(exerciseId)

        assertEquals(1, result.size)
        // 10.0 weight * 10 reps * 2 projectiles = 200.0
        assertEquals(200.0, result[0].totalVolumeKg, 0.0)
    }

    @Test
    fun `test volume calculation with projectileCount = 0 (Bodyweight Abs)`() = runBlocking {
        val exerciseId = 4L
        val exercise = Exercise(
            id = exerciseId, 
            name = "Upper Abs", 
            isWeighted = false, 
            progressionType = ProgressionType.INCREASE, 
            laterality = Laterality.BILATERAL, 
            isCustom = false,
            projectileCount = 0
        )
        val history = listOf(
            ExerciseSetWithDate(date = 1000L, weightKg = null, weightLb = null, reps = 20, side = SetSide.BOTH)
        )

        `when`(repository.getExerciseById(exerciseId)).thenReturn(exercise)
        `when`(repository.getExerciseSetHistory(exerciseId)).thenReturn(history)

        val result = useCase(exerciseId)

        assertEquals(1, result.size)
        // projectileCount = 0 means volume is 0
        assertEquals(0.0, result[0].totalVolumeKg, 0.0)
    }

    @Test
    fun `test volume calculation for assisted exercise (Gravitron)`() = runBlocking {
        val exerciseId = 5L
        val bodyWeight = 80.0
        val exercise = Exercise(
            id = exerciseId, 
            name = "Assisted Pullups", 
            isWeighted = true, 
            progressionType = ProgressionType.DECREASE, 
            laterality = Laterality.BILATERAL, 
            isCustom = false,
            projectileCount = 1
        )
        val history = listOf(
            ExerciseSetWithDate(date = 1000L, weightKg = 30.0, weightLb = 30.0 * 2.20462, reps = 10, side = SetSide.BOTH)
        )

        `when`(repository.getExerciseById(exerciseId)).thenReturn(exercise)
        `when`(repository.getExerciseSetHistory(exerciseId)).thenReturn(history)
        `when`(repository.getLatestBodyWeight()).thenReturn(bodyWeight)

        val result = useCase(exerciseId)

        assertEquals(1, result.size)
        // Effective Weight = 80.0 (body) - 30.0 (assistance) = 50.0
        // Volume = 50.0 * 10 reps * 1 projectile = 500.0
        assertEquals(500.0, result[0].totalVolumeKg, 0.0)
        
        // 1RM check: 50.0 * (1 + 10/30.0) = 50.0 * 1.3333 = 66.666
        assertEquals(66.666, result[0].max1RMKg, 0.001)
    }
}
