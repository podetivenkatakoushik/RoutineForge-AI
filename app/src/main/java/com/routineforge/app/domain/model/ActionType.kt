package com.routineforge.app.domain.model

/**
 * Conceptual domain action types supported by RoutineForge AI.
 * These represent abstract workflow capabilities without direct binding to Android execution APIs.
 */
enum class ActionType(val defaultRiskLevel: RiskLevel, val displayTitle: String) {
    READ_FILE(RiskLevel.LOW, "Read File"),
    SELECT_FILE(RiskLevel.LOW, "Select File"),
    RENAME_FILE(RiskLevel.MEDIUM, "Rename File"),
    MOVE_FILE(RiskLevel.MEDIUM, "Move File"),
    COPY_FILE(RiskLevel.MEDIUM, "Copy File"),
    CREATE_FILE(RiskLevel.MEDIUM, "Create File"),
    EXTRACT_TEXT(RiskLevel.LOW, "Extract Text"),
    EXTRACT_DATA(RiskLevel.LOW, "Extract Data"),
    TRANSFORM_DATA(RiskLevel.LOW, "Transform Data"),
    OPEN_APPLICATION(RiskLevel.LOW, "Open Application"),
    USER_INPUT(RiskLevel.LOW, "User Input"),
    SHOW_RESULT(RiskLevel.LOW, "Show Result"),
    NOTIFICATION(RiskLevel.HIGH, "Send Notification"),
    WAIT(RiskLevel.LOW, "Wait Delay"),
    CONDITIONAL(RiskLevel.LOW, "Conditional Branch")
}
