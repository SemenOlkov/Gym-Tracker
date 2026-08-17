package hihihiha.semchik2017.gymtracker.ui.screens.nutrition

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import hihihiha.semchik2017.gymtracker.R
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutritionListScreen(
    onDayClick: (Long) -> Unit,
    viewModel: NutritionListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.nav_nutrition)) })
        }
    ) { padding ->
        val calModel = remember { CartesianChartModelProducer() }
        val proModel = remember { CartesianChartModelProducer() }
        val fatModel = remember { CartesianChartModelProducer() }
        val carbModel = remember { CartesianChartModelProducer() }
        
        val filteredDays = remember(uiState.days, uiState.selectedPeriod) {
            uiState.days.filter { summarizedDay ->
                val dayCal = Calendar.getInstance().apply { timeInMillis = summarizedDay.day.date }
                when (uiState.selectedPeriod) {
                    NutritionPeriod.WEEK -> {
                        val weekAgo = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -7) }
                        dayCal.after(weekAgo)
                    }
                    NutritionPeriod.MONTH -> {
                        val monthAgo = Calendar.getInstance().apply { add(Calendar.MONTH, -1) }
                        dayCal.after(monthAgo)
                    }
                    NutritionPeriod.YEAR -> {
                        val yearAgo = Calendar.getInstance().apply { add(Calendar.YEAR, -1) }
                        dayCal.after(yearAgo)
                    }
                }
            }.sortedBy { it.day.date }
        }

        LaunchedEffect(filteredDays) {
            if (filteredDays.isNotEmpty()) {
                calModel.runTransaction { lineSeries { series(filteredDays.map { it.totalCalories }) } }
                proModel.runTransaction { lineSeries { series(filteredDays.map { it.totalProteins }) } }
                fatModel.runTransaction { lineSeries { series(filteredDays.map { it.totalFats }) } }
                carbModel.runTransaction { lineSeries { series(filteredDays.map { it.totalCarbs }) } }
            }
        }

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
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    PeriodSelector(uiState.selectedPeriod, onPeriodSelect = { viewModel.setPeriod(it) })
                }
                
                if (filteredDays.isNotEmpty()) {
                    item {
                        Text(stringResource(R.string.nutrition_calories), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        CartesianChartHost(
                            chart = rememberCartesianChart(
                                rememberLineCartesianLayer(),
                                startAxis = rememberStartAxis(),
                                bottomAxis = rememberBottomAxis(),
                            ),
                            modelProducer = calModel,
                            modifier = Modifier.fillMaxWidth().height(150.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(stringResource(R.string.nutrition_proteins_short), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        CartesianChartHost(
                            chart = rememberCartesianChart(
                                rememberLineCartesianLayer(),
                                startAxis = rememberStartAxis(),
                                bottomAxis = rememberBottomAxis(),
                            ),
                            modelProducer = proModel,
                            modifier = Modifier.fillMaxWidth().height(150.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(stringResource(R.string.nutrition_fats_short), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        CartesianChartHost(
                            chart = rememberCartesianChart(
                                rememberLineCartesianLayer(),
                                startAxis = rememberStartAxis(),
                                bottomAxis = rememberBottomAxis(),
                            ),
                            modelProducer = fatModel,
                            modifier = Modifier.fillMaxWidth().height(150.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(stringResource(R.string.nutrition_carbs_short), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        CartesianChartHost(
                            chart = rememberCartesianChart(
                                rememberLineCartesianLayer(),
                                startAxis = rememberStartAxis(),
                                bottomAxis = rememberBottomAxis(),
                            ),
                            modelProducer = carbModel,
                            modifier = Modifier.fillMaxWidth().height(150.dp)
                        )
                    }
                }

                items(uiState.days) { summarizedDay ->
                    NutritionDayCard(summarizedDay, onClick = { onDayClick(summarizedDay.day.id) })
                }
            }
        }
    }
}

@Composable
fun PeriodSelector(selected: NutritionPeriod, onPeriodSelect: (NutritionPeriod) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(stringResource(R.string.nutrition_chart_period))
        NutritionPeriod.entries.forEach { period ->
            FilterChip(
                selected = selected == period,
                onClick = { onPeriodSelect(period) },
                label = {
                    Text(
                        when (period) {
                            NutritionPeriod.WEEK -> stringResource(R.string.nutrition_period_week)
                            NutritionPeriod.MONTH -> stringResource(R.string.nutrition_period_month)
                            NutritionPeriod.YEAR -> stringResource(R.string.nutrition_period_year)
                        }
                    )
                }
            )
        }
    }
}

@Composable
fun NutritionDayCard(
    summarizedDay: NutritionDayWithSummary,
    onClick: () -> Unit
) {
    val date = Date(summarizedDay.day.date)
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    val dateStr = dateFormat.format(date)
    
    val isToday = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date()) == dateStr
    val displayDate = if (isToday) stringResource(R.string.nutrition_today) else dateStr

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = displayDate,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (summarizedDay.day.isClosed) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = stringResource(R.string.nutrition_closed_day),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                NutritionInfoItem(summarizedDay.totalCalories.toInt().toString(), stringResource(R.string.nutrition_calories))
                NutritionInfoItem(String.format(Locale.getDefault(), "%.1f", summarizedDay.totalProteins), stringResource(R.string.nutrition_proteins_short))
                NutritionInfoItem(String.format(Locale.getDefault(), "%.1f", summarizedDay.totalFats), stringResource(R.string.nutrition_fats_short))
                NutritionInfoItem(String.format(Locale.getDefault(), "%.1f", summarizedDay.totalCarbs), stringResource(R.string.nutrition_carbs_short))
            }
        }
    }
}

@Composable
fun NutritionInfoItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
    }
}
