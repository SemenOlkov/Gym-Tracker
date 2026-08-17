package hihihiha.semchik2017.gymtracker.domain.usecase

import hihihiha.semchik2017.gymtracker.data.model.*
import hihihiha.semchik2017.gymtracker.domain.repository.GymRepository
import javax.inject.Inject

class GetExerciseStatsUseCase @Inject constructor(
    private val repository: GymRepository,
    private val calculateStatsUseCase: CalculateStatsUseCase
) {
    suspend operator fun invoke(exerciseId: Long): List<ExerciseStatPoint> {
        val exercise = repository.getExerciseById(exerciseId)
        val isDecrease = exercise?.progressionType == ProgressionType.DECREASE
        val history = repository.getExerciseSetHistory(exerciseId)
        
        return history.groupBy { it.date }
            .map { (date, sets) ->
                val statsBySide = sets.groupBy { it.side }.map { (side, sideSets) ->
                    val bestWeight = if (isDecrease) {
                        sideSets.mapNotNull { it.weight }.minOrNull() ?: 0.0
                    } else {
                        sideSets.mapNotNull { it.weight }.maxOrNull() ?: 0.0
                    }
                    val totalVolume = sideSets.sumOf { calculateStatsUseCase.calculateVolume(it.weight, it.reps, exercise?.projectileCount ?: 1) }
                    val max1RM = sideSets.maxOfOrNull { calculateStatsUseCase.calculateOneRepMax(it.weight, it.reps) } ?: 0.0
                    
                    ExerciseStatPoint(date, bestWeight, side, totalVolume, max1RM)
                }
                statsBySide
            }
            .flatten()
            .sortedBy { it.date }
    }
}

data class ExerciseStatPoint(
    val date: Long,
    val maxWeight: Double,
    val side: SetSide,
    val totalVolume: Double = 0.0,
    val max1RM: Double = 0.0
)
