package com.routineforge.app.ui.screens.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.routineforge.app.ai.InMemoryRoutineProposalStore
import com.routineforge.app.ai.RoutineProposalStore
import com.routineforge.app.domain.model.ActionType
import com.routineforge.app.domain.model.RiskLevel
import com.routineforge.app.domain.model.RoutineProposal
import com.routineforge.app.domain.model.RoutineStep
import com.routineforge.app.domain.model.StepParameter
import com.routineforge.app.domain.model.Trigger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

data class RoutineEditorUiState(
    val proposal: RoutineProposal? = null,
    val nameInput: String = "",
    val descriptionInput: String = "",
    val intentInput: String = "",
    val triggerNameInput: String = "",
    val steps: List<RoutineStep> = emptyList(),
    val editingStep: RoutineStep? = null,
    val showAddStepDialog: Boolean = false,
    val showLeaveConfirmDialog: Boolean = false,
    val validationError: String? = null,
    val hasUnsavedChanges: Boolean = false
)

class RoutineEditorViewModel(
    private val proposalStore: RoutineProposalStore = InMemoryRoutineProposalStore.instance
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoutineEditorUiState())
    val uiState: StateFlow<RoutineEditorUiState> = _uiState.asStateFlow()

    init {
        val initialProposal = proposalStore.currentProposal.value
        if (initialProposal != null) {
            _uiState.value = RoutineEditorUiState(
                proposal = initialProposal,
                nameInput = initialProposal.name,
                descriptionInput = initialProposal.description,
                intentInput = initialProposal.intent,
                triggerNameInput = initialProposal.trigger.displayTitle,
                steps = initialProposal.steps
            )
        }

        try {
            proposalStore.currentProposal
                .onEach { proposal ->
                    if (proposal != null) {
                        _uiState.value = _uiState.value.copy(
                            proposal = proposal,
                            nameInput = proposal.name,
                            descriptionInput = proposal.description,
                            intentInput = proposal.intent,
                            triggerNameInput = proposal.trigger.displayTitle,
                            steps = proposal.steps
                        )
                    }
                }
                .launchIn(viewModelScope)
        } catch (e: Exception) {
            // Safe fallback for plain JVM unit tests without Main dispatcher
        }
    }

    fun updateName(text: String) {
        _uiState.value = _uiState.value.copy(nameInput = text, hasUnsavedChanges = true)
    }

    fun updateDescription(text: String) {
        _uiState.value = _uiState.value.copy(descriptionInput = text, hasUnsavedChanges = true)
    }

    fun updateIntent(text: String) {
        _uiState.value = _uiState.value.copy(intentInput = text, hasUnsavedChanges = true)
    }

    fun updateTriggerName(text: String) {
        _uiState.value = _uiState.value.copy(triggerNameInput = text, hasUnsavedChanges = true)
    }

    fun deleteStep(stepId: String) {
        val current = _uiState.value.steps.toMutableList()
        val removed = current.removeIf { it.id == stepId }
        if (removed) {
            val renumbered = current.mapIndexed { idx, st -> st.copy(order = idx + 1) }
            _uiState.value = _uiState.value.copy(steps = renumbered, hasUnsavedChanges = true)
        }
    }

    fun moveStepUp(index: Int) {
        val current = _uiState.value.steps.toMutableList()
        if (index in 1 until current.size) {
            val item = current.removeAt(index)
            current.add(index - 1, item)
            val renumbered = current.mapIndexed { idx, st -> st.copy(order = idx + 1) }
            _uiState.value = _uiState.value.copy(steps = renumbered, hasUnsavedChanges = true)
        }
    }

    fun moveStepDown(index: Int) {
        val current = _uiState.value.steps.toMutableList()
        if (index in 0 until current.size - 1) {
            val item = current.removeAt(index)
            current.add(index + 1, item)
            val renumbered = current.mapIndexed { idx, st -> st.copy(order = idx + 1) }
            _uiState.value = _uiState.value.copy(steps = renumbered, hasUnsavedChanges = true)
        }
    }

    fun startEditingStep(step: RoutineStep) {
        _uiState.value = _uiState.value.copy(editingStep = step)
    }

    fun cancelEditingStep() {
        _uiState.value = _uiState.value.copy(editingStep = null)
    }

    fun saveStepEdit(
        updatedTitle: String,
        updatedDescription: String,
        updatedRiskLevel: RiskLevel,
        updatedRequiresConfirmation: Boolean
    ) {
        val editing = _uiState.value.editingStep ?: return
        val current = _uiState.value.steps.toMutableList()
        val index = current.indexOfFirst { it.id == editing.id }
        if (index >= 0) {
            current[index] = editing.copy(
                title = updatedTitle,
                description = updatedDescription,
                riskLevel = updatedRiskLevel,
                requiresConfirmation = updatedRequiresConfirmation
            )
            _uiState.value = _uiState.value.copy(
                steps = current,
                editingStep = null,
                hasUnsavedChanges = true
            )
        }
    }

    fun startAddingStep() {
        _uiState.value = _uiState.value.copy(showAddStepDialog = true)
    }

    fun cancelAddingStep() {
        _uiState.value = _uiState.value.copy(showAddStepDialog = false)
    }

    fun addStep(
        actionType: ActionType,
        title: String,
        description: String,
        riskLevel: RiskLevel,
        parameters: List<StepParameter> = emptyList()
    ) {
        val current = _uiState.value.steps.toMutableList()
        val newStep = RoutineStep(
            id = "step_${current.size + 1}_${System.currentTimeMillis()}",
            order = current.size + 1,
            actionType = actionType,
            title = title.ifBlank { actionType.displayTitle },
            description = description.ifBlank { "Perform ${actionType.displayTitle}" },
            parameters = parameters,
            riskLevel = riskLevel,
            requiresConfirmation = (riskLevel != RiskLevel.LOW)
        )
        current.add(newStep)
        _uiState.value = _uiState.value.copy(
            steps = current,
            showAddStepDialog = false,
            hasUnsavedChanges = true
        )
    }

    fun saveChanges(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val currentProposal = _uiState.value.proposal ?: return

        val name = _uiState.value.nameInput.trim()
        if (name.isBlank()) {
            val error = "Routine name cannot be empty."
            _uiState.value = _uiState.value.copy(validationError = error)
            onError(error)
            return
        }

        val description = _uiState.value.descriptionInput.trim()
        if (description.isBlank()) {
            val error = "Routine description cannot be empty."
            _uiState.value = _uiState.value.copy(validationError = error)
            onError(error)
            return
        }

        if (_uiState.value.steps.isEmpty()) {
            val error = "Routine must contain at least one step."
            _uiState.value = _uiState.value.copy(validationError = error)
            onError(error)
            return
        }

        val updatedProposal = currentProposal.copy(
            name = name,
            description = description,
            intent = _uiState.value.intentInput.trim().ifBlank { currentProposal.intent },
            trigger = Trigger.Manual(name = _uiState.value.triggerNameInput.trim().ifBlank { "When document selected" }),
            steps = _uiState.value.steps
        )

        proposalStore.updateProposal(updatedProposal)
        _uiState.value = _uiState.value.copy(
            proposal = updatedProposal,
            hasUnsavedChanges = false,
            validationError = null
        )
        onSuccess()
    }
}
