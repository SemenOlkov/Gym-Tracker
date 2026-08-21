package hihihiha.semchik2017.gymtracker.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import hihihiha.semchik2017.gymtracker.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSettingsScreen(
    viewModel: AppSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.nav_app_settings)) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Rest Timer
            Column {
                Text(text = stringResource(R.string.settings_app_rest_timer), style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Slider(
                        value = uiState.restTime.toFloat(),
                        onValueChange = { viewModel.updateRestTime(it.toInt()) },
                        valueRange = 30f..300f,
                        steps = 8,
                        modifier = Modifier.weight(1f)
                    )
                    Text(text = "${uiState.restTime}с", modifier = Modifier.padding(start = 8.dp))
                }
            }

            // Weight Unit
            Column {
                Text(text = stringResource(R.string.settings_app_weight_unit), style = MaterialTheme.typography.titleMedium)
                Row(modifier = Modifier.selectableGroup()) {
                    listOf("kg", "lb").forEach { unit ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 16.dp)) {
                            RadioButton(
                                selected = uiState.weightUnit == unit,
                                onClick = { viewModel.updateWeightUnit(unit) }
                            )
                            Text(text = unit)
                        }
                    }
                }
            }

            // Theme Mode
            Column {
                Text(text = stringResource(R.string.settings_app_theme), style = MaterialTheme.typography.titleMedium)
                val themes = listOf(
                    "system" to R.string.settings_app_theme_system,
                    "light" to R.string.settings_app_theme_light,
                    "dark" to R.string.settings_app_theme_dark
                )
                themes.forEach { (mode, labelRes) ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        RadioButton(
                            selected = uiState.themeMode == mode,
                            onClick = { viewModel.updateThemeMode(mode) }
                        )
                        Text(text = stringResource(labelRes))
                    }
                }
            }

            // Dynamic Colors
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.settings_app_dynamic_colors)) },
                    supportingContent = { Text(stringResource(R.string.settings_app_dynamic_colors_desc)) },
                    trailingContent = {
                        Switch(
                            checked = uiState.dynamicColors,
                            onCheckedChange = { viewModel.updateDynamicColors(it) }
                        )
                    }
                )
            }
        }
    }
}
