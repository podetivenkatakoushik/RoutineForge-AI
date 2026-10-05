package com.routineforge.app.ui.screens.generated

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.routineforge.app.ai.GenerateRoutineUseCase
import com.routineforge.app.ai.InMemoryRoutineProposalStore
import com.routineforge.app.ai.RoutineProposalStore
import com.routineforge.app.data.repository.InMemoryRoutineRepository
import com.routineforge.app.domain.model.RoutineProposal
import com.routineforge.app.domain.repository.RoutineRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class GeneratedRoutineUiState(
    val proposal: RoutineProposal? = null,
    val isRegenerating: Boolean = false,
    val isEditing: Boolean = false,
    val editName: String = "",
    val editDescription: String = "",
    val isSaved: Boolean = false
)

class GeneratedRoutineViewModel(
    private val proposalStore: RoutineProposalStore = InMemoryRoutineProposalStore.instance,
    private val routineRepository: RoutineRepository = InMemoryRoutineRepository(),
    private val generateRoutineUseCase: GenerateRoutineUseCase = GenerateRoutineUseCase()
) : ViewModel() {

    private val _uiState = MutableStateFlow(GeneratedRoutineUiState())
    val uiState: StateFlow<GeneratedRoutineUiState> = _uiState.asStateFlow()

    init {
        proposalStore.currentProposal
            .onEach { proposal ->
                if (proposal != null) {
                    _uiState.value = _uiState.value.copy(
                        proposal = proposal,
                        editName = proposal.name,
                        editDescription = proposal.description
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun startEditing() {
        val proposal = _uiState.value.proposal ?: return
        _uiState.value = _uiState.value.copy(
            isEditing = true,
            editName = proposal.name,
            editDescription = proposal.description
        )
    }

    fun cancelEditing() {
        _uiState.value = _uiState.value.copy(isEditing = false)
    }

    fun updateEditFields(name: String, description: String) {
        _uiState.value = _uiState.value.copy(
            editName = name,
            editDescription = description
        )
    }

    fun saveEdits() {
        val current = _uiState.value.proposal ?: return
        val updated = current.copy(
            name = _uiState.value.editName.trim().ifBlank { current.name },
            description = _uiState.value.editDescription.trim().ifBlank { current.description }
        )
        proposalStore.updateProposal(updated)
        _uiState.value = _uiState.value.copy(
            proposal = updated,
            isEditing = false
        )
    }

    fun regenerateRoutine(onShowSnackbar: (String) -> Unit) {
        val candidate = proposalStore.activeCandidate.value
        if (candidate == null) {
            onShowSnackbar("No active workflow candidate available for regeneration.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRegenerating = true)
            val result = generateRoutineUseCase(candidate)
            _uiState.value = _uiState.value.copy(isRegenerating = false)

            result.fold(
                onSuccess = { newProposal ->
                    proposalStore.updateProposal(newProposal)
                    onShowSnackbar("Routine regenerated successfully!")
                },
                onFailure = { error ->
                    onShowSnackbar("Regeneration failed: ${error.localizedMessage ?: "Unknown error"}")
                }
            )
        }
    }

    fun saveRoutineToLibrary(onSuccess: () -> Unit) {
        val proposal = _uiState.value.proposal ?: return
        viewModelScope.launch {
            val routine = proposal.toRoutine()
            routineRepository.saveRoutine(routine)
            _uiState.value = _uiState.value.copy(isSaved = true)
            proposalStore.clear()
            onSuccess()
        }
    }
}
