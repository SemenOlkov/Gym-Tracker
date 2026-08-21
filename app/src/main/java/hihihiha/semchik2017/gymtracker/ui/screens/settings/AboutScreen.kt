package hihihiha.semchik2017.gymtracker.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import hihihiha.semchik2017.gymtracker.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen() {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.nav_about)) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.FitnessCenter,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = stringResource(R.string.about_version, "3.0.0"),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = stringResource(R.string.about_desc),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Text(
                text = "© 2026 WETH",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}
