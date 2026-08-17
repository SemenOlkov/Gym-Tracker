package hihihiha.semchik2017.gymtracker.domain.usecase

import hihihiha.semchik2017.gymtracker.data.model.ExerciseSet
import javax.inject.Inject

class CalculateStatsUseCase @Inject constructor() {
    
    fun calculateOneRepMax(weight: Double?, reps: Int, bodyWeight: Double = 0.0, isAssisted: Boolean = false): Double {
        if (reps == 0) return 0.0
        val effectiveWeight = if (isAssisted) {
            (bodyWeight - (weight ?: 0.0)).coerceAtLeast(0.0)
        } else {
            weight ?: 0.0
        }
        
        if (effectiveWeight == 0.0) return 0.0
        if (reps == 1) return effectiveWeight
        // Epley formula
        return effectiveWeight * (1 + reps / 30.0)
    }

    fun calculateVolume(weight: Double?, reps: Int, projectileCount: Int = 1, bodyWeight: Double = 0.0, isAssisted: Boolean = false): Double {
        val effectiveWeight = if (isAssisted) {
            (bodyWeight - (weight ?: 0.0)).coerceAtLeast(0.0)
        } else {
            weight ?: 0.0
        }
        return effectiveWeight * reps * projectileCount
    }

    fun calculateTotalVolume(sets: List<ExerciseSet>, projectileCount: Int = 1, bodyWeight: Double = 0.0, isAssisted: Boolean = false): Double {
        return sets.sumOf { calculateVolume(it.weight, it.reps, projectileCount, bodyWeight, isAssisted) }
    }
}
