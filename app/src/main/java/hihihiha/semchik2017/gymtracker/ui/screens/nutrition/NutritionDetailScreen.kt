package hihihiha.semchik2017.gymtracker.ui.screens.nutrition

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import hihihiha.semchik2017.gymtracker.R
import hihihiha.semchik2017.gymtracker.data.model.Product
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutritionDetailScreen(
    dayId: Long,
    onBack: () -> Unit,
    viewModel: NutritionDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(dayId) {
        viewModel.loadDayDetails(dayId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    val date = uiState.day?.let { Date(it.date) } ?: Date()
                    Text(SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(date))
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        },
        floatingActionButton = {
            if (uiState.day?.isClosed == false) {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = null)
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
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                SummarySection(
                    calories = uiState.entries.sumOf { it.calories },
                    proteins = uiState.entries.sumOf { it.proteins },
                    fats = uiState.entries.sumOf { it.fats },
                    carbs = uiState.entries.sumOf { it.carbs }
                )
                
                HorizontalDivider()
                
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.entries) { entry ->
                        EntryItem(
                            entry = entry,
                            onDelete = if (uiState.day?.isClosed == false) { { viewModel.deleteEntry(entry.entryId) } } else null
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddNutritionEntryDialog(
            onDismiss = { showAddDialog = false },
            onSearch = { viewModel.searchProducts(it) },
            searchResults = uiState.searchResults,
            onAddExisting = { product, weight ->
                viewModel.addEntry(product.id, weight)
                showAddDialog = false
            },
            onCreateNew = { name, cal, pro, fat, carb, weight ->
                viewModel.createAndAddProduct(name, cal, pro, fat, carb, weight)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun SummarySection(calories: Double, proteins: Double, fats: Double, carbs: Double) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        SummaryItem(calories.toInt().toString(), stringResource(R.string.nutrition_calories))
        SummaryItem(String.format(Locale.getDefault(), "%.1f", proteins), stringResource(R.string.nutrition_proteins_short))
        SummaryItem(String.format(Locale.getDefault(), "%.1f", fats), stringResource(R.string.nutrition_fats_short))
        SummaryItem(String.format(Locale.getDefault(), "%.1f", carbs), stringResource(R.string.nutrition_carbs_short))
    }
}

@Composable
fun SummaryItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(text = label, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
fun EntryItem(entry: hihihiha.semchik2017.gymtracker.data.model.NutritionEntryWithProduct, onDelete: (() -> Unit)?) {
    val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(entry.timestamp))
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = entry.productName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(
                    text = "${entry.weight.toInt()}г | ${entry.calories.toInt()} ккал | Б:${entry.proteins.toInt()} Ж:${entry.fats.toInt()} У:${entry.carbs.toInt()}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(text = timeStr, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 8.dp))
            if (onDelete != null) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                }
            }
        }
    }
}

@Composable
fun AddNutritionEntryDialog(
    onDismiss: () -> Unit,
    onSearch: (String) -> Unit,
    searchResults: List<Product>,
    onAddExisting: (Product, Double) -> Unit,
    onCreateNew: (String, Double, Double, Double, Double, Double) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var weightText by remember { mutableStateOf("") }
    var isCreatingNew by remember { mutableStateOf(false) }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    
    // Create new state
    var newName by remember { mutableStateOf("") }
    var newCal by remember { mutableStateOf("") }
    var newPro by remember { mutableStateOf("") }
    var newFat by remember { mutableStateOf("") }
    var newCarb by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isCreatingNew) stringResource(R.string.nutrition_create_product) else stringResource(R.string.nutrition_add_entry)) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (!isCreatingNew) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { 
                            query = it
                            selectedProduct = null
                            onSearch(it)
                        },
                        label = { Text(stringResource(R.string.nutrition_search_product)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (searchResults.isNotEmpty() && selectedProduct == null) {
                        LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                            items(searchResults) { product ->
                                ListItem(
                                    headlineContent = { Text(product.name) },
                                    supportingContent = { Text("${product.calories.toInt()} ккал | Б:${product.proteins.toInt()} Ж:${product.fats.toInt()} У:${product.carbs.toInt()} ${stringResource(R.string.nutrition_per_100g)}") },
                                    modifier = Modifier.clickable { 
                                        selectedProduct = product
                                        query = product.name
                                    }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = weightText,
                        onValueChange = { weightText = it },
                        label = { Text(stringResource(R.string.nutrition_weight_hint)) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    
                    TextButton(onClick = { isCreatingNew = true }) {
                        Text(stringResource(R.string.nutrition_create_product))
                    }
                } else {
                    OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text(stringResource(R.string.nutrition_product_name)) }, modifier = Modifier.fillMaxWidth())
                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(value = newCal, onValueChange = { newCal = it }, label = { Text(stringResource(R.string.nutrition_calories_short)) }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        Spacer(modifier = Modifier.width(4.dp))
                        OutlinedTextField(value = newPro, onValueChange = { newPro = it }, label = { Text(stringResource(R.string.nutrition_proteins_short)) }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(value = newFat, onValueChange = { newFat = it }, label = { Text(stringResource(R.string.nutrition_fats_short)) }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        Spacer(modifier = Modifier.width(4.dp))
                        OutlinedTextField(value = newCarb, onValueChange = { newCarb = it }, label = { Text(stringResource(R.string.nutrition_carbs_short)) }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    }
                    OutlinedTextField(value = weightText, onValueChange = { weightText = it }, label = { Text(stringResource(R.string.nutrition_weight_hint)) }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    
                    TextButton(onClick = { isCreatingNew = false }) {
                        Text("Вернуться к поиску")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val weight = weightText.toDoubleOrNull() ?: 0.0
                    if (isCreatingNew) {
                        onCreateNew(
                            newName,
                            newCal.toDoubleOrNull() ?: 0.0,
                            newPro.toDoubleOrNull() ?: 0.0,
                            newFat.toDoubleOrNull() ?: 0.0,
                            newCarb.toDoubleOrNull() ?: 0.0,
                            weight
                        )
                    } else {
                        selectedProduct?.let { onAddExisting(it, weight) }
                    }
                },
                enabled = weightText.isNotEmpty() && (isCreatingNew && newName.isNotEmpty() || !isCreatingNew && selectedProduct != null)
            ) {
                Text("Добавить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.workout_cancel)) }
        }
    )
}
