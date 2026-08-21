package hihihiha.semchik2017.gymtracker.ui.screens.workout

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import hihihiha.semchik2017.gymtracker.R
import hihihiha.semchik2017.gymtracker.data.model.Workout
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WorkoutListScreen(
    onWorkoutClick: (Long) -> Unit,
    viewModel: WorkoutViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var workoutToDelete by remember { mutableStateOf<Workout?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.workout_new))
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
                    .fillMaxSize()
                    .padding(padding)
            ) {
                uiState.groupedWorkouts.forEach { (month, monthWorkouts) ->
                    item {
                        Text(
                            text = month,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    items(monthWorkouts) { workout ->
                        WorkoutItem(
                            workout = workout,
                            onClick = { onWorkoutClick(workout.id) },
                            onDelete = { workoutToDelete = workout }
                        )
                    }
                }
            }
        }
    }

    if (workoutToDelete != null) {
        AlertDialog(
            onDismissRequest = { workoutToDelete = null },
            title = { Text(stringResource(R.string.delete_confirm_title)) },
            text = { Text(stringResource(R.string.delete_confirm_msg)) },
            confirmButton = {
                Button(
                    onClick = {
                        workoutToDelete?.let { viewModel.deleteWorkout(it) }
                        workoutToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.delete_confirm_btn))
                }
            },
            dismissButton = {
                TextButton(onClick = { workoutToDelete = null }) {
                    Text(stringResource(R.string.workout_cancel))
                }
            }
        )
    }

    if (showCreateDialog) {
        CreateWorkoutDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { name ->
                viewModel.createWorkout(name)
                showCreateDialog = false
            }
        )
    }
}

@Composable
fun CreateWorkoutDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.workout_new)) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.workout_name_hint)) },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(name) }, enabled = name.isNotBlank()) {
                Text(stringResource(R.string.workout_create))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.workout_cancel)) }
        }
    )
}

@Composable
fun WorkoutItem(
    workout: Workout,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = remember(workout.date) {
        SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(workout.date))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                if (workout.name != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = workout.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (workout.isCompleted) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.Default.CheckCircle, 
                                contentDescription = null, 
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
                Text(
                    text = if (workout.name == null && workout.isCompleted) "✓ " + stringResource(R.string.workout_default_name, dateStr) else stringResource(R.string.workout_default_name, dateStr),
                    style = if (workout.name == null) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodySmall
                )
            }
            if (!workout.isCompleted) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.workout_cancel))
                }
            }
        }
    }
}
