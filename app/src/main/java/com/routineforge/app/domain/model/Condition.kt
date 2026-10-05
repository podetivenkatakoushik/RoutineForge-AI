package com.routineforge.app.domain.model

enum class ConditionOperator(val symbol: String) {
    EQUALS("=="),
    NOT_EQUALS("!="),
    GREATER_THAN(">"),
    LESS_THAN("<"),
    CONTAINS("contains"),
    IS_EMPTY("isEmpty"),
    IS_NOT_EMPTY("isNotEmpty")
}

/**
 * Structured condition model for conditional branching steps.
 */
data class Condition(
    val field: String,
    val operator: ConditionOperator,
    val value: String,
    val description: String = "IF $field ${operator.symbol} $value"
)
