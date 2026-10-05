package com.routineforge.app.domain.model

enum class ParameterType {
    TEXT,
    FILE_PATH,
    FOLDER_PATH,
    NUMBER,
    BOOLEAN,
    JSON,
    URL
}

/**
 * Type-safe parameter structure for routine step execution parameters.
 */
data class StepParameter(
    val key: String,
    val value: String,
    val label: String = key,
    val type: ParameterType = ParameterType.TEXT,
    val isSensitive: Boolean = false
)
