package com.routineforge.app.domain.model

/**
 * Value representation for measuring manual vs automated time efficiency.
 */
data class TimeSavedMetrics(
    val manualEstimatedDurationMs: Long,
    val automatedDurationMs: Long
) {
    val timeSavedMs: Long
        get() = (manualEstimatedDurationMs - automatedDurationMs).coerceAtLeast(0)

    val timeSavedFormatted: String
        get() {
            val minutes = timeSavedMs / (1000 * 60)
            val hours = minutes / 60
            val remainingMinutes = minutes % 60
            return when {
                hours > 0 && remainingMinutes > 0 -> "${hours}h ${remainingMinutes}m"
                hours > 0 -> "${hours}h"
                else -> "${minutes} min"
            }
        }
}
