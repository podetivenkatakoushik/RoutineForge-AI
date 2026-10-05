package com.routineforge.app.data.model

data class ProductivityStats(
    val totalRoutines: Int = 14,
    val totalExecutions: Int = 87,
    val timeSavedFormatted: String = "2h 35m",
    val avgRoutineTimeFormatted: String = "3m 42s"
)

data class WeeklyTimeSavedPoint(
    val dayLabel: String,
    val minutesSaved: Int,
    val isPeakDay: Boolean = false
)
