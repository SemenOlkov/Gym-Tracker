package hihihiha.semchik2017.gymtracker.ui.screens.stats

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import hihihiha.semchik2017.gymtracker.data.model.SetSide
import hihihiha.semchik2017.gymtracker.domain.usecase.ExerciseStatPoint
import hihihiha.semchik2017.gymtracker.ui.screens.workout.ExercisePickerDialog
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StatsScreen(
    viewModel: StatsViewModel = hiltViewModel()
) {
    val selectedExercise by viewModel.selectedExercise.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val allExercises by viewModel.allExercises.collectAsState()
    var showExercisePicker by remember { mutableStateOf(false) }

    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(stats) {
        if (stats.isNotEmpty()) {
            modelProducer.runTransaction {
                // Separate series for different sides if unilateral
                val bilateralStats = stats.filter { it.side == SetSide.BOTH }
                val leftStats = stats.filter { it.side == SetSide.LEFT }
                val rightStats = stats.filter { it.side == SetSide.RIGHT }

                if (bilateralStats.isNotEmpty()) lineSeries { series(bilateralStats.map { it.maxWeight }) }
                if (leftStats.isNotEmpty()) lineSeries { series(leftStats.map { it.maxWeight }) }
                if (rightStats.isNotEmpty()) lineSeries { series(rightStats.map { it.maxWeight }) }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Button(
            onClick = { showExercisePicker = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(selectedExercise?.name ?: "Выберите упражнение")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (stats.isNotEmpty()) {
            CartesianChartHost(
                chart = rememberCartesianChart(
                    rememberLineCartesianLayer(),
                ),
                modelProducer = modelProducer,
                modifier = Modifier.fillMaxWidth().height(300.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("История максимумов:", style = MaterialTheme.typography.titleMedium)
            stats.reversed().forEach { point ->
                val dateStr = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(point.date))
                Text("$dateStr: ${point.maxWeight} кг ${if (point.side != SetSide.BOTH) "(${point.side})" else ""}")
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Нет данных для отображения")
            }
        }
    }

    if (showExercisePicker) {
        ExercisePickerDialog(
            exercises = allExercises,
            onDismiss = { showExercisePicker = false },
            onSelect = {
                viewModel.selectExercise(it)
                showExercisePicker = false
            }
        )
    }
}
