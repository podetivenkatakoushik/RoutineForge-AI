package com.routineforge.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.routineforge.app.domain.execution.InMemoryExecutionRepository
import com.routineforge.app.domain.workspace.InMemorySimulationWorkspace
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val selectedAiProvider: String = "Local AI / Gemini Nano",
    val isLocalProcessingOnly: Boolean = true,
    val isConfirmationRequired: Boolean = true,
    val isHighRiskSafetyGateEnabled: Boolean = true,
    val isExecutionNotificationsEnabled: Boolean = true,
    val appVersion: String = "v1.0.0 (Hackathon Build)"
)

class SettingsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun toggleLocalProcessing(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isLocalProcessingOnly = enabled)
    }

    fun toggleConfirmationRequired(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isConfirmationRequired = enabled)
    }

    fun toggleHighRiskGate(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isHighRiskSafetyGateEnabled = enabled)
    }

    fun toggleNotifications(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isExecutionNotificationsEnabled = enabled)
    }

    fun resetDemoData() {
        InMemorySimulationWorkspace.instance.reset()
        viewModelScope.launch {
            InMemoryExecutionRepository.instance.clear()
        }
    }
}
