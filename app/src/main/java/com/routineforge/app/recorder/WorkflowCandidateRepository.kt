package com.routineforge.app.recorder

import com.routineforge.app.recorder.model.WorkflowCandidate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

interface WorkflowCandidateRepository {
    fun getCandidates(): Flow<List<WorkflowCandidate>>
    fun getCandidateById(id: String): Flow<WorkflowCandidate?>
    suspend fun saveCandidate(candidate: WorkflowCandidate)
    suspend fun clear()
}

class InMemoryWorkflowCandidateRepository : WorkflowCandidateRepository {
    private val candidatesState = MutableStateFlow<List<WorkflowCandidate>>(emptyList())

    override fun getCandidates(): Flow<List<WorkflowCandidate>> = candidatesState.asStateFlow()

    override fun getCandidateById(id: String): Flow<WorkflowCandidate?> {
        return MutableStateFlow(candidatesState.value.find { it.candidateId == id })
    }

    override suspend fun saveCandidate(candidate: WorkflowCandidate) {
        val current = candidatesState.value.toMutableList()
        val index = current.indexOfFirst { it.candidateId == candidate.candidateId }
        if (index >= 0) {
            current[index] = candidate
        } else {
            current.add(candidate)
        }
        candidatesState.value = current
    }

    override suspend fun clear() {
        candidatesState.value = emptyList()
    }

    companion object {
        val instance: WorkflowCandidateRepository by lazy { InMemoryWorkflowCandidateRepository() }
    }
}
