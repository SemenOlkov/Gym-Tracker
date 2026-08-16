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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
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
    val workoutWithExercises by viewModel.workoutWithExercises.collectAsState()
    val allExercises by viewModel.allExercises.collectAsState()
    val recommendations by viewModel.recommendations.collectAsState()
    val timerSeconds by viewModel.timerSeconds.collectAsState()
    var showAddExerciseDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showCompleteConfirmDialog by remember { mutableStateOf(false) }

    val isCompleted = workoutWithExercises?.workout?.isCompleted == true

    LaunchedEffect(workoutId) {
        viewModel.loadWorkout(workoutId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(workoutWithExercises?.workout?.name ?: "Тренировка")
                            if (isCompleted) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    Icons.Default.CheckCircle, 
                                    contentDescription = "Завершена",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        workoutWithExercises?.workout?.date?.let {
                            Text(
                                SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(it)),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
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
                                    text = String.format("%02d:%02d", timerSeconds / 60, timerSeconds % 60),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    if (!isCompleted) {
                        IconButton(onClick = { showRenameDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Переименовать")
                        }
                        Button(
                            onClick = { showCompleteConfirmDialog = true },
                            modifier = Modifier.padding(end = 8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text("Завершить", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (!isCompleted) {
                FloatingActionButton(onClick = { showAddExerciseDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Добавить упражнение")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            val exercises = workoutWithExercises?.exercises?.sortedBy { it.workoutExercise.orderIndex } ?: emptyList()
                    items(exercises) { exerciseWithSets ->
                ExerciseCard(
                    exerciseWithSets = exerciseWithSets,
                    recommendation = recommendations[exerciseWithSets.workoutExercise.id],
                    readOnly = isCompleted,
                    onAddSet = { side -> viewModel.addSet(exerciseWithSets.workoutExercise.id, side) },
                    onUpdateSet = { viewModel.updateSet(it) },
                    onCompleteSet = { viewModel.completeSet(it) },
                    onDeleteSet = { viewModel.deleteSet(it) },
                    calculateStatsUseCase = viewModel.calculateStatsUseCase
                )
            }
        }
    }

    if (showCompleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showCompleteConfirmDialog = false },
            title = { Text("Завершить тренировку?") },
            text = { Text("После завершения тренировку нельзя будет редактировать, и её данные попадут в статистику и рекомендации.") },
            confirmButton = {
                Button(onClick = {
                    viewModel.completeWorkout()
                    showCompleteConfirmDialog = false
                }) {
                    Text("Да, завершить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCompleteConfirmDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }

    if (showRenameDialog) {
        RenameWorkoutDialog(
            currentName = workoutWithExercises?.workout?.name ?: "",
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
    calculateStatsUseCase: CalculateStatsUseCase
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
                
                val totalVolume = calculateStatsUseCase.calculateTotalVolume(exerciseWithSets.sets.filter { it.isCompleted })
                if (totalVolume > 0) {
                    Text(
                        "Объем: ${totalVolume} кг",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            
            recommendation?.let { rec ->
                val text = when (rec) {
                    is RecommendationResult.Bilateral -> "Рекомендуемый вес: ${rec.weight} кг"
                    is RecommendationResult.Unilateral -> {
                        val left = rec.leftWeight?.let { "Л: $it кг" } ?: ""
                        val right = rec.rightWeight?.let { "П: $it кг" } ?: ""
                        "Рекомендация: $left $right".trim()
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
                    Text("Левая сторона", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    exerciseWithSets.sets.filter { it.side == SetSide.LEFT }.forEach { set ->
                        SetRow(set, readOnly, onUpdateSet, onCompleteSet, onDeleteSet, calculateStatsUseCase)
                    }
                    if (!readOnly) {
                        TextButton(onClick = { onAddSet(SetSide.LEFT) }) { Text("+ Подход (Л)") }
                    }
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    Text("Правая сторона", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    exerciseWithSets.sets.filter { it.side == SetSide.RIGHT }.forEach { set ->
                        SetRow(set, readOnly, onUpdateSet, onCompleteSet, onDeleteSet, calculateStatsUseCase)
                    }
                    if (!readOnly) {
                        TextButton(onClick = { onAddSet(SetSide.RIGHT) }) { Text("+ Подход (П)") }
                    }
                }
            } else {
                exerciseWithSets.sets.forEach { set ->
                    SetRow(set, readOnly, onUpdateSet, onCompleteSet, onDeleteSet, calculateStatsUseCase)
                }
                if (!readOnly) {
                    Button(onClick = { onAddSet(SetSide.BOTH) }, modifier = Modifier.padding(top = 8.dp)) { 
                        Text("+ Подход") 
                    }
                }
            }
        }
    }
}

@Composable
fun SetRow(
    set: ExerciseSet,
    readOnly: Boolean,
    onUpdateSet: (ExerciseSet) -> Unit,
    onCompleteSet: (ExerciseSet) -> Unit,
    onDeleteSet: (ExerciseSet) -> Unit,
    calculateStatsUseCase: CalculateStatsUseCase
) {
    var weightText by remember(set.id) { mutableStateOf(set.weight?.toString() ?: "") }
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
                label = { Text("кг") },
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
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
                label = { Text("повт") },
                singleLine = true
            )
            
            if (!isSetReadOnly) {
                IconButton(onClick = { onCompleteSet(set) }) {
                    Icon(Icons.Default.Done, contentDescription = "Завершить подход", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = { showNote = !showNote }) {
                    Icon(Icons.Default.Edit, contentDescription = "Заметка", modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = { onDeleteSet(set) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Удалить подход")
                }
            } else if (set.isCompleted) {
                Icon(
                    Icons.Default.CheckCircle, 
                    contentDescription = null, 
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 8.dp).size(24.dp)
                )
                val onerepmax = calculateStatsUseCase.calculateOneRepMax(set.weight, set.reps)
                if (onerepmax > 0) {
                    Text(
                        "1RM: ${String.format(Locale.getDefault(), "%.1f", onerepmax)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
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
                label = { Text("Заметка") },
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
        title = { Text("Переименовать тренировку") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Название") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(name) }) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
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
