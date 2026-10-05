package com.routineforge.app.workflow.model

enum class RiskLevel {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

data class Routine(
    val id: String,
    val title: String,
    val description: String,
    val steps: List<RoutineStep> = emptyList(),
    val trigger: Trigger? = null,
    val createdAt: Long = System.currentTimeMillis()
)

data class RoutineStep(
    val id: String,
    val order: Int,
    val actionName: String,
    val targetApp: String? = null,
    val parameters: Map<String, String> = emptyMap(),
    val riskLevel: RiskLevel = RiskLevel.LOW
)

sealed interface Trigger {
    data class Manual(val name: String = "Manual Launch") : Trigger
    data class Schedule(val cronOrTime: String) : Trigger
    data class AppEvent(val packageName: String, val eventType: String) : Trigger
}

data class Condition(
    val expression: String,
    val requirementDescription: String
)

data class WorkflowVariable(
    val key: String,
    val value: String,
    val isSensitive: Boolean = false
)

data class ExecutionResult(
    val executionId: String,
    val routineId: String,
    val success: Boolean,
    val message: String,
    val executionTimeMs: Long,
    val timestamp: Long = System.currentTimeMillis()
)
