package com.routineforge.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.routineforge.app.data.model.ProductivityStats
import com.routineforge.app.data.model.RoutineItem
import com.routineforge.app.data.model.toRoutineItem
import com.routineforge.app.data.repository.InMemoryRoutineRepository
import com.routineforge.app.domain.repository.RoutineRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

data class HomeUiState(
    val userName: String = "Koushik",
    val greetingMessage: String = "Good morning, Koushik",
    val subtitleMessage: String = "Turn repetitive work into intelligent routines.",
    val appName: String = "RoutineForge AI",
    val tagline: String = "Teach it once. Automate it forever.",
    val stats: ProductivityStats = ProductivityStats(),
    val recentRoutines: List<RoutineItem> = emptyList(),
    val isEngineReady: Boolean = true
)

class HomeViewModel(
    private val repository: RoutineRepository = InMemoryRoutineRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        repository.getAllRoutines()
            .onEach { routines ->
                _uiState.value = _uiState.value.copy(
                    recentRoutines = routines.take(3).map { it.toRoutineItem() }
                )
            }
            .launchIn(viewModelScope)

        repository.getProductivityStats()
            .onEach { stats ->
                _uiState.value = _uiState.value.copy(stats = stats)
            }
            .launchIn(viewModelScope)
    }
}
