package com.routineforge.app.recorder

import com.routineforge.app.domain.model.ActionType
import com.routineforge.app.recorder.model.RecordingState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ActionRecorderTest {

    private lateinit var recorder: DefaultActionRecorder

    @Before
    fun setUp() {
        recorder = DefaultActionRecorder()
    }

    @Test
    fun `startRecording initializes state and clears previous actions`() {
        recorder.startRecording()
        assertEquals(RecordingState.RECORDING, recorder.recordingState.value)
        assertTrue(recorder.currentActions.value.isEmpty())
    }

    @Test
    fun `workspace normalization generates valid SELECT_FILE action`() {
        recorder.startRecording()

        val action = ActionNormalizer.normalizeSelectDocument(
            fileName = "Project_Report.pdf",
            folderPath = "/Documents/Reports",
            sequenceNumber = 1
        )
        recorder.recordAction(action)

        val recorded = recorder.currentActions.value
        assertEquals(1, recorded.size)
        assertEquals(ActionType.SELECT_FILE, recorded[0].actionType)
        assertEquals("Select Document", recorded[0].title)
        assertEquals(1, recorded[0].sequenceNumber)
    }

    @Test
    fun `workspace normalization generates valid RENAME_FILE action`() {
        recorder.startRecording()

        val action = ActionNormalizer.normalizeRenameDocument(
            oldName = "Project_Report.pdf",
            newName = "Final_Project_Report.pdf",
            folderPath = "/Documents/Reports",
            sequenceNumber = 1
        )
        recorder.recordAction(action)

        val recorded = recorder.currentActions.value
        assertEquals(1, recorded.size)
        assertEquals(ActionType.RENAME_FILE, recorded[0].actionType)
        assertEquals("Rename File", recorded[0].title)
    }

    @Test
    fun `pause and resume update state properly`() {
        recorder.startRecording()
        assertEquals(RecordingState.RECORDING, recorder.recordingState.value)

        recorder.pauseRecording()
        assertEquals(RecordingState.PAUSED, recorder.recordingState.value)

        recorder.resumeRecording()
        assertEquals(RecordingState.RECORDING, recorder.recordingState.value)
    }

    @Test
    fun `deleteAction removes action and renumbers sequence numbers`() {
        recorder.startRecording()

        val act1 = ActionNormalizer.normalizeSelectDocument("Doc1.pdf", "/Docs", 1)
        val act2 = ActionNormalizer.normalizeSelectDocument("Doc2.pdf", "/Docs", 2)
        val act3 = ActionNormalizer.normalizeSelectDocument("Doc3.pdf", "/Docs", 3)

        recorder.recordAction(act1)
        recorder.recordAction(act2)
        recorder.recordAction(act3)

        val currentList = recorder.currentActions.value
        assertEquals(3, currentList.size)

        // Delete the middle action (act2)
        recorder.deleteAction(currentList[1].id)

        val updatedList = recorder.currentActions.value
        assertEquals(2, updatedList.size)
        // Verify sequence numbers were renumbered to 1 and 2
        assertEquals(1, updatedList[0].sequenceNumber)
        assertEquals(2, updatedList[1].sequenceNumber)
    }

    @Test
    fun `reorderActions updates positions and sequence numbers`() {
        recorder.startRecording()

        val act1 = ActionNormalizer.normalizeSelectDocument("First.pdf", "/Docs", 1)
        val act2 = ActionNormalizer.normalizeRenameDocument("Second.pdf", "RenamedSecond.pdf", "/Docs", 2)

        recorder.recordAction(act1)
        recorder.recordAction(act2)

        // Swap position 0 and 1
        recorder.reorderActions(0, 1)

        val list = recorder.currentActions.value
        assertEquals(ActionType.RENAME_FILE, list[0].actionType)
        assertEquals(1, list[0].sequenceNumber)
        assertEquals(ActionType.SELECT_FILE, list[1].actionType)
        assertEquals(2, list[1].sequenceNumber)
    }

    @Test
    fun `stopRecording returns completed RecordingSession`() {
        recorder.startRecording()

        val act1 = ActionNormalizer.normalizeSelectDocument("Doc1.pdf", "/Docs", 1)
        recorder.recordAction(act1)

        val session = recorder.stopRecording()
        assertEquals(RecordingState.COMPLETED, recorder.recordingState.value)
        assertEquals(RecordingState.COMPLETED, session.status)
        assertEquals(1, session.actions.size)
        assertFalse(session.sessionId.isBlank())
    }
}
