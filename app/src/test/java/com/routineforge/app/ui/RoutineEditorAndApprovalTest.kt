package com.routineforge.app.ui

import com.routineforge.app.ai.InMemoryRoutineProposalStore
import com.routineforge.app.data.repository.InMemoryRoutineRepository
import com.routineforge.app.domain.model.ActionType
import com.routineforge.app.domain.model.RiskLevel
import com.routineforge.app.domain.model.RoutineProposal
import com.routineforge.app.domain.model.RoutineStep
import com.routineforge.app.domain.model.StepParameter
import com.routineforge.app.domain.model.Trigger
import com.routineforge.app.ui.screens.approval.RoutineApprovalViewModel
import com.routineforge.app.ui.screens.editor.RoutineEditorViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RoutineEditorAndApprovalTest {

    private lateinit var proposalStore: InMemoryRoutineProposalStore
    private lateinit var repository: InMemoryRoutineRepository
    private lateinit var editorViewModel: RoutineEditorViewModel
    private lateinit var approvalViewModel: RoutineApprovalViewModel

    @Before
    fun setUp() {
        proposalStore = InMemoryRoutineProposalStore()
        repository = InMemoryRoutineRepository.instance

        val initialProposal = RoutineProposal(
            id = "test_prop_123",
            name = "Organize Assignment Documents",
            description = "Rename selected assignment documents and move them.",
            intent = "Organize assignment documents into appropriate directory.",
            category = "Documents & Files",
            trigger = Trigger.Manual("When document selected"),
            steps = listOf(
                RoutineStep("s1", 1, ActionType.SELECT_FILE, "Select Document", "Select file", listOf(StepParameter("fileName", "Assignment.docx")), riskLevel = RiskLevel.LOW),
                RoutineStep("s2", 2, ActionType.RENAME_FILE, "Rename Document", "Rename file", listOf(StepParameter("newName", "Renamed_Assignment.docx")), riskLevel = RiskLevel.MEDIUM),
                RoutineStep("s3", 3, ActionType.MOVE_FILE, "Move Document", "Move file", listOf(StepParameter("destinationFolder", "/Documents/Organized")), riskLevel = RiskLevel.MEDIUM)
            ),
            estimatedManualDurationMs = 240000L,
            estimatedRoutineDurationMs = 60000L,
            confidenceScore = 0.92f
        )
        proposalStore.saveProposal(initialProposal)

        editorViewModel = RoutineEditorViewModel(proposalStore = proposalStore)
        approvalViewModel = RoutineApprovalViewModel(proposalStore = proposalStore, routineRepository = repository)
    }

    @Test
    fun `editor updates proposal metadata and reorders steps`() {
        editorViewModel.updateName("Custom Organized Assignment Documents")
        editorViewModel.updateDescription("Updated custom description")
        editorViewModel.moveStepDown(0) // Move step 1 down to step 2

        var savedSuccess = false
        editorViewModel.saveChanges(
            onSuccess = { savedSuccess = true },
            onError = {}
        )

        assertTrue(savedSuccess)
        val updatedProposal = proposalStore.currentProposal.value
        assertNotNull(updatedProposal)
        assertEquals("Custom Organized Assignment Documents", updatedProposal!!.name)
        assertEquals("Updated custom description", updatedProposal.description)
        assertEquals(3, updatedProposal.steps.size)
        // Verify reordered step order
        assertEquals(ActionType.RENAME_FILE, updatedProposal.steps[0].actionType)
        assertEquals(1, updatedProposal.steps[0].order)
        assertEquals(ActionType.SELECT_FILE, updatedProposal.steps[1].actionType)
        assertEquals(2, updatedProposal.steps[1].order)
    }

    @Test
    fun `editor prevents saving when routine name is empty`() {
        editorViewModel.updateName("   ")
        var errorMessage: String? = null
        editorViewModel.saveChanges(
            onSuccess = {},
            onError = { err -> errorMessage = err }
        )

        assertNotNull(errorMessage)
        assertEquals("Routine name cannot be empty.", errorMessage)
    }

    @Test
    fun `approval saves final routine to repository and makes it immediately queryable`() = runBlocking {
        var approvedSuccess = false
        approvalViewModel.approveAndSaveRoutine(
            onSuccess = { approvedSuccess = true },
            onError = {}
        )

        assertTrue(approvedSuccess)

        val allRoutines = repository.getAllRoutines().first()
        val savedRoutine = allRoutines.find { it.name == "Organize Assignment Documents" }

        assertNotNull(savedRoutine)
        assertEquals("Organize Assignment Documents", savedRoutine!!.name)
        assertEquals(3, savedRoutine.steps.size)
        assertEquals(ActionType.SELECT_FILE, savedRoutine.steps[0].actionType)
        assertEquals(ActionType.RENAME_FILE, savedRoutine.steps[1].actionType)
        assertEquals(ActionType.MOVE_FILE, savedRoutine.steps[2].actionType)
        assertEquals(RiskLevel.MEDIUM, savedRoutine.riskLevel)
    }

    @Test
    fun `duplicate save call is rejected while saving`() = runBlocking {
        var successCount = 0
        approvalViewModel.approveAndSaveRoutine(
            onSuccess = { successCount++ },
            onError = {}
        )
        // Second call while already saved or saving
        approvalViewModel.approveAndSaveRoutine(
            onSuccess = { successCount++ },
            onError = {}
        )

        assertEquals(1, successCount)
    }
}
