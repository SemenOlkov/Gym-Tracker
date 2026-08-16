package hihihiha.semchik2017.gymtracker.domain.usecase

import hihihiha.semchik2017.gymtracker.data.model.*
import hihihiha.semchik2017.gymtracker.domain.repository.GymRepository
import javax.inject.Inject

class GetExerciseStatsUseCase @Inject constructor(
    private val repository: GymRepository,
    private val calculateStatsUseCase: CalculateStatsUseCase
) {
    suspend operator fun invoke(exerciseId: Long): List<ExerciseStatPoint> {
        val history = repository.getExerciseSetHistory(exerciseId)
        
        return history.groupBy { it.date }
            .map { (date, sets) ->
                val statsBySide = sets.groupBy { it.side }.map { (side, sideSets) ->
                    val maxWeight = sideSets.maxOfOrNull { it.weight ?: 0.0 } ?: 0.0
                    val totalVolume = sideSets.sumOf { (it.weight ?: 0.0) * it.reps }
                    val max1RM = sideSets.maxOfOrNull { calculateStatsUseCase.calculateOneRepMax(it.weight, it.reps) } ?: 0.0
                    
                    ExerciseStatPoint(date, maxWeight, side, totalVolume, max1RM)
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
