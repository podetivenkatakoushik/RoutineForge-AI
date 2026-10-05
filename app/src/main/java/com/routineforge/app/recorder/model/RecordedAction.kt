package com.routineforge.app.recorder.model

import com.routineforge.app.domain.model.ActionType
import com.routineforge.app.domain.model.StepParameter

/**
 * Domain model representing a single captured semantic action during a recording session.
 */
data class RecordedAction(
    val id: String,
    val sequenceNumber: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val actionType: ActionType,
    val title: String,
    val description: String,
    val parameters: List<StepParameter> = emptyList(),
    val source: String = "IN_APP_WORKSPACE",
    val durationMs: Long = 0L
)
