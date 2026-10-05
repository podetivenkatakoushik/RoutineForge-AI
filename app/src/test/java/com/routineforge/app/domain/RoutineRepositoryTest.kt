package com.routineforge.app.domain

import com.routineforge.app.data.repository.InMemoryRoutineRepository
import com.routineforge.app.domain.model.ActionType
import com.routineforge.app.domain.model.Routine
import com.routineforge.app.domain.model.RoutineStatus
import com.routineforge.app.domain.model.RoutineStep
import com.routineforge.app.domain.model.TimeSavedMetrics
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RoutineRepositoryTest {

    private lateinit var repository: InMemoryRoutineRepository

    @Before
    fun setUp() {
        repository = InMemoryRoutineRepository()
    }

    @Test
    fun `getAllRoutines returns initial sample routines`() = runBlocking {
        val routines = repository.getAllRoutines().first()
        assertTrue(routines.isNotEmpty())
        assertEquals(5, routines.size)
    }

    @Test
    fun `organize reports routine has valid steps and ordering`() = runBlocking {
        val routine = repository.getRoutineById("r1").first()
        assertNotNull(routine)
        assertEquals("Organize Reports", routine!!.name)
        assertEquals(4, routine.steps.size)

        // Verify order sequence
        assertEquals(1, routine.steps[0].order)
        assertEquals(ActionType.SELECT_FILE, routine.steps[0].actionType)
        assertEquals(2, routine.steps[1].order)
        assertEquals(ActionType.EXTRACT_TEXT, routine.steps[1].actionType)
        assertEquals(3, routine.steps[2].order)
        assertEquals(ActionType.RENAME_FILE, routine.steps[2].actionType)
        assertEquals(4, routine.steps[3].order)
        assertEquals(ActionType.MOVE_FILE, routine.steps[3].actionType)
    }

    @Test
    fun `url investigation routine steps are structured safely`() = runBlocking {
        val routine = repository.getRoutineById("r3").first()
        assertNotNull(routine)
        assertEquals("URL Investigation", routine!!.name)
        assertEquals(4, routine.steps.size)
        assertEquals(ActionType.USER_INPUT, routine.steps[0].actionType)
        assertEquals(ActionType.SHOW_RESULT, routine.steps[3].actionType)
    }

    @Test
    fun `saveRoutine adds new routine and updates flow`() = runBlocking {
        val newRoutine = Routine(
            id = "test_r1",
            name = "Test Automation",
            description = "Test description",
            status = RoutineStatus.READY,
            steps = listOf(
                RoutineStep(
                    id = "ts1",
                    order = 1,
                    actionType = ActionType.READ_FILE,
                    title = "Read Input",
                    description = "Read input text"
                )
            )
        )

        val saved = repository.saveRoutine(newRoutine)
        assertTrue(saved)

        val retrieved = repository.getRoutineById("test_r1").first()
        assertNotNull(retrieved)
        assertEquals("Test Automation", retrieved!!.name)
        assertEquals(RoutineStatus.READY, retrieved.status)
    }

    @Test
    fun `updateRoutine modifies existing routine`() = runBlocking {
        val original = repository.getRoutineById("r1").first()!!
        val updated = original.copy(description = "Updated description for test")

        repository.updateRoutine(updated)

        val retrieved = repository.getRoutineById("r1").first()
        assertEquals("Updated description for test", retrieved!!.description)
    }

    @Test
    fun `deleteRoutine removes routine from state`() = runBlocking {
        val deleted = repository.deleteRoutine("r1")
        assertTrue(deleted)

        val retrieved = repository.getRoutineById("r1").first()
        assertNull(retrieved)

        val remaining = repository.getAllRoutines().first()
        assertEquals(4, remaining.size)
    }

    @Test
    fun `time saved metrics calculates difference correctly`() {
        val metrics = TimeSavedMetrics(
            manualEstimatedDurationMs = 15 * 60 * 1000L,
            automatedDurationMs = 3 * 60 * 1000L
        )
        assertEquals(12 * 60 * 1000L, metrics.timeSavedMs)
        assertEquals("12 min", metrics.timeSavedFormatted)
    }
}
