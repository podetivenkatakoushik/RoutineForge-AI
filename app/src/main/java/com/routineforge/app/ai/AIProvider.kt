package com.routineforge.app.ai

import com.routineforge.app.domain.model.RoutineProposal
import com.routineforge.app.recorder.model.WorkflowCandidate

interface AIProvider {
    val name: String
    val isAvailable: Boolean

    suspend fun generateRoutine(candidate: WorkflowCandidate): Result<RoutineProposal>
}
