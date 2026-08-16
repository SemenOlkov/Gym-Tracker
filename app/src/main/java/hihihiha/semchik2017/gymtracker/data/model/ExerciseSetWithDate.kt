package hihihiha.semchik2017.gymtracker.data.model

data class ExerciseSetWithDate(
    val date: Long,
    val weight: Double?,
    val reps: Int,
    val side: SetSide
)
