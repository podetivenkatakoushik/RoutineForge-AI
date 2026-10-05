package com.routineforge.app.domain.analytics

object TimeFormatter {

    fun formatMs(durationMs: Long): String {
        val totalSec = (durationMs / 1000).coerceAtLeast(0)
        val hours = totalSec / 3600
        val minutes = (totalSec % 3600) / 60
        val remSec = totalSec % 60

        return when {
            hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
            hours > 0 -> "${hours}h"
            minutes > 0 && remSec > 0 -> "${minutes}m ${remSec}s"
            minutes > 0 -> "${minutes}m"
            else -> "${remSec}s"
        }
    }

    fun formatTimeSaved(savedMs: Long): String {
        if (savedMs <= 0) return "0s"
        return formatMs(savedMs)
    }
}
