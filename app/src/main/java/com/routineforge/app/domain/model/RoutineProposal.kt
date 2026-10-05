package com.routineforge.app.domain.model

/**
 * Domain model representing an AI-generated proposal before final user approval and saving.
 */
data class RoutineProposal(
    val id: String = "prop_${System.currentTimeMillis()}",
    val name: String,
    val description: String,
    val intent: String,
    val category: String = "Documents & Files",
    val trigger: Trigger = Trigger.Manual("When document selected"),
    val steps: List<RoutineStep> = emptyList(),
    val estimatedManualDurationMs: Long = 260000L,
    val estimatedRoutineDurationMs: Long = 65000L,
    val reasoningSummary: String = "RoutineForge synthesized a document-organization workflow from observed actions.",
    val confidenceScore: Float = 0.92f
) {
    val overallRiskLevel: RiskLevel
        get() = steps.maxOfOrNull { it.riskLevel } ?: RiskLevel.LOW

    val timeSavedFormatted: String
        get() {
            val savedMs = (estimatedManualDurationMs - estimatedRoutineDurationMs).coerceAtLeast(0L)
            val sec = savedMs / 1000
            val min = sec / 60
            val remSec = sec % 60
            return if (min > 0) "${min}m ${remSec}s" else "${remSec}s"
        }

    val manualDurationFormatted: String
        get() {
            val sec = estimatedManualDurationMs / 1000
            val min = sec / 60
            val remSec = sec % 60
            return if (min > 0) "${min}m ${remSec}s" else "${remSec}s"
        }

    val routineDurationFormatted: String
        get() {
            val sec = estimatedRoutineDurationMs / 1000
            val min = sec / 60
            val remSec = sec % 60
            return if (min > 0) "${min}m ${remSec}s" else "${remSec}s"
        }

    val confidencePercentage: Int
        get() = (confidenceScore * 100).toInt().coerceIn(0, 100)

    fun toRoutine(): Routine {
        return Routine(
            id = "rt_${System.currentTimeMillis()}",
            name = name,
            description = description,
            category = category,
            trigger = trigger,
            steps = steps,
            timeSavedMetrics = TimeSavedMetrics(
                manualEstimatedDurationMs = estimatedManualDurationMs,
                automatedDurationMs = estimatedRoutineDurationMs
            )
        )
    }
}
