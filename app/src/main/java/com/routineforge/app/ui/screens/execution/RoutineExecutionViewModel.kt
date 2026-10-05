package com.routineforge.app.ui.screens.execution

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.routineforge.app.domain.execution.DefaultRoutineExecutionEngine
import com.routineforge.app.domain.execution.RoutineExecutionEngine
import com.routineforge.app.domain.execution.RoutineExecutionState
import com.routineforge.app.domain.model.ExecutionMode
import com.routineforge.app.domain.model.Routine
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RoutineExecutionViewModel(
    val engine: RoutineExecutionEngine = DefaultRoutineExecutionEngine.instance
) : ViewModel() {

    val uiState: StateFlow<RoutineExecutionState> = engine.executionState

    fun prepareRoutine(routine: Routine): Result<Unit> {
        return engine.prepareExecution(routine, ExecutionMode.SIMULATION)
    }

    fun startSimulation() {
        viewModelScope.launch {
            engine.startExecution()
        }
    }

    fun pauseSimulation() {
        viewModelScope.launch {
            engine.pauseExecution()
        }
    }

    fun resumeSimulation() {
        viewModelScope.launch {
            engine.resumeExecution()
        }
    }

    fun cancelSimulation() {
        viewModelScope.launch {
            engine.cancelExecution()
        }
    }

    fun confirmStepApproval(approved: Boolean) {
        viewModelScope.launch {
            engine.confirmStepApproval(approved)
        }
    }
}
