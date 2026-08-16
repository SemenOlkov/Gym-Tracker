package hihihiha.semchik2017.gymtracker.ui.screens.exercise

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import hihihiha.semchik2017.gymtracker.data.model.*
import hihihiha.semchik2017.gymtracker.domain.usecase.RecommendationResult
import hihihiha.semchik2017.gymtracker.domain.usecase.ExerciseStatPoint
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailScreen(
    exerciseId: Long,
    onBack: () -> Unit,
    viewModel: ExerciseDetailViewModel = hiltViewModel()
) {
    val exercise by viewModel.exercise.collectAsState()
    val recommendation by viewModel.recommendation.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val prs by viewModel.prs.collectAsState()
    val lastWorkoutDate by viewModel.lastWorkoutDate.collectAsState()

    val modelProducerBoth = remember { CartesianChartModelProducer() }
    val modelProducerLeft = remember { CartesianChartModelProducer() }
    val modelProducerRight = remember { CartesianChartModelProducer() }
    
    LaunchedEffect(exerciseId) {
        viewModel.loadExerciseDetail(exerciseId)
    }

    LaunchedEffect(stats) {
        if (stats.isNotEmpty()) {
            val bilateralStats = stats.filter { it.side == SetSide.BOTH }
            val leftStats = stats.filter { it.side == SetSide.LEFT }
            val rightStats = stats.filter { it.side == SetSide.RIGHT }

            if (bilateralStats.isNotEmpty()) {
                modelProducerBoth.runTransaction { lineSeries { series(bilateralStats.map { it.maxWeight }) } }
            }
            if (leftStats.isNotEmpty()) {
                modelProducerLeft.runTransaction { lineSeries { series(leftStats.map { it.maxWeight }) } }
            }
            if (rightStats.isNotEmpty()) {
                modelProducerRight.runTransaction { lineSeries { series(rightStats.map { it.maxWeight }) } }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(exercise?.name ?: "Упражнение") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
        ) {
            exercise?.let { ex ->
                Text("Группы мышц:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(ex.muscleGroups.ifBlank { "Не указано" }, style = MaterialTheme.typography.bodyLarge)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Инструкция:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(ex.instructions.ifBlank { "Инструкция отсутствует" }, style = MaterialTheme.typography.bodyLarge)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Последняя тренировка:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    lastWorkoutDate?.let { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(it)) } ?: "Никогда",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Личный рекорд (PR):", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                if (prs.isEmpty()) {
                    Text("Данных нет", style = MaterialTheme.typography.bodyLarge)
                } else {
                    prs.forEach { (side, result) ->
                        val dateStr = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(result.date))
                        val sidePrefix = when (side) {
                            SetSide.LEFT -> "Лево: "
                            SetSide.RIGHT -> "Право: "
                            else -> ""
                        }
                        Text(
                            text = "$sidePrefix${result.maxWeight} кг ($dateStr)", 
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Текущая рекомендация:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                RecommendationPlate(recommendation)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                if (ex.laterality == Laterality.BILATERAL) {
                    ExerciseChartSection("Прогресс Макс. Веса", stats.filter { it.side == SetSide.BOTH }, modelProducerBoth)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("История объема и 1RM:", style = MaterialTheme.typography.titleSmall)
                    stats.filter { it.side == SetSide.BOTH }.reversed().forEach { point ->
                        val dateStr = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(point.date))
                        Text(
                            text = "$dateStr: Объем ${point.totalVolume} кг | 1RM: ${String.format(Locale.getDefault(), "%.1f", point.max1RM)} кг",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                } else {
                    ExerciseChartSection("Прогресс (Лево)", stats.filter { it.side == SetSide.LEFT }, modelProducerLeft)
                    stats.filter { it.side == SetSide.LEFT }.reversed().take(1).forEach { point ->
                         Text("Последний 1RM (Л): ${String.format(Locale.getDefault(), "%.1f", point.max1RM)} кг", style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    ExerciseChartSection("Прогресс (Право)", stats.filter { it.side == SetSide.RIGHT }, modelProducerRight)
                    stats.filter { it.side == SetSide.RIGHT }.reversed().take(1).forEach { point ->
                         Text("Последний 1RM (П): ${String.format(Locale.getDefault(), "%.1f", point.max1RM)} кг", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
fun ExerciseChartSection(title: String, points: List<ExerciseStatPoint>, modelProducer: CartesianChartModelProducer) {
    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    if (points.isNotEmpty()) {
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberLineCartesianLayer(),
                startAxis = rememberStartAxis(
                    label = rememberTextComponent(color = MaterialTheme.colorScheme.onSurface),
                    title = "Вес (кг)",
                    titleComponent = rememberTextComponent(color = MaterialTheme.colorScheme.secondary)
                ),
                bottomAxis = rememberBottomAxis(
                    label = rememberTextComponent(color = MaterialTheme.colorScheme.onSurface),
                    title = "№ Тренировки",
                    titleComponent = rememberTextComponent(color = MaterialTheme.colorScheme.secondary)
                ),
                marker = rememberDefaultCartesianMarker(
                    label = rememberTextComponent(
                        color = MaterialTheme.colorScheme.onSurface,
                        background = rememberShapeComponent(
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            shape = com.patrykandpatrick.vico.core.common.shape.Shape.Rectangle
                        )
                    )
                )
            ),
            modelProducer = modelProducer,
            modifier = Modifier.fillMaxWidth().height(250.dp),
        )
    } else {
        Text("Недостаточно данных", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun RecommendationPlate(recommendation: RecommendationResult) {
    val text = when (recommendation) {
        is RecommendationResult.Bilateral -> "${recommendation.weight} кг"
        is RecommendationResult.Unilateral -> {
            val left = recommendation.leftWeight?.let { "Л: $it кг" } ?: ""
            val right = recommendation.rightWeight?.let { "П: $it кг" } ?: ""
            "$left $right".trim()
        }
        RecommendationResult.NoData -> "Данных нет"
        RecommendationResult.None -> "Без веса"
    }
    
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = MaterialTheme.shapes.medium
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}
