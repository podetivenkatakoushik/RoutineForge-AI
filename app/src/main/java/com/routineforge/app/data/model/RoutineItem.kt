package com.routineforge.app.data.model

import com.routineforge.app.domain.model.RiskLevel
import com.routineforge.app.domain.model.Routine

data class RoutineItem(
    val id: String,
    val name: String,
    val description: String,
    val lastRun: String,
    val executionCount: Int,
    val estimatedTimeSaved: String,
    val status: String = "Active",
    val riskLevel: RiskLevel = RiskLevel.LOW,
    val category: String = "General"
)

fun Routine.toRoutineItem(): RoutineItem {
    val lastRunText = lastExecutedAt?.let {
        val diffHours = (System.currentTimeMillis() - it) / (1000 * 3600)
        when {
            diffHours < 1 -> "Just now"
            diffHours < 24 -> "Today"
            diffHours < 48 -> "Yesterday"
            else -> "${diffHours / 24} days ago"
        }
    } ?: "Not run recently"

    return RoutineItem(
        id = id,
        name = name,
        description = description,
        lastRun = lastRunText,
        executionCount = executionCount,
        estimatedTimeSaved = timeSavedMetrics.timeSavedFormatted,
        status = status.name.lowercase().replaceFirstChar { it.uppercase() },
        riskLevel = riskLevel,
        category = category
    )
}
