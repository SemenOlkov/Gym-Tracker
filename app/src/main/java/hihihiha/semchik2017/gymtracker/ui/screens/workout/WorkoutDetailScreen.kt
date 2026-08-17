package hihihiha.semchik2017.gymtracker.ui.screens.workout

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import hihihiha.semchik2017.gymtracker.R
import hihihiha.semchik2017.gymtracker.data.model.*
import hihihiha.semchik2017.gymtracker.domain.usecase.RecommendationResult
import hihihiha.semchik2017.gymtracker.domain.usecase.CalculateStatsUseCase
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetailScreen(
    workoutId: Long,
    onBack: () -> Unit,
    viewModel: WorkoutDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val allExercises by viewModel.allExercises.collectAsState()
    val timerSeconds by viewModel.timerSeconds.collectAsState()
    var showAddExerciseDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showCompleteConfirmDialog by remember { mutableStateOf(false) }

    val isCompleted = uiState.workoutWithExercises?.workout?.isCompleted == true

    LaunchedEffect(workoutId) {
        viewModel.loadWorkout(workoutId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(uiState.workoutWithExercises?.workout?.name ?: stringResource(R.string.nav_workouts))
                            if (isCompleted) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    Icons.Default.CheckCircle, 
                                    contentDescription = stringResource(R.string.workout_completed_badge),
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        uiState.workoutWithExercises?.workout?.date?.let {
                            Text(
                                SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(it)),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.workout_cancel))
                    }
                },
                actions = {
                    if (timerSeconds > 0) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.padding(end = 8.dp).clickable { viewModel.stopTimer() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = String.format(Locale.getDefault(), "%02d:%02d", timerSeconds / 60, timerSeconds % 60),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    if (!isCompleted) {
                        IconButton(onClick = { showRenameDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.workout_rename))
                        }
                        Button(
                            onClick = { showCompleteConfirmDialog = true },
                            modifier = Modifier.padding(end = 8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text(stringResource(R.string.workout_complete), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (!isCompleted) {
                FloatingActionButton(onClick = { showAddExerciseDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.workout_add_exercise))
                }
            }
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
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                val exercises = uiState.workoutWithExercises?.exercises?.sortedBy { it.workoutExercise.orderIndex } ?: emptyList()
                items(exercises) { exerciseWithSets ->
                    ExerciseCard(
                        exerciseWithSets = exerciseWithSets,
                        recommendation = uiState.recommendations[exerciseWithSets.workoutExercise.id],
                        readOnly = isCompleted,
                        onAddSet = { side -> viewModel.addSet(exerciseWithSets.workoutExercise.id, side) },
                        onUpdateSet = { viewModel.updateSet(it) },
                        onCompleteSet = { viewModel.completeSet(it) },
                        onDeleteSet = { viewModel.deleteSet(it) },
                        calculateStatsUseCase = viewModel.calculateStatsUseCase,
                        bodyWeight = uiState.bodyWeight
                    )
                }
            }
        }
    }

    if (showCompleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showCompleteConfirmDialog = false },
            title = { Text(stringResource(R.string.workout_complete_title)) },
            text = { Text(stringResource(R.string.workout_complete_msg)) },
            confirmButton = {
                Button(onClick = {
                    viewModel.completeWorkout()
                    showCompleteConfirmDialog = false
                }) {
                    Text(stringResource(R.string.workout_complete_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCompleteConfirmDialog = false }) {
                    Text(stringResource(R.string.workout_cancel))
                }
            }
        )
    }

    if (showRenameDialog) {
        RenameWorkoutDialog(
            currentName = uiState.workoutWithExercises?.workout?.name ?: "",
            onDismiss = { showRenameDialog = false },
            onConfirm = {
                viewModel.updateWorkoutName(it)
                showRenameDialog = false
            }
        )
    }

    if (showAddExerciseDialog) {
        ExercisePickerDialog(
            exercises = allExercises,
            onDismiss = { showAddExerciseDialog = false },
            onSelect = {
                viewModel.addExerciseToWorkout(it)
                showAddExerciseDialog = false
            }
        )
    }
}

