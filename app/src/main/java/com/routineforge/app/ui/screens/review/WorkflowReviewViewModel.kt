package com.routineforge.app.ui.screens.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.routineforge.app.ai.GenerateRoutineUseCase
import com.routineforge.app.ai.InMemoryRoutineProposalStore
import com.routineforge.app.ai.RoutineProposalStore
import com.routineforge.app.recorder.ActionRecorder
import com.routineforge.app.recorder.DefaultActionRecorder
import com.routineforge.app.recorder.InMemoryRecordingSessionStore
import com.routineforge.app.recorder.InMemoryWorkflowCandidateRepository
import com.routineforge.app.recorder.RecordingSessionStore
import com.routineforge.app.recorder.WorkflowCandidateRepository
import com.routineforge.app.recorder.model.RecordedAction
import com.routineforge.app.recorder.model.RecordingSession
import com.routineforge.app.recorder.model.RecordingState
import com.routineforge.app.recorder.model.WorkflowCandidate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class WorkflowReviewUiState(
    val session: RecordingSession = RecordingSession("session_demo", System.currentTimeMillis(), System.currentTimeMillis()),
    val actions: List<RecordedAction> = emptyList(),
    val candidateName: String = "Recorded Workflow",
    val candidateDescription: String = "Workflow captured via in-app workspace demonstration",
    val editingAction: RecordedAction? = null,
    val isSaved: Boolean = false,
    val isGenerating: Boolean = false,
    val generationError: String? = null
)

class WorkflowReviewViewModel(
    val actionRecorder: ActionRecorder = DefaultActionRecorder(),
    private val sessionStore: RecordingSessionStore = InMemoryRecordingSessionStore.instance,
    private val candidateRepository: WorkflowCandidateRepository = InMemoryWorkflowCandidateRepository.instance,
    private val proposalStore: RoutineProposalStore = InMemoryRoutineProposalStore.instance,
    private val generateRoutineUseCase: GenerateRoutineUseCase = GenerateRoutineUseCase()
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkflowReviewUiState())
    val uiState: StateFlow<WorkflowReviewUiState> = _uiState.asStateFlow()

    init {
        val storedSession = sessionStore.activeSession.value
        if (storedSession != null) {
            _uiState.value = WorkflowReviewUiState(
                session = storedSession,
                actions = storedSession.actions,
                candidateName = "Recorded Workflow"
            )
        } else {
            val currentActionsList = actionRecorder.currentActions.value
            val session = RecordingSession(
                sessionId = "rec_${System.currentTimeMillis()}",
                startedAt = System.currentTimeMillis() - 60000,
                endedAt = System.currentTimeMillis(),
                status = RecordingState.COMPLETED,
                actions = currentActionsList,
                durationMs = actionRecorder.elapsedTimeMs.value
            )
            _uiState.value = WorkflowReviewUiState(
                session = session,
                actions = currentActionsList,
                candidateName = "Recorded Workflow"
            )
        }
    }

    fun updateCandidateName(name: String) {
        _uiState.value = _uiState.value.copy(candidateName = name)
    }

    fun updateCandidateDescription(desc: String) {
        _uiState.value = _uiState.value.copy(candidateDescription = desc)
    }

    fun deleteAction(actionId: String) {
        val currentList = _uiState.value.actions.toMutableList()
        val removed = currentList.removeIf { it.id == actionId }
        if (removed) {
            val renumbered = currentList.mapIndexed { idx, act ->
                act.copy(sequenceNumber = idx + 1)
            }
            val updatedSession = _uiState.value.session.copy(actions = renumbered)
            _uiState.value = _uiState.value.copy(
                actions = renumbered,
                session = updatedSession
            )
            sessionStore.updateSession(updatedSession)
        }
    }

    fun moveActionUp(index: Int) {
        val currentList = _uiState.value.actions.toMutableList()
        if (index in 1 until currentList.size) {
            val item = currentList.removeAt(index)
            currentList.add(index - 1, item)
            val renumbered = currentList.mapIndexed { idx, act ->
                act.copy(sequenceNumber = idx + 1)
            }
            val updatedSession = _uiState.value.session.copy(actions = renumbered)
            _uiState.value = _uiState.value.copy(
                actions = renumbered,
                session = updatedSession
            )
            sessionStore.updateSession(updatedSession)
        }
    }

    fun moveActionDown(index: Int) {
        val currentList = _uiState.value.actions.toMutableList()
        if (index in 0 until currentList.size - 1) {
            val item = currentList.removeAt(index)
            currentList.add(index + 1, item)
            val renumbered = currentList.mapIndexed { idx, act ->
                act.copy(sequenceNumber = idx + 1)
            }
            val updatedSession = _uiState.value.session.copy(actions = renumbered)
            _uiState.value = _uiState.value.copy(
                actions = renumbered,
                session = updatedSession
            )
            sessionStore.updateSession(updatedSession)
        }
    }

    fun startEditingAction(action: RecordedAction) {
        _uiState.value = _uiState.value.copy(editingAction = action)
    }

    fun cancelEditingAction() {
        _uiState.value = _uiState.value.copy(editingAction = null)
    }

    fun saveActionEdit(updatedTitle: String, updatedDescription: String) {
        val editing = _uiState.value.editingAction ?: return
        val currentList = _uiState.value.actions.toMutableList()
        val index = currentList.indexOfFirst { it.id == editing.id }
        if (index >= 0) {
            currentList[index] = editing.copy(
                title = updatedTitle,
                description = updatedDescription
            )
            val updatedSession = _uiState.value.session.copy(actions = currentList)
            _uiState.value = _uiState.value.copy(
                actions = currentList,
                session = updatedSession,
                editingAction = null
            )
            sessionStore.updateSession(updatedSession)
        }
    }

    fun generateRoutineWithAI(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isGenerating = true, generationError = null)

            val candidate = WorkflowCandidate(
                candidateId = "cand_${System.currentTimeMillis()}",
                name = _uiState.value.candidateName,
                description = _uiState.value.candidateDescription,
                session = _uiState.value.session.copy(actions = _uiState.value.actions)
            )

            val result = generateRoutineUseCase(candidate)
            _uiState.value = _uiState.value.copy(isGenerating = false)

            result.fold(
                onSuccess = { proposal ->
                    proposalStore.saveProposal(proposal, candidate)
                    onSuccess()
                },
                onFailure = { error ->
                    val msg = error.localizedMessage ?: "Failed to generate routine proposal."
                    _uiState.value = _uiState.value.copy(generationError = msg)
                    onError(msg)
                }
            )
        }
    }

    fun saveWorkflowCandidate(onComplete: () -> Unit) {
        viewModelScope.launch {
            val candidate = WorkflowCandidate(
                candidateId = "cand_${System.currentTimeMillis()}",
                name = _uiState.value.candidateName,
                description = _uiState.value.candidateDescription,
                session = _uiState.value.session.copy(actions = _uiState.value.actions)
            )
            candidateRepository.saveCandidate(candidate)
            _uiState.value = _uiState.value.copy(isSaved = true)
            actionRecorder.clear()
            sessionStore.clear()
            onComplete()
        }
    }
}
