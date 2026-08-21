package hihihiha.semchik2017.gymtracker.ui.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hihihiha.semchik2017.gymtracker.data.model.Exercise
import hihihiha.semchik2017.gymtracker.domain.repository.GymRepository
import hihihiha.semchik2017.gymtracker.domain.repository.SettingsRepository
import hihihiha.semchik2017.gymtracker.domain.usecase.ExerciseStatPoint
import hihihiha.semchik2017.gymtracker.domain.usecase.GetExerciseStatsUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val repository: GymRepository,
    private val settingsRepository: SettingsRepository,
    private val getExerciseStatsUseCase: GetExerciseStatsUseCase
) : ViewModel() {

    private val _selectedExercise = MutableStateFlow<Exercise?>(null)
    val selectedExercise: StateFlow<Exercise?> = _selectedExercise.asStateFlow()

    private val _stats = MutableStateFlow<List<ExerciseStatPoint>>(emptyList())
    val stats: StateFlow<List<ExerciseStatPoint>> = _stats.asStateFlow()

    val weightUnit = settingsRepository.weightUnit
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "kg")


    val allExercises = repository.getAllExercises()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectExercise(exercise: Exercise) {
        _selectedExercise.value = exercise
        viewModelScope.launch {
            _stats.value = getExerciseStatsUseCase(exercise.id)
        }
    }
}