@Composable
fun ExerciseCard(
    exerciseWithSets: WorkoutExerciseWithSets,
    recommendation: RecommendationResult?,
    readOnly: Boolean,
    onAddSet: (SetSide) -> Unit,
    onUpdateSet: (ExerciseSet) -> Unit,
    onCompleteSet: (ExerciseSet) -> Unit,
    onDeleteSet: (ExerciseSet) -> Unit,
    calculateStatsUseCase: CalculateStatsUseCase,
    bodyWeight: Double
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = exerciseWithSets.exercise.name, style = MaterialTheme.typography.titleLarge)
                
                val totalVolume = calculateStatsUseCase.calculateTotalVolume(
                    exerciseWithSets.sets.filter { it.isCompleted },
                    exerciseWithSets.exercise.projectileCount,
                    bodyWeight = bodyWeight,
                    isAssisted = exerciseWithSets.exercise.progressionType == ProgressionType.DECREASE
                )
                if (totalVolume > 0) {
                    Text(
                        stringResource(R.string.workout_volume_label, totalVolume.toInt()),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            
            recommendation?.let { rec ->
                val text = when (rec) {
                    is RecommendationResult.Bilateral -> stringResource(R.string.set_recommendation, rec.weight.toString())
                    is RecommendationResult.Unilateral -> {
                        val left = rec.leftWeight?.let { "Л: $it кг" } ?: ""
                        val right = rec.rightWeight?.let { "П: $it кг" } ?: ""
                        stringResource(R.string.set_recommendation_unilateral, "$left $right".trim())
                    }
                    else -> null
                }
                text?.let {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = it,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            val isUnilateral = exerciseWithSets.exercise.laterality == Laterality.UNILATERAL
            
            if (isUnilateral) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.exercise_side_left), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    exerciseWithSets.sets.filter { it.side == SetSide.LEFT }.forEach { set ->
                        SetRow(set, exerciseWithSets.exercise.isWeighted, readOnly, onUpdateSet, onCompleteSet, onDeleteSet, calculateStatsUseCase, bodyWeight, exerciseWithSets.exercise.progressionType == ProgressionType.DECREASE)
                    }
                    if (!readOnly) {
                        TextButton(onClick = { onAddSet(SetSide.LEFT) }) { Text(stringResource(R.string.set_add_left_btn)) }
                    }
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    Text(stringResource(R.string.exercise_side_right), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    exerciseWithSets.sets.filter { it.side == SetSide.RIGHT }.forEach { set ->
                        SetRow(set, exerciseWithSets.exercise.isWeighted, readOnly, onUpdateSet, onCompleteSet, onDeleteSet, calculateStatsUseCase, bodyWeight, exerciseWithSets.exercise.progressionType == ProgressionType.DECREASE)
                    }
                    if (!readOnly) {
                        TextButton(onClick = { onAddSet(SetSide.RIGHT) }) { Text(stringResource(R.string.set_add_right_btn)) }
                    }
                }
            } else {
                exerciseWithSets.sets.forEach { set ->
                    SetRow(set, exerciseWithSets.exercise.isWeighted, readOnly, onUpdateSet, onCompleteSet, onDeleteSet, calculateStatsUseCase, bodyWeight, exerciseWithSets.exercise.progressionType == ProgressionType.DECREASE)
                }
                if (!readOnly) {
                    Button(onClick = { onAddSet(SetSide.BOTH) }, modifier = Modifier.padding(top = 8.dp)) { 
                        Text(stringResource(R.string.set_add_btn)) 
                    }
                }
            }
        }
    }
}

