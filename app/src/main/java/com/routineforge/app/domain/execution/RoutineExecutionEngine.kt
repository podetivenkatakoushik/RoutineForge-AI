package com.routineforge.app.domain.execution

import com.routineforge.app.data.repository.InMemoryRoutineRepository
import com.routineforge.app.domain.analytics.AnalyticsRepository
import com.routineforge.app.domain.analytics.InMemoryAnalyticsRepository
import com.routineforge.app.domain.model.ActionType
import com.routineforge.app.domain.model.ExecutionMode
import com.routineforge.app.domain.model.ExecutionResult
import com.routineforge.app.domain.model.ExecutionStatus
import com.routineforge.app.domain.model.ExecutionStepResult
import com.routineforge.app.domain.model.RiskLevel
import com.routineforge.app.domain.model.Routine
import com.routineforge.app.domain.model.RoutineStep
import com.routineforge.app.domain.repository.RoutineRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RoutineExecutionState(
    val activeRoutine: Routine? = null,
    val status: ExecutionStatus = ExecutionStatus.QUEUED,
    val executionMode: ExecutionMode = ExecutionMode.SIMULATION,
    val currentStepIndex: Int = 0,
    val totalSteps: Int = 0,
    val currentStep: RoutineStep? = null,
    val completedStepResults: List<ExecutionStepResult> = emptyList(),
    val currentConfirmationStep: RoutineStep? = null,
    val lastExecutionResult: ExecutionResult? = null,
    val errorMessage: String? = null,
    val startTimeMs: Long = 0L,
    val endTimeMs: Long = 0L
) {
    val progressFraction: Float
        get() = if (totalSteps > 0) (currentStepIndex.toFloat() / totalSteps.toFloat()).coerceIn(0f, 1f) else 0f
}

interface RoutineExecutionEngine {
    val executionState: StateFlow<RoutineExecutionState>

    fun validateRoutine(routine: Routine): Result<Unit>
    fun prepareExecution(routine: Routine, mode: ExecutionMode = ExecutionMode.SIMULATION): Result<Unit>
    suspend fun startExecution()
    suspend fun pauseExecution()
    suspend fun resumeExecution()
    suspend fun cancelExecution()
    suspend fun confirmStepApproval(approved: Boolean)
}

