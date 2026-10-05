package com.routineforge.app.ai

import com.routineforge.app.domain.model.RoutineProposal
import com.routineforge.app.recorder.model.WorkflowCandidate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface RoutineProposalStore {
    val currentProposal: StateFlow<RoutineProposal?>
    val activeCandidate: StateFlow<WorkflowCandidate?>

    fun saveProposal(proposal: RoutineProposal, candidate: WorkflowCandidate? = null)
    fun updateProposal(proposal: RoutineProposal)
    fun clear()
}

class InMemoryRoutineProposalStore : RoutineProposalStore {
    private val _currentProposal = MutableStateFlow<RoutineProposal?>(null)
    override val currentProposal: StateFlow<RoutineProposal?> = _currentProposal.asStateFlow()

    private val _activeCandidate = MutableStateFlow<WorkflowCandidate?>(null)
    override val activeCandidate: StateFlow<WorkflowCandidate?> = _activeCandidate.asStateFlow()

    override fun saveProposal(proposal: RoutineProposal, candidate: WorkflowCandidate?) {
        _currentProposal.value = proposal
        if (candidate != null) {
            _activeCandidate.value = candidate
        }
    }

    override fun updateProposal(proposal: RoutineProposal) {
        _currentProposal.value = proposal
    }

    override fun clear() {
        _currentProposal.value = null
        _activeCandidate.value = null
    }

    companion object {
        val instance: RoutineProposalStore by lazy { InMemoryRoutineProposalStore() }
    }
}
