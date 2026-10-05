package com.routineforge.app.domain

import com.routineforge.app.data.repository.InMemoryRoutineRepository
import com.routineforge.app.domain.execution.DefaultRoutineExecutionEngine
import com.routineforge.app.domain.execution.InMemoryExecutionRepository
import com.routineforge.app.domain.model.ActionType
import com.routineforge.app.domain.model.ExecutionMode
import com.routineforge.app.domain.model.ExecutionStatus
import com.routineforge.app.domain.model.RiskLevel
import com.routineforge.app.domain.model.Routine
import com.routineforge.app.domain.model.RoutineStep
import com.routineforge.app.domain.model.StepParameter
import com.routineforge.app.domain.model.Trigger
import com.routineforge.app.domain.workspace.InMemorySimulationWorkspace
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ExecutionEngineTest {

    private lateinit var workspace: InMemorySimulationWorkspace
    private lateinit var executionRepo: InMemoryExecutionRepository
    private lateinit var routineRepo: InMemoryRoutineRepository
    private lateinit var engine: DefaultRoutineExecutionEngine

    private lateinit var testRoutine: Routine

    @Before
    fun setUp() {
        workspace = InMemorySimulationWorkspace()
        workspace.reset()
        executionRepo = InMemoryExecutionRepository()
        routineRepo = InMemoryRoutineRepository.instance

        testRoutine = Routine(
            id = "exec_test_routine_1",
            name = "Organize Assignment Documents",
            description = "Rename selected document and move it.",
            category = "Documents & Files",
            trigger = Trigger.Manual("When document selected"),
            steps = listOf(
                RoutineStep("s1", 1, ActionType.SELECT_FILE, "Select Document", "Select file", listOf(StepParameter("fileName", "Assignment.docx")), riskLevel = RiskLevel.LOW, requiresConfirmation = false),
                RoutineStep("s2", 2, ActionType.RENAME_FILE, "Rename Document", "Rename file", listOf(StepParameter("oldName", "Assignment.docx"), StepParameter("newName", "Renamed_Assignment.docx")), riskLevel = RiskLevel.MEDIUM, requiresConfirmation = false),
                RoutineStep("s3", 3, ActionType.MOVE_FILE, "Move Document", "Move file", listOf(StepParameter("fileName", "Renamed_Assignment.docx"), StepParameter("destinationFolder", "/Documents/Organized")), riskLevel = RiskLevel.MEDIUM, requiresConfirmation = false)
            )
        )
    }

    @Test
    fun `validateRoutine passes valid document routine`() {
        engine = DefaultRoutineExecutionEngine(executionRepository = executionRepo, routineRepository = routineRepo)
        val result = engine.validateRoutine(testRoutine)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `validateRoutine fails for routine with empty steps`() {
        engine = DefaultRoutineExecutionEngine(executionRepository = executionRepo, routineRepository = routineRepo)
        val emptyRoutine = testRoutine.copy(steps = emptyList())
        val result = engine.validateRoutine(emptyRoutine)
        assertTrue(result.isFailure)
    }

    @Test
    fun `simulation execution runs all 3 steps successfully and updates workspace`() = runBlocking {
        engine = DefaultRoutineExecutionEngine(
            executionRepository = executionRepo,
            routineRepository = routineRepo
        )

        val prepResult = engine.prepareExecution(testRoutine, ExecutionMode.SIMULATION)
        assertTrue(prepResult.isSuccess)

        val stateAfterPrep = engine.executionState.value
        assertEquals(ExecutionStatus.QUEUED, stateAfterPrep.status)
        assertEquals(3, stateAfterPrep.totalSteps)

        engine.startExecution()

        val finalState = engine.executionState.value
        assertEquals(ExecutionStatus.COMPLETED, finalState.status)
        assertEquals(3, finalState.completedStepResults.size)
        assertTrue(finalState.completedStepResults.all { it.success })

        // Verify ExecutionResult saved in repository
        val history = executionRepo.executionHistory.value
        assertTrue(history.isNotEmpty())
        assertEquals(ExecutionStatus.COMPLETED, history.first().status)
        assertEquals(3, history.first().completedStepCount)
    }

    @Test
    fun `cancelExecution halts execution and marks status as CANCELLED`() = runBlocking {
        engine = DefaultRoutineExecutionEngine(
            executionRepository = executionRepo,
            routineRepository = routineRepo
        )

        engine.prepareExecution(testRoutine, ExecutionMode.SIMULATION)
        engine.cancelExecution()

        val state = engine.executionState.value
        assertEquals(ExecutionStatus.CANCELLED, state.status)

        val history = executionRepo.executionHistory.value
        assertTrue(history.isNotEmpty())
        assertEquals(ExecutionStatus.CANCELLED, history.first().status)
    }
}
