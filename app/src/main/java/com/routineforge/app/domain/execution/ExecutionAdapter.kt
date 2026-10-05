package com.routineforge.app.domain.execution

import com.routineforge.app.domain.model.ExecutionStepResult
import com.routineforge.app.domain.model.RoutineStep

interface ExecutionAdapter {
    val name: String
    suspend fun executeStep(step: RoutineStep): ExecutionStepResult
}
