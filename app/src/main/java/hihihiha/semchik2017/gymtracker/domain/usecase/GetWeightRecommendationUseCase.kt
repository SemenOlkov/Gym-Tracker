package hihihiha.semchik2017.gymtracker.domain.usecase

import hihihiha.semchik2017.gymtracker.data.model.*
import hihihiha.semchik2017.gymtracker.domain.repository.GymRepository
import javax.inject.Inject

class GetWeightRecommendationUseCase @Inject constructor(
    private val repository: GymRepository
) {
    suspend operator fun invoke(exerciseId: Long, exercise: Exercise, beforeDate: Long): RecommendationResult {
        if (!exercise.isWeighted) return RecommendationResult.None

        val lastWorkoutExercise = repository.getLastWorkoutExerciseWithSets(exerciseId, beforeDate) ?: return RecommendationResult.NoData

        val sets = lastWorkoutExercise.sets
        if (sets.isEmpty()) return RecommendationResult.NoData

        return if (exercise.laterality == Laterality.BILATERAL) {
            val recommendation = calculateRecommendation(sets, exercise.progressionType, exercise.defaultWeightStep)
            RecommendationResult.Bilateral(recommendation)
        } else {
            val leftSets = sets.filter { it.side == SetSide.LEFT }
            val rightSets = sets.filter { it.side == SetSide.RIGHT }
            
            val leftRecommendation = if (leftSets.isNotEmpty()) calculateRecommendation(leftSets, exercise.progressionType, exercise.defaultWeightStep) else null
            val rightRecommendation = if (rightSets.isNotEmpty()) calculateRecommendation(rightSets, exercise.progressionType, exercise.defaultWeightStep) else null
            
            RecommendationResult.Unilateral(leftRecommendation, rightRecommendation)
        }
    }

    private fun calculateRecommendation(sets: List<ExerciseSet>, progressionType: ProgressionType, step: Double): Double {
        val weightFreq = sets.groupBy { it.weight }.mapValues { it.value.size }
        val commonWeight = weightFreq.maxByOrNull { it.value }?.key ?: return 0.0
        
        val setsWithCommonWeight = sets.filter { it.weight == commonWeight }
        val maxRepsWithCommonWeight = setsWithCommonWeight.maxOf { it.reps }
        val setsWithHighReps = setsWithCommonWeight.count { it.reps >= 8 }
        
        return when {
            setsWithHighReps >= 2 -> {
                if (progressionType == ProgressionType.INCREASE) commonWeight + step else commonWeight - step
            }
            maxRepsWithCommonWeight < 6 -> {
                if (progressionType == ProgressionType.INCREASE) commonWeight - step else commonWeight + step
            }
            else -> commonWeight
        }.coerceAtLeast(0.0)
    }
}

sealed class RecommendationResult {
    object None : RecommendationResult()
    object NoData : RecommendationResult()
    data class Bilateral(val weight: Double) : RecommendationResult()
    data class Unilateral(val leftWeight: Double?, val rightWeight: Double?) : RecommendationResult()
}
