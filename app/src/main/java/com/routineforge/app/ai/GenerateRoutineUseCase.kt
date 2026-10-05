package com.routineforge.app.ai

import com.routineforge.app.domain.model.RoutineProposal
import com.routineforge.app.recorder.model.WorkflowCandidate

class GenerateRoutineUseCase(
    private val aiProvider: AIProvider = GeminiAIProvider()
) {
    suspend operator fun invoke(candidate: WorkflowCandidate): Result<RoutineProposal> {
        if (candidate.session.actions.isEmpty()) {
            return Result.failure(IllegalArgumentException("Cannot generate routine from empty workflow candidate."))
        }
        return aiProvider.generateRoutine(candidate)
    }
}
