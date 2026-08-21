package hihihiha.semchik2017.gymtracker.ui.screens.weight

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
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
import hihihiha.semchik2017.gymtracker.data.model.BodyWeight
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WeightScreen(
    viewModel: WeightViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var weightToDelete by remember { mutableStateOf<BodyWeight?>(null) }
    
    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(uiState.weightHistory) {
        if (uiState.weightHistory.isNotEmpty()) {
            modelProducer.runTransaction {
                lineSeries {
                    series(uiState.weightHistory.reversed().map { if (uiState.unit == "lb") it.weightLb else it.weightKg })
                }
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.weight_add_title))
            }
        }
    ) { padding ->
        val dates = remember(uiState.weightHistory) { uiState.weightHistory.reversed().map { it.date } }
        
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.errorMessage != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(uiState.errorMessage!!, color = MaterialTheme.colorScheme.error)
            }
        } else {
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                if (uiState.weightHistory.isNotEmpty()) {
                    Text(
                        stringResource(R.string.weight_chart_title),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                    CartesianChartHost(
                        chart = rememberCartesianChart(
                            rememberLineCartesianLayer(),
                            startAxis = rememberStartAxis(
                                label = rememberTextComponent(color = MaterialTheme.colorScheme.onSurface),
                                title = uiState.unit,
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
                        modifier = Modifier.fillMaxWidth().height(250.dp).padding(horizontal = 16.dp)
                    )
                }
                
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(uiState.weightHistory) { entry ->
                        WeightItem(
                            entry = entry,
                            unit = uiState.unit,
                            onDelete = { weightToDelete = entry }
                        )
                    }
                }
            }
        }
    }

    if (weightToDelete != null) {
        AlertDialog(
            onDismissRequest = { weightToDelete = null },
            title = { Text(stringResource(R.string.delete_confirm_title)) },
            text = { Text(stringResource(R.string.delete_confirm_msg)) },
            confirmButton = {
                Button(
                    onClick = {
                        weightToDelete?.let { viewModel.deleteWeight(it) }
                        weightToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.delete_confirm_btn))
                }
            },
            dismissButton = {
                TextButton(onClick = { weightToDelete = null }) {
                    Text(stringResource(R.string.workout_cancel))
                }
            }
        )
    }

    if (showAddDialog) {
        AddWeightDialog(
            unit = uiState.unit,
            onDismiss = { showAddDialog = false },
            onConfirm = { 
                viewModel.addWeight(it)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun WeightItem(entry: BodyWeight, unit: String, onDelete: () -> Unit) {
    val dateStr = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(entry.date))
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = dateStr, style = MaterialTheme.typography.bodyLarge)
                val weightVal = if (unit == "lb") entry.weightLb else entry.weightKg
                Text(text = String.format(Locale.getDefault(), "%.2f %s", weightVal, unit), style = MaterialTheme.typography.titleMedium)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.workout_cancel), tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun AddWeightDialog(unit: String, onDismiss: () -> Unit, onConfirm: (Double) -> Unit) {
    var weightStr by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.weight_add_title)) },
        text = {
            OutlinedTextField(
                value = weightStr,
                onValueChange = { weightStr = it.replace(',', '.') },
                label = { Text("${stringResource(R.string.weight_hint)} ($unit)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = { weightStr.toDoubleOrNull()?.let { onConfirm(it) } },
                enabled = weightStr.toDoubleOrNull() != null
            ) {
                Text(stringResource(R.string.weight_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.workout_cancel)) }
        }
    )
}
