package com.routineforge.app.recorder.model

/**
 * State lifecycle for the Action Recorder engine.
 */
enum class RecordingState {
    /**
     * Recorder is ready and idle.
     */
    IDLE,

    /**
     * Recorder is actively capturing semantic actions.
     */
    RECORDING,

    /**
     * Recording is paused; timer and capture are temporarily suspended.
     */
    PAUSED,

    /**
     * Recording is completed and ready for workflow review.
     */
    COMPLETED
}
