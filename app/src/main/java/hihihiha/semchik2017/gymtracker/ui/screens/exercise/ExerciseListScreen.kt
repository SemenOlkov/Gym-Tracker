package hihihiha.semchik2017.gymtracker.ui.screens.exercise

import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.res.stringResource
import hihihiha.semchik2017.gymtracker.R
import hihihiha.semchik2017.gymtracker.data.model.*

@Composable
fun ExerciseListScreen(
    onExerciseClick: (Long) -> Unit,
    viewModel: ExerciseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var exerciseToDelete by remember { mutableStateOf<Exercise?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.workout_create))
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
            LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
                items(uiState.exercises) { exercise ->
                    ExerciseItem(
                        exercise = exercise,
                        onClick = { onExerciseClick(exercise.id) },
                        onDelete = if (exercise.isCustom) { { exerciseToDelete = exercise } } else null
                    )
                }
            }
        }
    }

    if (exerciseToDelete != null) {
        AlertDialog(
            onDismissRequest = { exerciseToDelete = null },
            title = { Text(stringResource(R.string.delete_confirm_title)) },
            text = { Text(stringResource(R.string.delete_confirm_msg)) },
            confirmButton = {
                Button(
                    onClick = {
                        exerciseToDelete?.let { viewModel.deleteExercise(it) }
                        exerciseToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.delete_confirm_btn))
                }
            },
            dismissButton = {
                TextButton(onClick = { exerciseToDelete = null }) {
                    Text(stringResource(R.string.workout_cancel))
                }
            }
        )
    }

    if (showAddDialog) {
        CreateExerciseDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, isWeighted, laterality, projectileCount, progressionType, muscleGroups, instructions ->
                viewModel.createExercise(name, isWeighted, laterality, projectileCount, progressionType, muscleGroups, instructions)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ExerciseItem(exercise: Exercise, onClick: () -> Unit, onDelete: (() -> Unit)?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = exercise.name, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = "${if (exercise.isWeighted) "С весом" else "Без веса"}, ${if (exercise.laterality == Laterality.BILATERAL) "Билатеральное" else "Монолатеральное"}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (onDelete != null) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Удалить")
                }
            }
        }
    }
}

@Composable
fun CreateExerciseDialog(onDismiss: () -> Unit, onConfirm: (String, Boolean, Laterality, Int, ProgressionType, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var isWeighted by remember { mutableStateOf(true) }
    var laterality by remember { mutableStateOf(Laterality.BILATERAL) }
    var progressionType by remember { mutableStateOf(ProgressionType.INCREASE) }
    var projectileCount by remember { mutableStateOf(1) }
    var muscleGroups by remember { mutableStateOf("") }
    var instructions by remember { mutableStateOf("") }

    // Enforce projectile rules based on weight and laterality
    LaunchedEffect(isWeighted, laterality) {
        if (!isWeighted) {
            projectileCount = 0
        } else {
            if (laterality == Laterality.UNILATERAL) {
                projectileCount = 1
            } else if (projectileCount == 0) {
                // If it was unweighted (0) and we turn weight on, default to 1
                projectileCount = 1
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новое упражнение") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = muscleGroups,
                    onValueChange = { muscleGroups = it },
                    label = { Text("Группы мышц") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = instructions,
                    onValueChange = { instructions = it },
                    label = { Text("Инструкция") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isWeighted, onCheckedChange = { isWeighted = it })
                    Text("Есть вес")
                }
                Text("Тип:", style = MaterialTheme.typography.labelMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = laterality == Laterality.BILATERAL,
                        onClick = { laterality = Laterality.BILATERAL }
                    )
                    Text("Билатеральное")
                    RadioButton(
                        selected = laterality == Laterality.UNILATERAL,
                        onClick = { laterality = Laterality.UNILATERAL }
                    )
                    Text("Монолатеральное")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Количество снарядов:", style = MaterialTheme.typography.labelMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    (0..2).forEach { count ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 8.dp)) {
                            RadioButton(
                                selected = projectileCount == count,
                                onClick = { projectileCount = count },
                                enabled = when {
                                    !isWeighted -> count == 0
                                    laterality == Laterality.UNILATERAL -> count == 1
                                    else -> count > 0 // Weighted + Bilateral
                                }
                            )
                            Text(text = count.toString())
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Влияние веса:", style = MaterialTheme.typography.labelMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = progressionType == ProgressionType.INCREASE,
                        onClick = { progressionType = ProgressionType.INCREASE }
                    )
                    Text("С весом сложнее")
                    RadioButton(
                        selected = progressionType == ProgressionType.DECREASE,
                        onClick = { progressionType = ProgressionType.DECREASE }
                    )
                    Text("С весом легче (помощь)")
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(name, isWeighted, laterality, projectileCount, progressionType, muscleGroups, instructions) }, enabled = name.isNotBlank()) {
                Text("Создать")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}
