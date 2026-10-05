package com.routineforge.app.ai

import com.routineforge.app.domain.model.ActionType
import com.routineforge.app.domain.model.RiskLevel
import com.routineforge.app.recorder.ActionNormalizer
import com.routineforge.app.recorder.model.RecordingSession
import com.routineforge.app.recorder.model.RecordingState
import com.routineforge.app.recorder.model.WorkflowCandidate
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AIGeneratorTest {

    private lateinit var mockAiProvider: MockAIProvider
    private lateinit var generateRoutineUseCase: GenerateRoutineUseCase

    @Before
    fun setUp() {
        mockAiProvider = MockAIProvider()
        generateRoutineUseCase = GenerateRoutineUseCase(aiProvider = mockAiProvider)
    }

    @Test
    fun `mockAiProvider generates valid RoutineProposal from document workflow`() = runBlocking {
        val act1 = ActionNormalizer.normalizeSelectDocument("Assignment.docx", "/Documents/Reports", 1)
        val act2 = ActionNormalizer.normalizeRenameDocument("Assignment.docx", "Renamed_Assignment.docx", "/Documents/Reports", 2)
        val act3 = ActionNormalizer.normalizeMoveDocument("Renamed_Assignment.docx", "/Documents/Reports", "/Documents/Organized", 3)

        val session = RecordingSession(
            sessionId = "rec_test_123",
            startedAt = System.currentTimeMillis() - 240000,
            endedAt = System.currentTimeMillis(),
            status = RecordingState.COMPLETED,
            actions = listOf(act1, act2, act3),
            durationMs = 240000L
        )

        val candidate = WorkflowCandidate(
            candidateId = "cand_test_123",
            name = "Test Workflow",
            description = "Test description",
            session = session
        )

        val result = generateRoutineUseCase(candidate)
        assertTrue(result.isSuccess)

        val proposal = result.getOrNull()
        assertNotNull(proposal)
        assertEquals("Organize Assignment Documents", proposal!!.name)
        assertEquals(3, proposal.steps.size)
        assertEquals(ActionType.SELECT_FILE, proposal.steps[0].actionType)
        assertEquals(ActionType.RENAME_FILE, proposal.steps[1].actionType)
        assertEquals(ActionType.MOVE_FILE, proposal.steps[2].actionType)
        assertEquals(RiskLevel.MEDIUM, proposal.overallRiskLevel)
        assertEquals(92, proposal.confidencePercentage)
    }

    @Test
    fun `RoutineProposalValidator correctly parses valid JSON`() {
        val rawJson = """
        {
          "name": "Organize Assignment Documents",
          "description": "Rename selected assignment documents and move them.",
          "intent": "Organize assignment documents",
          "category": "Documents & Files",
          "trigger": {
            "type": "MANUAL",
            "displayTitle": "When document selected"
          },
          "steps": [
            {
              "order": 1,
              "actionType": "SELECT_FILE",
              "title": "Select Document",
              "description": "User selected Assignment.docx",
              "parameters": [{"key": "fileName", "value": "Assignment.docx"}],
              "riskLevel": "LOW",
              "requiresConfirmation": false
            },
            {
              "order": 2,
              "actionType": "RENAME_FILE",
              "title": "Rename Document",
              "description": "User renamed file",
              "parameters": [{"key": "newName", "value": "Renamed_Assignment.docx"}],
              "riskLevel": "MEDIUM",
              "requiresConfirmation": true
            }
          ],
          "estimatedManualDurationSeconds": 240,
          "estimatedRoutineDurationSeconds": 60,
          "reasoningSummary": "Detected document organization workflow.",
          "confidence": 0.95
        }
        """.trimIndent()

        val validation = RoutineProposalValidator.parseAndValidate(rawJson)
        assertTrue(validation is RoutineProposalValidationResult.Success)

        val proposal = (validation as RoutineProposalValidationResult.Success).proposal
        assertEquals("Organize Assignment Documents", proposal.name)
        assertEquals(2, proposal.steps.size)
        assertEquals(95, proposal.confidencePercentage)
        assertEquals("3m 0s", proposal.timeSavedFormatted)
    }

    @Test
    fun `RoutineProposalValidator returns error on malformed JSON`() {
        val invalidJson = "{ invalid_json: true, "
        val validation = RoutineProposalValidator.parseAndValidate(invalidJson)
        assertTrue(validation is RoutineProposalValidationResult.Error)
    }

    @Test
    fun `GenerateRoutineUseCase returns failure for empty workflow candidate`() = runBlocking {
        val emptySession = RecordingSession(
            sessionId = "empty_123",
            startedAt = System.currentTimeMillis(),
            endedAt = System.currentTimeMillis(),
            status = RecordingState.COMPLETED,
            actions = emptyList(),
            durationMs = 0L
        )

        val candidate = WorkflowCandidate(
            candidateId = "cand_empty",
            name = "Empty",
            description = "Empty",
            session = emptySession
        )

        val result = generateRoutineUseCase(candidate)
        assertTrue(result.isFailure)
    }
}
