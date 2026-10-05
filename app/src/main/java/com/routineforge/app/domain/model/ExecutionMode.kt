package com.routineforge.app.domain.model

/**
 * Represents the execution safety mode for running routines.
 */
enum class ExecutionMode {
    /**
     * Preview execution steps in dry-run mode without modifying system state.
     */
    SIMULATION,

    /**
     * Require manual tap confirmation before proceeding to each step.
     */
    MANUAL_CONFIRMATION,

    /**
     * Automated execution mode for trusted routines.
     */
    AUTOMATIC
}
