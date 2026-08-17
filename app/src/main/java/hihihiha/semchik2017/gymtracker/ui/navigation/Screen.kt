package hihihiha.semchik2017.gymtracker.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable object Workouts : Screen
    @Serializable object Exercises : Screen
    @Serializable object Nutrition : Screen
    @Serializable object Weight : Screen
    
    @Serializable 
    data class ExerciseDetail(val exerciseId: Long) : Screen

    @Serializable 
    data class WorkoutDetail(val workoutId: Long) : Screen

    @Serializable
    data class NutritionDetail(val dayId: Long) : Screen
}

data class BottomNavItem(
    val route: Any,
    val titleRes: Int,
    val icon: ImageVector
)
