package hihihiha.semchik2017.gymtracker.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class ProgressionType {
    INCREASE, DECREASE
}

@Serializable
enum class Laterality {
    BILATERAL, UNILATERAL
}

@Serializable
enum class SetSide {
    LEFT, RIGHT, BOTH
}
