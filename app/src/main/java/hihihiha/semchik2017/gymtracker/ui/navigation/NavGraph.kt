package hihihiha.semchik2017.gymtracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import hihihiha.semchik2017.gymtracker.ui.screens.workout.WorkoutListScreen
import hihihiha.semchik2017.gymtracker.ui.screens.workout.WorkoutDetailScreen
import hihihiha.semchik2017.gymtracker.ui.screens.exercise.ExerciseListScreen
import hihihiha.semchik2017.gymtracker.ui.screens.exercise.ExerciseDetailScreen
import hihihiha.semchik2017.gymtracker.ui.screens.weight.WeightScreen
import hihihiha.semchik2017.gymtracker.ui.screens.nutrition.NutritionListScreen
import hihihiha.semchik2017.gymtracker.ui.screens.nutrition.NutritionDetailScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Workouts
    ) {
        composable<Screen.Workouts> {
            WorkoutListScreen(
                onWorkoutClick = { workoutId ->
                    navController.navigate(Screen.WorkoutDetail(workoutId))
                }
            )
        }
        composable<Screen.WorkoutDetail> { backStackEntry ->
            val args = backStackEntry.toRoute<Screen.WorkoutDetail>()
            WorkoutDetailScreen(
                workoutId = args.workoutId,
                onBack = { navController.popBackStack() }
            )
        }
        composable<Screen.Exercises> {
            ExerciseListScreen(
                onExerciseClick = { exerciseId ->
                    navController.navigate(Screen.ExerciseDetail(exerciseId))
                }
            )
        }
        composable<Screen.ExerciseDetail> { backStackEntry ->
            val args = backStackEntry.toRoute<Screen.ExerciseDetail>()
            ExerciseDetailScreen(
                exerciseId = args.exerciseId,
                onBack = { navController.popBackStack() }
            )
        }
        composable<Screen.Weight> {
            WeightScreen()
        }
        composable<Screen.Nutrition> {
            NutritionListScreen(
                onDayClick = { dayId: Long ->
                    navController.navigate(Screen.NutritionDetail(dayId))
                }
            )
        }
        composable<Screen.NutritionDetail> { backStackEntry ->
            val args = backStackEntry.toRoute<Screen.NutritionDetail>()
            NutritionDetailScreen(
                dayId = args.dayId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
