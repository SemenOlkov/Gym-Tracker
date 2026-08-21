package hihihiha.semchik2017.gymtracker.ui.screens.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import hihihiha.semchik2017.gymtracker.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val backupFileName = stringResource(R.string.settings_backup_file_name)
    
    var showRestoreConfirm by remember { mutableStateOf(false) }
    var pendingRestoreUri by remember { mutableStateOf<Uri?>(null) }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            context.contentResolver.openOutputStream(it)?.let { outputStream ->
                viewModel.exportBackup(outputStream)
            }
        }
    }

    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            pendingRestoreUri = it
            showRestoreConfirm = true
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.nav_backup)) })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text(
                text = stringResource(R.string.settings_backup_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Button(
                        onClick = { createDocumentLauncher.launch(backupFileName) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.settings_backup_create))
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    TextButton(
                        onClick = { openDocumentLauncher.launch(arrayOf("application/json")) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.FileUpload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.settings_backup_restore))
                    }
                }
            }
        }
    }

    if (showRestoreConfirm) {
        AlertDialog(
            onDismissRequest = { showRestoreConfirm = false },
            title = { Text(stringResource(R.string.settings_backup_restore_confirm_title)) },
            text = { Text(stringResource(R.string.settings_backup_restore_confirm_msg)) },
            confirmButton = {
                Button(
                    onClick = {
                        pendingRestoreUri?.let { uri ->
                            context.contentResolver.openInputStream(uri)?.let { inputStream ->
                                viewModel.importBackup(inputStream)
                            }
                        }
                        showRestoreConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.settings_backup_restore))
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreConfirm = false }) {
                    Text(stringResource(R.string.workout_cancel))
                }
            }
        )
    }
}
