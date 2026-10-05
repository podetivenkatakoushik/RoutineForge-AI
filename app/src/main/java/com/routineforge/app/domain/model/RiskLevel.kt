package com.routineforge.app.domain.model

/**
 * Risk classification for actions and routines to enforce human-in-the-loop confirmation.
 */
enum class RiskLevel {
    /**
     * Safe, non-destructive, read-only operations (e.g. reading files, displaying results).
     */
    LOW,

    /**
     * Local modifications such as moving, renaming, or creating files.
     */
    MEDIUM,

    /**
     * External communications, system notifications, or data exports.
     */
    HIGH,

    /**
     * Financial transactions or irreversible destructive operations (requires multi-step user approval).
     */
    CRITICAL
}
