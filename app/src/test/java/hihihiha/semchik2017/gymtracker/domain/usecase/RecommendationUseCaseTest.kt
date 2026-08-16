package hihihiha.semchik2017.gymtracker.domain.usecase

import hihihiha.semchik2017.gymtracker.data.model.*
import hihihiha.semchik2017.gymtracker.domain.repository.GymRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

class RecommendationUseCaseTest {
    private lateinit var repository: GymRepository
    private lateinit var useCase: GetWeightRecommendationUseCase

    @Before
    fun setup() {
        repository = mock(GymRepository::class.java)
        useCase = GetWeightRecommendationUseCase(repository)
    }

    @Test
    fun `test increase recommendation when 2 sets with 8+ reps`() = runBlocking {
        val exercise = Exercise(id = 1, name = "Test", isWeighted = true, progressionType = ProgressionType.INCREASE, laterality = Laterality.BILATERAL, isCustom = true)
        val sets = listOf(
            ExerciseSet(id = 1, workoutExerciseId = 1, setNumber = 1, weight = 100.0, reps = 8, side = SetSide.BOTH),
            ExerciseSet(id = 2, workoutExerciseId = 1, setNumber = 2, weight = 100.0, reps = 8, side = SetSide.BOTH),
            ExerciseSet(id = 3, workoutExerciseId = 1, setNumber = 3, weight = 100.0, reps = 5, side = SetSide.BOTH)
        )
        val lastWorkout = WorkoutExerciseWithSets(
            WorkoutExercise(id = 1, workoutId = 1, exerciseId = 1, orderIndex = 0),
            exercise,
            sets
        )
        
        `when`(repository.getLastWorkoutExerciseWithSets(eq(1L), anyLong())).thenReturn(lastWorkout)
        
        val result = useCase(1, exercise, System.currentTimeMillis())
        
        assertEquals(RecommendationResult.Bilateral(102.5), result)
    }

    @Test
    fun `test no increase recommendation when only 1 set with 8+ reps`() = runBlocking {
        val exercise = Exercise(id = 1, name = "Test", isWeighted = true, progressionType = ProgressionType.INCREASE, laterality = Laterality.BILATERAL, isCustom = true)
        val sets = listOf(
            ExerciseSet(id = 1, workoutExerciseId = 1, setNumber = 1, weight = 100.0, reps = 8, side = SetSide.BOTH),
            ExerciseSet(id = 2, workoutExerciseId = 1, setNumber = 2, weight = 100.0, reps = 7, side = SetSide.BOTH)
        )
        val lastWorkout = WorkoutExerciseWithSets(
            WorkoutExercise(id = 1, workoutId = 1, exerciseId = 1, orderIndex = 0),
            exercise,
            sets
        )
        
        `when`(repository.getLastWorkoutExerciseWithSets(eq(1L), anyLong())).thenReturn(lastWorkout)
        
        val result = useCase(1, exercise, System.currentTimeMillis())
        
        assertEquals(RecommendationResult.Bilateral(100.0), result)
    }

    @Test
    fun `test decrease recommendation for Gravitron when 2 sets with 8+ reps`() = runBlocking {
        val exercise = Exercise(id = 1, name = "Gravitron", isWeighted = true, progressionType = ProgressionType.DECREASE, laterality = Laterality.BILATERAL, isCustom = true)
        val sets = listOf(
            ExerciseSet(id = 1, workoutExerciseId = 1, setNumber = 1, weight = 50.0, reps = 8, side = SetSide.BOTH),
            ExerciseSet(id = 2, workoutExerciseId = 1, setNumber = 2, weight = 50.0, reps = 8, side = SetSide.BOTH)
        )
        val lastWorkout = WorkoutExerciseWithSets(
            WorkoutExercise(id = 1, workoutId = 1, exerciseId = 1, orderIndex = 0),
            exercise,
            sets
        )
        
        `when`(repository.getLastWorkoutExerciseWithSets(eq(1L), anyLong())).thenReturn(lastWorkout)
        
        val result = useCase(1, exercise, System.currentTimeMillis())
        
        assertEquals(RecommendationResult.Bilateral(47.5), result)
    }
}
