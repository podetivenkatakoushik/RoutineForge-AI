package com.routineforge.app.recorder.model

/**
 * Clean handoff model representing a reviewed workflow recording ready for AI routine synthesis.
 */
data class WorkflowCandidate(
    val candidateId: String,
    val name: String,
    val description: String,
    val session: RecordingSession,
    val reviewedAt: Long = System.currentTimeMillis()
)
