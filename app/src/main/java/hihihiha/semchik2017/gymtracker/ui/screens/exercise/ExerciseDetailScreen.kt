package hihihiha.semchik2017.gymtracker.ui.screens.exercise

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
import hihihiha.semchik2017.gymtracker.R
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
    val uiState by viewModel.uiState.collectAsState()

    val modelBoth = remember { CartesianChartModelProducer() }
    val modelLeft = remember { CartesianChartModelProducer() }
    val modelRight = remember { CartesianChartModelProducer() }
    
    LaunchedEffect(exerciseId) {
        viewModel.loadExerciseDetail(exerciseId)
    }

    LaunchedEffect(uiState.stats) {
        if (uiState.stats.isNotEmpty()) {
            val bilateralStats = uiState.stats.filter { it.side == SetSide.BOTH }
            val leftStats = uiState.stats.filter { it.side == SetSide.LEFT }
            val rightStats = uiState.stats.filter { it.side == SetSide.RIGHT }

            if (bilateralStats.isNotEmpty()) {
                modelBoth.runTransaction { lineSeries { series(bilateralStats.map { if (uiState.unit == "lb") it.maxWeightLb else it.maxWeightKg }) } }
            }
            if (leftStats.isNotEmpty()) {
                modelLeft.runTransaction { lineSeries { series(leftStats.map { if (uiState.unit == "lb") it.maxWeightLb else it.maxWeightKg }) } }
            }
            if (rightStats.isNotEmpty()) {
                modelRight.runTransaction { lineSeries { series(rightStats.map { if (uiState.unit == "lb") it.maxWeightLb else it.maxWeightKg }) } }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.exercise?.name ?: stringResource(R.string.nav_exercises)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.workout_cancel))
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.errorMessage != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(uiState.errorMessage!!, color = MaterialTheme.colorScheme.error)
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
                    .fillMaxSize()
            ) {
                uiState.exercise?.let { ex ->
                    Text(stringResource(R.string.exercise_muscle_groups), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(ex.muscleGroups.ifBlank { stringResource(R.string.exercise_no_data) }, style = MaterialTheme.typography.bodyLarge)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(stringResource(R.string.exercise_instructions), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(ex.instructions.ifBlank { stringResource(R.string.exercise_no_data) }, style = MaterialTheme.typography.bodyLarge)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(stringResource(R.string.exercise_last_workout), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        uiState.lastWorkoutDate?.let { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(it)) } ?: stringResource(R.string.exercise_last_workout_never),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(stringResource(R.string.exercise_pr), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                    if (uiState.prs.isEmpty()) {
                        Text(stringResource(R.string.exercise_no_data), style = MaterialTheme.typography.bodyLarge)
                    } else {
                        uiState.prs.forEach { (side, result) ->
                            val dateStr = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(result.date))
                            val sidePrefix = when (side) {
                                SetSide.LEFT -> stringResource(R.string.exercise_side_left) + ": "
                                SetSide.RIGHT -> stringResource(R.string.exercise_side_right) + ": "
                                else -> ""
                            }
                            val weightVal = if (uiState.unit == "lb") result.weightLb else result.weightKg
                            val weightStr = if (ex.isWeighted) "${String.format(Locale.getDefault(), "%.2f", weightVal)} ${uiState.unit}" else ""
                            Text(
                                text = "$sidePrefix$weightStr ($dateStr)", 
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(stringResource(R.string.exercise_rec_weight), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    RecommendationPlate(uiState.recommendation, uiState.unit)
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    if (ex.laterality == Laterality.BILATERAL) {
                        ExerciseChartSection(stringResource(R.string.exercise_progress_max_weight), uiState.stats.filter { it.side == SetSide.BOTH }, modelBoth, uiState.unit)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(stringResource(R.string.exercise_history_title), style = MaterialTheme.typography.titleSmall)
                        uiState.stats.filter { it.side == SetSide.BOTH }.reversed().forEach { point ->
                            val dateStr = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(point.date))
                            val volumeVal = if (uiState.unit == "lb") point.totalVolumeLb else point.totalVolumeKg
                            val rmVal = if (uiState.unit == "lb") point.max1RMLb else point.max1RMKg
                            
                            val volumeStr = if (ex.isWeighted) "Объем ${String.format(Locale.getDefault(), "%.2f", volumeVal)} ${uiState.unit}" else ""
                            val rmStr = if (ex.isWeighted) " | 1RM: ${String.format(Locale.getDefault(), "%.2f", rmVal)} ${uiState.unit}" else ""
                            if (volumeStr.isNotEmpty() || rmStr.isNotEmpty()) {
                                Text(
                                    text = "$dateStr: $volumeStr$rmStr",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            } else {
                                Text(text = dateStr, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    } else {
                        ExerciseChartSection(stringResource(R.string.exercise_progress_chart) + " (${stringResource(R.string.exercise_side_left)})", uiState.stats.filter { it.side == SetSide.LEFT }, modelLeft, uiState.unit)
                        if (ex.isWeighted) {
                            uiState.stats.filter { it.side == SetSide.LEFT }.reversed().take(1).forEach { point ->
                                 val rmVal = if (uiState.unit == "lb") point.max1RMLb else point.max1RMKg
                                 Text("Последний 1RM (Л): ${String.format(Locale.getDefault(), "%.2f", rmVal)} ${uiState.unit}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        ExerciseChartSection(stringResource(R.string.exercise_progress_chart) + " (${stringResource(R.string.exercise_side_right)})", uiState.stats.filter { it.side == SetSide.RIGHT }, modelRight, uiState.unit)
                        if (ex.isWeighted) {
                            uiState.stats.filter { it.side == SetSide.RIGHT }.reversed().take(1).forEach { point ->
                                 val rmVal = if (uiState.unit == "lb") point.max1RMLb else point.max1RMKg
                                 Text("Последний 1RM (П): ${String.format(Locale.getDefault(), "%.2f", rmVal)} ${uiState.unit}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExerciseChartSection(title: String, points: List<ExerciseStatPoint>, modelProducer: CartesianChartModelProducer, unit: String) {
    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    if (points.isNotEmpty()) {
        val dates = remember(points) { points.map { it.date } }
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberLineCartesianLayer(),
                startAxis = rememberStartAxis(
                    label = rememberTextComponent(color = MaterialTheme.colorScheme.onSurface),
                    title = unit,
                    titleComponent = rememberTextComponent(color = MaterialTheme.colorScheme.secondary)
                ),
                bottomAxis = rememberBottomAxis(
                    label = rememberTextComponent(color = MaterialTheme.colorScheme.onSurface),
                    valueFormatter = { x, _, _ ->
                        val index = x.toInt()
                        if (index in dates.indices) {
                            SimpleDateFormat("dd.MM", Locale.getDefault()).format(Date(dates[index]))
                        } else ""
                    }
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
        Text(stringResource(R.string.exercise_no_data), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun RecommendationPlate(recommendation: RecommendationResult, unit: String) {
    val text = when (recommendation) {
        is RecommendationResult.Bilateral -> String.format(Locale.getDefault(), "%.2f %s", recommendation.weight, unit)
        is RecommendationResult.Unilateral -> {
            val left = recommendation.leftWeight?.let { String.format(Locale.getDefault(), "Л: %.2f %s", it, unit) } ?: ""
            val right = recommendation.rightWeight?.let { String.format(Locale.getDefault(), "П: %.2f %s", it, unit) } ?: ""
            "$left $right".trim()
        }
        RecommendationResult.NoData -> stringResource(R.string.exercise_no_data)
        RecommendationResult.None -> stringResource(R.string.set_no_weight)
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
