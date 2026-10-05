package com.routineforge.app.recorder.model

/**
 * Domain model representing a complete workflow recording session.
 */
data class RecordingSession(
    val sessionId: String,
    val startedAt: Long,
    val endedAt: Long? = null,
    val status: RecordingState = RecordingState.COMPLETED,
    val actions: List<RecordedAction> = emptyList(),
    val durationMs: Long = 0L
) {
    val actionCount: Int
        get() = actions.size

    val formattedDuration: String
        get() {
            val seconds = durationMs / 1000
            val minutes = seconds / 60
            val remSecs = seconds % 60
            return if (minutes > 0) "${minutes}m ${remSecs}s" else "${remSecs}s"
        }
}
