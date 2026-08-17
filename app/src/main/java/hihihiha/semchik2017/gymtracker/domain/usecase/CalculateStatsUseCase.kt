package hihihiha.semchik2017.gymtracker.domain.usecase

import hihihiha.semchik2017.gymtracker.data.model.ExerciseSet
import javax.inject.Inject

class CalculateStatsUseCase @Inject constructor() {
    
    fun calculateOneRepMax(weight: Double?, reps: Int): Double {
        if (weight == null || reps == 0) return 0.0
        if (reps == 1) return weight
        // Epley formula
        return weight * (1 + reps / 30.0)
    }

    fun calculateVolume(weight: Double?, reps: Int, projectileCount: Int = 1): Double {
        return (weight ?: 0.0) * reps * projectileCount
    }

    fun calculateTotalVolume(sets: List<ExerciseSet>, projectileCount: Int = 1): Double {
        return sets.sumOf { calculateVolume(it.weight, it.reps, projectileCount) }
    }
}
