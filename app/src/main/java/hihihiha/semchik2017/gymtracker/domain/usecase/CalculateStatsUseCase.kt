package hihihiha.semchik2017.gymtracker.domain.usecase

import hihihiha.semchik2017.gymtracker.data.model.ExerciseSet
import javax.inject.Inject

class CalculateStatsUseCase @Inject constructor() {
    
    fun calculateOneRepMax(weight: Double?, reps: Int, bodyWeight: Double = 0.0, isAssisted: Boolean = false): Double {
        if (reps == 0 || weight == null) return 0.0
        val effectiveWeight = if (isAssisted) {
            (bodyWeight - weight).coerceAtLeast(0.0)
        } else {
            weight
        }
        
        if (effectiveWeight == 0.0) return 0.0
        if (reps == 1) return effectiveWeight
        // Epley formula
        return effectiveWeight * (1 + reps / 30.0)
    }

    fun calculateVolume(weight: Double?, reps: Int, projectileCount: Int = 1, bodyWeight: Double = 0.0, isAssisted: Boolean = false): Double {
        if (weight == null) return 0.0
        val effectiveWeight = if (isAssisted) {
            (bodyWeight - weight).coerceAtLeast(0.0)
        } else {
            weight
        }
        return effectiveWeight * reps * projectileCount
    }

    fun calculateTotalVolume(sets: List<ExerciseSet>, unit: String = "kg", projectileCount: Int = 1, bodyWeight: Double = 0.0, isAssisted: Boolean = false): Double {
        return sets.sumOf { 
            val weight = if (unit == "lb") it.weightLb else it.weightKg
            calculateVolume(weight, it.reps, projectileCount, bodyWeight, isAssisted) 
        }
    }
}
