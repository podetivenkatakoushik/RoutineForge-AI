package com.routineforge.app.ui.screens.recording

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.routineforge.app.recorder.ActionNormalizer
import com.routineforge.app.recorder.ActionRecorder
import com.routineforge.app.recorder.DefaultActionRecorder
import com.routineforge.app.recorder.InMemoryRecordingSessionStore
import com.routineforge.app.recorder.RecordingSessionStore
import com.routineforge.app.recorder.model.RecordedAction
import com.routineforge.app.recorder.model.RecordingSession
import com.routineforge.app.recorder.model.RecordingState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn

data class WorkspaceDocument(
    val name: String,
    val folderPath: String = "/Documents/Reports",
    val category: String,
    val sizeText: String
)

data class RecordingWorkspaceUiState(
    val recordingState: RecordingState = RecordingState.IDLE,
    val elapsedTimeMs: Long = 0L,
    val actions: List<RecordedAction> = emptyList(),
    val documents: List<WorkspaceDocument> = listOf(
        WorkspaceDocument("Project_Report.pdf", "/Documents/Reports", "Reports", "2.4 MB"),
        WorkspaceDocument("Assignment.docx", "/Documents/Reports", "Documents", "1.1 MB"),
        WorkspaceDocument("Meeting_Notes.txt", "/Documents/Reports", "Notes", "45 KB"),
        WorkspaceDocument("Research_Data.csv", "/Documents/Reports", "Spreadsheets", "820 KB")
    ),
    val folders: List<String> = listOf("/Documents/Reports", "/Documents/Organized", "/Documents/Archive"),
    val selectedDocument: WorkspaceDocument? = null,
    val renameInputText: String = "",
    val selectedDestinationFolder: String = "/Documents/Organized",
    val newFolderNameInput: String = "",
    val lastSession: RecordingSession? = null
) {
    val formattedTimer: String
        get() {
            val seconds = elapsedTimeMs / 1000
            val mins = seconds / 60
            val remSecs = seconds % 60
            return String.format("%02d:%02d", mins, remSecs)
        }
}

class RecordingWorkspaceViewModel(
    val actionRecorder: ActionRecorder = DefaultActionRecorder(),
    private val sessionStore: RecordingSessionStore = InMemoryRecordingSessionStore.instance
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecordingWorkspaceUiState())
    val uiState: StateFlow<RecordingWorkspaceUiState> = _uiState.asStateFlow()

    init {
        actionRecorder.startRecording()

        combine(
            actionRecorder.recordingState,
            actionRecorder.elapsedTimeMs,
            actionRecorder.currentActions
        ) { state, time, actions ->
            _uiState.value = _uiState.value.copy(
                recordingState = state,
                elapsedTimeMs = time,
                actions = actions
            )
        }.launchIn(viewModelScope)
    }

    fun selectDocument(doc: WorkspaceDocument) {
        _uiState.value = _uiState.value.copy(
            selectedDocument = doc,
            renameInputText = "Renamed_${doc.name}"
        )
        val action = ActionNormalizer.normalizeSelectDocument(
            fileName = doc.name,
            folderPath = doc.folderPath,
            sequenceNumber = _uiState.value.actions.size + 1
        )
        actionRecorder.recordAction(action)
    }

    fun updateRenameInput(text: String) {
        _uiState.value = _uiState.value.copy(renameInputText = text)
    }

    fun performRename() {
        val selected = _uiState.value.selectedDocument ?: return
        val newName = _uiState.value.renameInputText.trim()
        if (newName.isBlank() || newName == selected.name) return

        val updatedDocs = _uiState.value.documents.map { doc ->
            if (doc.name == selected.name) doc.copy(name = newName) else doc
        }
        val updatedSelected = selected.copy(name = newName)

        _uiState.value = _uiState.value.copy(
            documents = updatedDocs,
            selectedDocument = updatedSelected
        )

        val action = ActionNormalizer.normalizeRenameDocument(
            oldName = selected.name,
            newName = newName,
            folderPath = selected.folderPath,
            sequenceNumber = _uiState.value.actions.size + 1
        )
        actionRecorder.recordAction(action)
    }

    fun selectDestinationFolder(folder: String) {
        _uiState.value = _uiState.value.copy(selectedDestinationFolder = folder)
    }

    fun performMove() {
        val selected = _uiState.value.selectedDocument ?: return
        val dest = _uiState.value.selectedDestinationFolder

        val updatedDocs = _uiState.value.documents.map { doc ->
            if (doc.name == selected.name) doc.copy(folderPath = dest) else doc
        }

        _uiState.value = _uiState.value.copy(documents = updatedDocs)

        val action = ActionNormalizer.normalizeMoveDocument(
            fileName = selected.name,
            sourceFolder = selected.folderPath,
            destinationFolder = dest,
            sequenceNumber = _uiState.value.actions.size + 1
        )
        actionRecorder.recordAction(action)
    }

    fun updateNewFolderNameInput(text: String) {
        _uiState.value = _uiState.value.copy(newFolderNameInput = text)
    }

    fun performCreateFolder() {
        val folderName = _uiState.value.newFolderNameInput.trim()
        if (folderName.isBlank()) return

        val newFolderPath = "/Documents/$folderName"
        val updatedFolders = _uiState.value.folders.toMutableList().apply {
            if (!contains(newFolderPath)) add(newFolderPath)
        }

        _uiState.value = _uiState.value.copy(
            folders = updatedFolders,
            newFolderNameInput = "",
            selectedDestinationFolder = newFolderPath
        )

        val action = ActionNormalizer.normalizeCreateFolder(
            folderName = folderName,
            parentPath = "/Documents",
            sequenceNumber = _uiState.value.actions.size + 1
        )
        actionRecorder.recordAction(action)
    }

    fun performExtractText() {
        val selected = _uiState.value.selectedDocument ?: return

        val action = ActionNormalizer.normalizeExtractText(
            fileName = selected.name,
            extractedSummary = "Header, Date, and Key Metrics extracted successfully.",
            sequenceNumber = _uiState.value.actions.size + 1
        )
        actionRecorder.recordAction(action)
    }

    fun togglePauseResume() {
        if (_uiState.value.recordingState == RecordingState.RECORDING) {
            actionRecorder.pauseRecording()
        } else if (_uiState.value.recordingState == RecordingState.PAUSED) {
            actionRecorder.resumeRecording()
        }
    }

    fun stopRecording(): RecordingSession {
        val session = actionRecorder.stopRecording()
        _uiState.value = _uiState.value.copy(lastSession = session)
        sessionStore.saveSession(session)
        return session
    }

    fun clearWorkspaceSession() {
        actionRecorder.clear()
        actionRecorder.startRecording()
    }
}
