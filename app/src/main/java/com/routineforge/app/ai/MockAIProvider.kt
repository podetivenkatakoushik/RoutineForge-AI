package com.routineforge.app.ai

import com.routineforge.app.domain.model.ActionType
import com.routineforge.app.domain.model.ParameterType
import com.routineforge.app.domain.model.RiskLevel
import com.routineforge.app.domain.model.RoutineProposal
import com.routineforge.app.domain.model.RoutineStep
import com.routineforge.app.domain.model.StepParameter
import com.routineforge.app.domain.model.Trigger
import com.routineforge.app.recorder.model.WorkflowCandidate
import kotlinx.coroutines.delay

class MockAIProvider : AIProvider {
    override val name: String = "Mock AI Engine (Offline Test Mode)"
    override val isAvailable: Boolean = true

    override suspend fun generateRoutine(candidate: WorkflowCandidate): Result<RoutineProposal> {
        delay(1200) // Simulate fast deterministic AI synthesis delay

        val actions = candidate.session.actions
        if (actions.isEmpty()) {
            return Result.failure(IllegalArgumentException("Cannot generate routine from an empty workflow candidate."))
        }

        val hasRename = actions.any { it.actionType == ActionType.RENAME_FILE }
        val hasMove = actions.any { it.actionType == ActionType.MOVE_FILE }

        val routineName = if (hasRename && hasMove) {
            "Organize Assignment Documents"
        } else {
            "Automated Workflow: ${actions.firstOrNull()?.title ?: "Document Action"}"
        }

        val intent = "Organize an assignment document by renaming and moving it into the appropriate folder."
        val description = "Rename selected assignment documents and organize them automatically into target directories."

        val steps = actions.mapIndexed { index, act ->
            val mappedRisk = when (act.actionType) {
                ActionType.RENAME_FILE, ActionType.MOVE_FILE -> RiskLevel.MEDIUM
                else -> RiskLevel.LOW
            }

            RoutineStep(
                id = "step_${index + 1}",
                order = index + 1,
                actionType = act.actionType,
                title = act.title,
                description = act.description,
                parameters = act.parameters,
                riskLevel = mappedRisk,
                requiresConfirmation = (mappedRisk != RiskLevel.LOW)
            )
        }

        val manualDurationMs = candidate.session.durationMs.coerceAtLeast(240000L)
        val routineDurationMs = (manualDurationMs / 4).coerceAtLeast(60000L)

        val proposal = RoutineProposal(
            name = routineName,
            description = description,
            intent = intent,
            category = "Documents & Files",
            trigger = Trigger.Manual(name = "When document selected"),
            steps = steps,
            estimatedManualDurationMs = manualDurationMs,
            estimatedRoutineDurationMs = routineDurationMs,
            reasoningSummary = "RoutineForge detected a document-organization workflow from the ${actions.size} demonstrated actions.",
            confidenceScore = 0.92f
        )

        return Result.success(proposal)
    }
}
