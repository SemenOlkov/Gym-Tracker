package hihihiha.semchik2017.gymtracker.ui.screens.weight

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hihihiha.semchik2017.gymtracker.data.model.BodyWeight
import hihihiha.semchik2017.gymtracker.domain.repository.GymRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeightViewModel @Inject constructor(
    private val repository: GymRepository
) : ViewModel() {

    val weightHistory: StateFlow<List<BodyWeight>> = repository.getAllBodyWeights()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addWeight(weight: Double) {
        viewModelScope.launch {
            repository.insertBodyWeight(BodyWeight(date = System.currentTimeMillis(), weight = weight))
        }
    }

    fun deleteWeight(bodyWeight: BodyWeight) {
        viewModelScope.launch {
            repository.deleteBodyWeight(bodyWeight)
        }
    }
}
