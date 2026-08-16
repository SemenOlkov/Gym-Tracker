package hihihiha.semchik2017.gymtracker.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Workouts : Screen("workouts", "Тренировки", Icons.Default.FitnessCenter)
    object Exercises : Screen("exercises", "Упражнения", Icons.Default.List)
    object Stats : Screen("stats", "Графики", Icons.Default.BarChart)
    object Weight : Screen("weight", "Вес тела", Icons.Default.Person)
    
    object ExerciseDetail : Screen("exercise_detail/{exerciseId}", "Детали упражнения") {
        fun createRoute(exerciseId: Long) = "exercise_detail/$exerciseId"
    }

    object WorkoutDetail : Screen("workout_detail/{workoutId}", "Детали тренировки") {
        fun createRoute(workoutId: Long) = "workout_detail/$workoutId"
    }
}
