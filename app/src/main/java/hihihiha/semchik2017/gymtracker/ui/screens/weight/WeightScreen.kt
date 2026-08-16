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
import hihihiha.semchik2017.gymtracker.data.model.BodyWeight
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WeightScreen(
    viewModel: WeightViewModel = hiltViewModel()
) {
    val weightHistory by viewModel.weightHistory.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    
    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(weightHistory) {
        if (weightHistory.isNotEmpty()) {
            modelProducer.runTransaction {
                lineSeries {
                    series(weightHistory.reversed().map { it.weight })
                }
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Записать вес")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (weightHistory.isNotEmpty()) {
                Text(
                    "График веса",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )
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
                            title = "Дата",
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
                    modifier = Modifier.fillMaxWidth().height(250.dp).padding(horizontal = 16.dp)
                )
            }
            
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(weightHistory) { entry ->
                    WeightItem(
                        entry = entry,
                        onDelete = { viewModel.deleteWeight(entry) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddWeightDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { 
                viewModel.addWeight(it)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun WeightItem(entry: BodyWeight, onDelete: () -> Unit) {
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
                Text(text = "${entry.weight} кг", style = MaterialTheme.typography.titleMedium)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Удалить", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun AddWeightDialog(onDismiss: () -> Unit, onConfirm: (Double) -> Unit) {
    var weightStr by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Записать вес тела") },
        text = {
            OutlinedTextField(
                value = weightStr,
                onValueChange = { weightStr = it.replace(',', '.') },
                label = { Text("Вес (кг)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = { weightStr.toDoubleOrNull()?.let { onConfirm(it) } },
                enabled = weightStr.toDoubleOrNull() != null
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}
