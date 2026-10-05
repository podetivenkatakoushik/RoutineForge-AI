package com.routineforge.app.recorder

import com.routineforge.app.domain.model.ActionType
import com.routineforge.app.domain.model.StepParameter
import com.routineforge.app.recorder.model.RecordedAction

/**
 * Extensible interface for capturing actions from various inputs (e.g. In-App Workspace, Voice, Camera).
 */
interface ActionCaptureSource {
    val sourceName: String

    fun captureAction(
        actionType: ActionType,
        title: String,
        description: String,
        parameters: List<StepParameter> = emptyList(),
        sequenceNumber: Int = 1
    ): RecordedAction
}

/**
 * In-App Controlled Workspace Capture Source implementation.
 */
class InAppWorkspaceCaptureSource : ActionCaptureSource {
    override val sourceName: String = "IN_APP_WORKSPACE"

    override fun captureAction(
        actionType: ActionType,
        title: String,
        description: String,
        parameters: List<StepParameter>,
        sequenceNumber: Int
    ): RecordedAction {
        return RecordedAction(
            id = "act_${System.currentTimeMillis()}_${(100..999).random()}",
            sequenceNumber = sequenceNumber,
            timestamp = System.currentTimeMillis(),
            actionType = actionType,
            title = title,
            description = description,
            parameters = parameters,
            source = sourceName
        )
    }
}
