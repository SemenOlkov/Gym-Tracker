package hihihiha.semchik2017.gymtracker.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hihihiha.semchik2017.gymtracker.data.model.AppBackup
import hihihiha.semchik2017.gymtracker.domain.repository.BackupRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val backupRepository: BackupRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val json = Json { 
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    fun exportBackup(outputStream: OutputStream) {
        viewModelScope.launch {
            try {
                val backup = backupRepository.createBackup()
                val jsonString = json.encodeToString(backup)
                outputStream.use { it.write(jsonString.toByteArray()) }
                _uiState.value = _uiState.value.copy(successMessage = "Backup exported successfully")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun importBackup(inputStream: InputStream) {
        viewModelScope.launch {
            try {
                val jsonString = inputStream.bufferedReader().use { it.readText() }
                val backup = json.decodeFromString<AppBackup>(jsonString)
                backupRepository.restoreBackup(backup)
                _uiState.value = _uiState.value.copy(successMessage = "Backup restored successfully")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(successMessage = null, errorMessage = null)
    }
}

data class SettingsUiState(
    val successMessage: String? = null,
    val errorMessage: String? = null
)
