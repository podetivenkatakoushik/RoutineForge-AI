package com.routineforge.app.domain.model

/**
 * Represents the lifecycle state of a Routine in RoutineForge AI.
 */
enum class RoutineStatus {
    /**
     * Newly synthesized or edited routine waiting for user review and approval.
     */
    DRAFT,

    /**
     * Approved and validated routine ready for manual or automated execution.
     */
    READY,

    /**
     * Currently active and enabled routine that can respond to triggers.
     */
    ACTIVE,

    /**
     * Temporarily paused routine; triggers will be ignored until resumed.
     */
    PAUSED,

    /**
     * Inactive or soft-deleted routine retained for historical analytics.
     */
    ARCHIVED
}
