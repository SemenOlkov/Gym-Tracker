package hihihiha.semchik2017.gymtracker.data.model

data class ExerciseSetWithDate(
    val date: Long,
    val weightKg: Double?,
    val weightLb: Double?,
    val reps: Int,
    val side: SetSide
)
