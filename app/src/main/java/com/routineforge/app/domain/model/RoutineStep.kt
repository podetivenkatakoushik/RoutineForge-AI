package com.routineforge.app.domain.model

/**
 * Domain model representing a single step within a Routine.
 */
data class RoutineStep(
    val id: String,
    val order: Int,
    val actionType: ActionType,
    val title: String,
    val description: String,
    val parameters: List<StepParameter> = emptyList(),
    val condition: Condition? = null,
    val riskLevel: RiskLevel = actionType.defaultRiskLevel,
    val requiresConfirmation: Boolean = (riskLevel != RiskLevel.LOW),
    val enabled: Boolean = true
)
