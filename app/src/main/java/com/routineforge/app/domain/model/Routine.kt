package com.routineforge.app.domain.model

/**
 * Core Domain Model representing a reusable, automated workflow in RoutineForge AI.
 */
data class Routine(
    val id: String,
    val name: String,
    val description: String,
    val category: String = "General",
    val status: RoutineStatus = RoutineStatus.ACTIVE,
    val trigger: Trigger = Trigger.Manual(),
    val steps: List<RoutineStep> = emptyList(),
    val timeSavedMetrics: TimeSavedMetrics = TimeSavedMetrics(
        manualEstimatedDurationMs = 15 * 60 * 1000L,
        automatedDurationMs = 3 * 60 * 1000L
    ),
    val executionCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastExecutedAt: Long? = null
) {
    val riskLevel: RiskLevel
        get() = steps.maxOfOrNull { it.riskLevel } ?: RiskLevel.LOW
}
