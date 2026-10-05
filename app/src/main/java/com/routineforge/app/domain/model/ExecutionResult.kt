package com.routineforge.app.domain.model

import com.routineforge.app.domain.analytics.TimeFormatter

enum class ExecutionStatus {
    QUEUED,
    RUNNING,
    PAUSED,
    COMPLETED,
    FAILED,
    CANCELLED
}

data class ExecutionStepResult(
    val stepId: String,
    val stepTitle: String,
    val success: Boolean,
    val durationMs: Long,
    val outputMessage: String,
    val errorMessage: String? = null
)

/**
 * Domain model representing execution analytics and step outcomes for a routine run.
 */
data class ExecutionResult(
    val executionId: String,
    val routineId: String,
    val routineName: String = "Routine Execution",
    val startTimeMs: Long,
    val endTimeMs: Long,
    val status: ExecutionStatus = ExecutionStatus.COMPLETED,
    val completedStepCount: Int = 0,
    val failedStepCount: Int = 0,
    val estimatedManualDurationMs: Long = 260000L,
    val stepResults: List<ExecutionStepResult> = emptyList(),
    val errorMessage: String? = null
) {
    val durationMs: Long
        get() = (endTimeMs - startTimeMs).coerceAtLeast(0)

    val timeSavedMs: Long
        get() = if (status == ExecutionStatus.COMPLETED) {
            (estimatedManualDurationMs - durationMs).coerceAtLeast(0)
        } else 0L

    val timeSavedFormatted: String
        get() = TimeFormatter.formatMs(timeSavedMs)

    val durationFormatted: String
        get() = TimeFormatter.formatMs(durationMs)
}
