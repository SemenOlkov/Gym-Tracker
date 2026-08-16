package hihihiha.semchik2017.gymtracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import hihihiha.semchik2017.gymtracker.ui.screens.workout.WorkoutListScreen
import hihihiha.semchik2017.gymtracker.ui.screens.workout.WorkoutDetailScreen
import hihihiha.semchik2017.gymtracker.ui.screens.exercise.ExerciseListScreen
import hihihiha.semchik2017.gymtracker.ui.screens.exercise.ExerciseDetailScreen
import hihihiha.semchik2017.gymtracker.ui.screens.weight.WeightScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Workouts.route
    ) {
        composable(Screen.Workouts.route) {
            WorkoutListScreen(
                onWorkoutClick = { workoutId ->
                    navController.navigate(Screen.WorkoutDetail.createRoute(workoutId))
                }
            )
        }
        composable(
            route = Screen.WorkoutDetail.route,
            arguments = listOf(navArgument("workoutId") { type = NavType.LongType })
        ) { backStackEntry ->
            val workoutId = backStackEntry.arguments?.getLong("workoutId") ?: -1L
            WorkoutDetailScreen(
                workoutId = workoutId,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Exercises.route) {
            ExerciseListScreen(
                onExerciseClick = { exerciseId ->
                    navController.navigate(Screen.ExerciseDetail.createRoute(exerciseId))
                }
            )
        }
        composable(
            route = Screen.ExerciseDetail.route,
            arguments = listOf(navArgument("exerciseId") { type = NavType.LongType })
        ) { backStackEntry ->
            val exerciseId = backStackEntry.arguments?.getLong("exerciseId") ?: -1L
            ExerciseDetailScreen(
                exerciseId = exerciseId,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Weight.route) {
            WeightScreen()
        }
    }
}