@Composable
fun SetRow(
    set: ExerciseSet,
    isWeighted: Boolean,
    readOnly: Boolean,
    onUpdateSet: (ExerciseSet) -> Unit,
    onCompleteSet: (ExerciseSet) -> Unit,
    onDeleteSet: (ExerciseSet) -> Unit,
    calculateStatsUseCase: CalculateStatsUseCase,
    bodyWeight: Double,
    isAssisted: Boolean
) {
    // Formatting helper to avoid .0 when not needed
    fun formatDouble(d: Double?): String = when {
        d == null -> ""
        d % 1.0 == 0.0 -> d.toInt().toString()
        else -> d.toString()
    }

    var weightText by remember(set.id) { mutableStateOf(formatDouble(set.weight)) }
    var repsText by remember(set.id) { mutableStateOf(set.reps.toString()) }
    var noteText by remember(set.id) { mutableStateOf(set.note ?: "") }
    var showNote by remember { mutableStateOf(set.note?.isNotBlank() == true) }

    val isSetReadOnly = readOnly || set.isCompleted

    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            Text("${set.setNumber}.", modifier = Modifier.width(28.dp), style = MaterialTheme.typography.bodyMedium)
            
            if (isWeighted) {
                OutlinedTextField(
                    value = weightText,
                    onValueChange = { 
                        weightText = it
                        val newWeight = it.replace(',', '.').toDoubleOrNull()
                        if (newWeight != null || it.isEmpty()) {
                            onUpdateSet(set.copy(weight = newWeight))
                        }
                    },
                    enabled = !isSetReadOnly,
                    modifier = Modifier.width(90.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    label = { Text(stringResource(R.string.set_label_kg)) },
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            OutlinedTextField(
                value = repsText,
                onValueChange = { 
                    repsText = it
                    val newReps = it.toIntOrNull()
                    if (newReps != null) {
                        onUpdateSet(set.copy(reps = newReps))
                    } else if (it.isEmpty()) {
                        onUpdateSet(set.copy(reps = 0))
                    }
                },
                enabled = !isSetReadOnly,
                modifier = Modifier.width(80.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                label = { Text(stringResource(R.string.set_label_reps)) },
                singleLine = true
            )
            
            if (!isSetReadOnly) {
                IconButton(onClick = { onCompleteSet(set) }) {
                    Icon(Icons.Default.Done, contentDescription = stringResource(R.string.workout_complete), tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = { showNote = !showNote }) {
                    Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.set_note_hint), modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = { onDeleteSet(set) }) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.workout_cancel))
                }
            } else if (set.isCompleted) {
                Icon(
                    Icons.Default.CheckCircle, 
                    contentDescription = null, 
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 8.dp).size(24.dp)
                )
                if (isWeighted) {
                    val onerepmax = calculateStatsUseCase.calculateOneRepMax(set.weight, set.reps, bodyWeight, isAssisted)
                    if (onerepmax > 0) {
                        Text(
                            "1RM: ${String.format(Locale.getDefault(), "%.1f", onerepmax)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }
        }
        
        if (showNote) {
            OutlinedTextField(
                value = noteText,
                onValueChange = { 
                    noteText = it
                    onUpdateSet(set.copy(note = it))
                },
                enabled = !isSetReadOnly,
                modifier = Modifier.fillMaxWidth().padding(start = 28.dp, bottom = 8.dp),
                label = { Text(stringResource(R.string.set_note_hint)) },
                textStyle = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun RenameWorkoutDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.workout_rename)) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.workout_rename)) },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(name) }) {
                Text(stringResource(R.string.weight_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.workout_cancel)) }
        }
    )
}

@Composable
fun ExercisePickerDialog(
    exercises: List<Exercise>,
    onDismiss: () -> Unit,
    onSelect: (Exercise) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card {
            LazyColumn(modifier = Modifier.padding(16.dp).fillMaxWidth().heightIn(max = 400.dp)) {
                items(exercises) { exercise ->
                    Text(
                        text = exercise.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(exercise) }
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}
