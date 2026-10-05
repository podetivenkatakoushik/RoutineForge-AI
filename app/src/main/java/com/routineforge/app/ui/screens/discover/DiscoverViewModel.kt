package com.routineforge.app.ui.screens.discover

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class DiscoverUiState(
    val title: String = "Discover a Routine",
    val subtitle: String = "Show RoutineForge how you normally work.",
    val isSimulatingAILoading: Boolean = false
)

class DiscoverViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DiscoverUiState())
    val uiState: StateFlow<DiscoverUiState> = _uiState.asStateFlow()

    fun toggleAILoadingSimulation() {
        _uiState.value = _uiState.value.copy(
            isSimulatingAILoading = !_uiState.value.isSimulatingAILoading
        )
    }
}
