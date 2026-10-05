package com.routineforge.app.domain.analytics

import com.routineforge.app.data.model.WeeklyTimeSavedPoint

data class AnalyticsSummary(
    val totalRoutines: Int = 5,
    val totalExecutions: Int = 87,
    val completedExecutions: Int = 85,
    val failedExecutions: Int = 2,
    val totalTimeSavedMs: Long = 92400000L, // ~25h 40m baseline with dynamic increments
    val avgExecutionDurationMs: Long = 135000L, // ~2m 15s
    val successRatePercentage: Int = 98,
    val weeklyChartPoints: List<WeeklyTimeSavedPoint> = listOf(
        WeeklyTimeSavedPoint("Mon", 18),
        WeeklyTimeSavedPoint("Tue", 25),
        WeeklyTimeSavedPoint("Wed", 45, isPeakDay = true),
        WeeklyTimeSavedPoint("Thu", 30),
        WeeklyTimeSavedPoint("Fri", 22),
        WeeklyTimeSavedPoint("Sat", 10),
        WeeklyTimeSavedPoint("Sun", 5)
    )
) {
    val totalTimeSavedFormatted: String
        get() = TimeFormatter.formatMs(totalTimeSavedMs)

    val avgExecutionDurationFormatted: String
        get() = TimeFormatter.formatMs(avgExecutionDurationMs)
}

data class RoutineImpactItem(
    val routineId: String,
    val routineName: String,
    val totalTimeSavedFormatted: String,
    val executionCount: Int,
    val successRatePercentage: Int = 100
)