class DefaultRoutineExecutionEngine(
    private val adapter: ExecutionAdapter = SimulationExecutionAdapter(),
    private val executionRepository: ExecutionRepository = InMemoryExecutionRepository.instance,
    private val routineRepository: RoutineRepository = InMemoryRoutineRepository.instance,
    private val analyticsRepository: AnalyticsRepository = InMemoryAnalyticsRepository.instance
) : RoutineExecutionEngine {

    private val _executionState = MutableStateFlow(RoutineExecutionState())
    override val executionState: StateFlow<RoutineExecutionState> = _executionState.asStateFlow()

    private var executionJob: Job? = null

    override fun validateRoutine(routine: Routine): Result<Unit> {
        if (routine.name.isBlank()) {
            return Result.failure(IllegalArgumentException("Routine name cannot be empty."))
        }
        if (routine.steps.isEmpty()) {
            return Result.failure(IllegalArgumentException("Routine contains no steps."))
        }
        val unsupportedStep = routine.steps.find {
            it.actionType != ActionType.SELECT_FILE &&
                    it.actionType != ActionType.RENAME_FILE &&
                    it.actionType != ActionType.MOVE_FILE &&
                    it.actionType != ActionType.COPY_FILE &&
                    it.actionType != ActionType.CREATE_FILE &&
                    it.actionType != ActionType.EXTRACT_TEXT &&
                    it.actionType != ActionType.TRANSFORM_DATA &&
                    it.actionType != ActionType.SHOW_RESULT &&
                    it.actionType != ActionType.READ_FILE &&
                    it.actionType != ActionType.EXTRACT_DATA &&
                    it.actionType != ActionType.USER_INPUT &&
                    it.actionType != ActionType.WAIT
        }
        if (unsupportedStep != null) {
            return Result.failure(IllegalArgumentException("Step '${unsupportedStep.title}' has unsupported action type '${unsupportedStep.actionType.name}'."))
        }
        return Result.success(Unit)
    }

    override fun prepareExecution(routine: Routine, mode: ExecutionMode): Result<Unit> {
        val validation = validateRoutine(routine)
        if (validation.isFailure) return validation

        _executionState.value = RoutineExecutionState(
            activeRoutine = routine,
            status = ExecutionStatus.QUEUED,
            executionMode = mode,
            currentStepIndex = 0,
            totalSteps = routine.steps.size,
            currentStep = routine.steps.firstOrNull(),
            completedStepResults = emptyList(),
            startTimeMs = System.currentTimeMillis()
        )
        return Result.success(Unit)
    }

    override suspend fun startExecution() {
        val currentState = _executionState.value
        val routine = currentState.activeRoutine ?: return
        if (currentState.status != ExecutionStatus.QUEUED) return

        _executionState.value = _executionState.value.copy(
            status = ExecutionStatus.RUNNING,
            startTimeMs = System.currentTimeMillis()
        )

        runExecutionLoop(routine)
    }

    override suspend fun pauseExecution() {
        if (_executionState.value.status == ExecutionStatus.RUNNING) {
            _executionState.value = _executionState.value.copy(status = ExecutionStatus.PAUSED)
        }
    }

    override suspend fun resumeExecution() {
        val currentState = _executionState.value
        val routine = currentState.activeRoutine ?: return
        if (currentState.status == ExecutionStatus.PAUSED) {
            _executionState.value = _executionState.value.copy(status = ExecutionStatus.RUNNING)
            runExecutionLoop(routine)
        }
    }

    override suspend fun cancelExecution() {
        executionJob?.cancel()
        executionJob = null

        val currentState = _executionState.value
        val endTime = System.currentTimeMillis()
        val routine = currentState.activeRoutine

        val result = ExecutionResult(
            executionId = "exec_cancelled_${System.currentTimeMillis()}",
            routineId = routine?.id ?: "unknown",
            routineName = routine?.name ?: "Routine",
            startTimeMs = currentState.startTimeMs,
            endTimeMs = endTime,
            status = ExecutionStatus.CANCELLED,
            completedStepCount = currentState.completedStepResults.size,
            estimatedManualDurationMs = routine?.timeSavedMetrics?.manualEstimatedDurationMs ?: 240000L,
            stepResults = currentState.completedStepResults,
            errorMessage = "Execution cancelled by user."
        )

        _executionState.value = _executionState.value.copy(
            status = ExecutionStatus.CANCELLED,
            lastExecutionResult = result,
            endTimeMs = endTime,
            currentConfirmationStep = null
        )

        executionRepository.saveExecutionResult(result)
        analyticsRepository.recordExecution(result)
    }

    override suspend fun confirmStepApproval(approved: Boolean) {
        if (approved) {
            _executionState.value = _executionState.value.copy(
                status = ExecutionStatus.RUNNING,
                currentConfirmationStep = null
            )
            val routine = _executionState.value.activeRoutine ?: return
            runExecutionLoop(routine)
        } else {
            cancelExecution()
        }
    }

    private suspend fun runExecutionLoop(routine: Routine) {
        val steps = routine.steps
        while (_executionState.value.currentStepIndex < steps.size && _executionState.value.status == ExecutionStatus.RUNNING) {
            val currentIndex = _executionState.value.currentStepIndex
            val step = steps[currentIndex]

            _executionState.value = _executionState.value.copy(
                currentStep = step,
                currentStepIndex = currentIndex
            )

            if (step.requiresConfirmation || step.riskLevel == RiskLevel.HIGH || step.riskLevel == RiskLevel.CRITICAL) {
                if (_executionState.value.currentConfirmationStep == null) {
                    _executionState.value = _executionState.value.copy(
                        status = ExecutionStatus.PAUSED,
                        currentConfirmationStep = step
                    )
                    return
                }
            }

            val stepResult = adapter.executeStep(step)
            val updatedResults = _executionState.value.completedStepResults.toMutableList().apply { add(stepResult) }

            if (stepResult.success) {
                val nextIndex = currentIndex + 1
                if (nextIndex < steps.size) {
                    _executionState.value = _executionState.value.copy(
                        currentStepIndex = nextIndex,
                        currentStep = steps[nextIndex],
                        completedStepResults = updatedResults,
                        currentConfirmationStep = null
                    )
                } else {
                    val endTime = System.currentTimeMillis()
                    val result = ExecutionResult(
                        executionId = "exec_${System.currentTimeMillis()}",
                        routineId = routine.id,
                        routineName = routine.name,
                        startTimeMs = _executionState.value.startTimeMs,
                        endTimeMs = endTime,
                        status = ExecutionStatus.COMPLETED,
                        completedStepCount = steps.size,
                        estimatedManualDurationMs = routine.timeSavedMetrics.manualEstimatedDurationMs,
                        stepResults = updatedResults
                    )

                    _executionState.value = _executionState.value.copy(
                        status = ExecutionStatus.COMPLETED,
                        currentStepIndex = steps.size,
                        completedStepResults = updatedResults,
                        lastExecutionResult = result,
                        endTimeMs = endTime,
                        currentConfirmationStep = null
                    )

                    executionRepository.saveExecutionResult(result)
                    analyticsRepository.recordExecution(result)

                    val updatedRoutine = routine.copy(
                        executionCount = routine.executionCount + 1,
                        lastExecutedAt = System.currentTimeMillis()
                    )
                    routineRepository.saveRoutine(updatedRoutine)
                    return
                }
            } else {
                val endTime = System.currentTimeMillis()
                val result = ExecutionResult(
                    executionId = "exec_failed_${System.currentTimeMillis()}",
                    routineId = routine.id,
                    routineName = routine.name,
                    startTimeMs = _executionState.value.startTimeMs,
                    endTimeMs = endTime,
                    status = ExecutionStatus.FAILED,
                    completedStepCount = updatedResults.count { it.success },
                    failedStepCount = 1,
                    estimatedManualDurationMs = routine.timeSavedMetrics.manualEstimatedDurationMs,
                    stepResults = updatedResults,
                    errorMessage = stepResult.errorMessage ?: "Step '${step.title}' failed."
                )

                _executionState.value = _executionState.value.copy(
                    status = ExecutionStatus.FAILED,
                    lastExecutionResult = result,
                    errorMessage = stepResult.errorMessage ?: "Execution failed at step '${step.title}'.",
                    endTimeMs = endTime,
                    currentConfirmationStep = null
                )

                executionRepository.saveExecutionResult(result)
                analyticsRepository.recordExecution(result)
                return
            }
        }
    }

    companion object {
        val instance: RoutineExecutionEngine by lazy { DefaultRoutineExecutionEngine() }
    }
}
