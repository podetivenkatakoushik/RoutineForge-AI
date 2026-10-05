package com.routineforge.app.ui.screens.recording

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DriveFileMove
import androidx.compose.material.icons.automirrored.filled.TextSnippet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.routineforge.app.recorder.model.RecordedAction
import com.routineforge.app.recorder.model.RecordingState
import com.routineforge.app.ui.components.EmptyStateView
import com.routineforge.app.ui.components.PrimaryButton
import com.routineforge.app.ui.components.RoutineForgeCard
import com.routineforge.app.ui.components.SecondaryButton
import com.routineforge.app.ui.components.SectionHeader

@Composable
fun RecordingWorkspaceScreen(
    onNavigateBack: () -> Unit,
    onRecordingCompleted: () -> Unit,
    onShowSnackbar: (String) -> Unit,
    viewModel: RecordingWorkspaceViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showLeaveConfirmDialog by remember { mutableStateOf(false) }

    // Intercept back button if recording is active
    BackHandler(enabled = uiState.recordingState == RecordingState.RECORDING || uiState.recordingState == RecordingState.PAUSED) {
        showLeaveConfirmDialog = true
    }

    if (showLeaveConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveConfirmDialog = false },
            title = { Text("Stop Recording?") },
            text = { Text("Leaving will stop the recording session and discard unreviewed actions.") },
            confirmButton = {
                TextButton(onClick = {
                    showLeaveConfirmDialog = false
                    viewModel.actionRecorder.clear()
                    onNavigateBack()
                }) {
                    Text("Leave Recording", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // TOP NAVIGATION & STATUS HEADER
        TopRecordingHeader(
            recordingState = uiState.recordingState,
            timerText = uiState.formattedTimer,
            actionCount = uiState.actions.size,
            onBackClick = {
                if (uiState.recordingState == RecordingState.RECORDING || uiState.recordingState == RecordingState.PAUSED) {
                    showLeaveConfirmDialog = true
                } else {
                    onNavigateBack()
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // RECORDING CONTROLS TOOLBAR
        RecordingControlsBar(
            recordingState = uiState.recordingState,
            onPauseResumeClick = { viewModel.togglePauseResume() },
            onClearClick = {
                viewModel.clearWorkspaceSession()
                onShowSnackbar("Recording session cleared.")
            },
            onStopReviewClick = {
                viewModel.stopRecording()
                onRecordingCompleted()
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // LAZY COLUMN FOR WORKSPACE + TIMELINE
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // WORKSPACE DEMO CARD
            item {
                SectionHeader(
                    title = "Controlled Workspace",
                    subtitle = "Demonstrate your workflow using sample documents"
                )
                WorkspaceDemoCard(
                    documents = uiState.documents,
                    folders = uiState.folders,
                    selectedDoc = uiState.selectedDocument,
                    renameText = uiState.renameInputText,
                    selectedFolder = uiState.selectedDestinationFolder,
                    newFolderText = uiState.newFolderNameInput,
                    onSelectDoc = { viewModel.selectDocument(it) },
                    onRenameTextChange = { viewModel.updateRenameInput(it) },
                    onPerformRename = {
                        viewModel.performRename()
                        onShowSnackbar("Recorded: RENAME_FILE action")
                    },
                    onSelectFolder = { viewModel.selectDestinationFolder(it) },
                    onPerformMove = {
                        viewModel.performMove()
                        onShowSnackbar("Recorded: MOVE_FILE action")
                    },
                    onNewFolderTextChange = { viewModel.updateNewFolderNameInput(it) },
                    onPerformCreateFolder = {
                        viewModel.performCreateFolder()
                        onShowSnackbar("Recorded: CREATE_FILE directory action")
                    },
                    onPerformExtractText = {
                        viewModel.performExtractText()
                        onShowSnackbar("Recorded: EXTRACT_TEXT action")
                    }
                )
            }

            // TIMELINE SECTION HEADER
            item {
                SectionHeader(
                    title = "Captured Action Timeline",
                    subtitle = "Structured semantic actions detected in real time"
                )
            }

            if (uiState.actions.isEmpty()) {
                item {
                    EmptyStateView(
                        title = "No actions captured yet",
                        description = "Perform an action in the workspace above to teach RoutineForge how you work."
                    )
                }
            } else {
                items(uiState.actions, key = { it.id }) { action ->
                    CapturedActionItemCard(action = action)
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun TopRecordingHeader(
    recordingState: RecordingState,
    timerText: String,
    actionCount: Int,
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = "Record Workflow",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            RecordingStatusBadge(recordingState = recordingState)
            Spacer(modifier = Modifier.width(10.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = timerText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun RecordingStatusBadge(recordingState: RecordingState) {
    val infiniteTransition = rememberInfiniteTransition(label = "Pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Alpha"
    )

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (recordingState == RecordingState.RECORDING) MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
        else MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .alpha(if (recordingState == RecordingState.RECORDING) alpha else 1f)
                    .background(
                        if (recordingState == RecordingState.RECORDING) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.outline
                    )
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = when (recordingState) {
                    RecordingState.RECORDING -> "● RECORDING"
                    RecordingState.PAUSED -> "PAUSED"
                    else -> "IDLE"
                },
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (recordingState == RecordingState.RECORDING) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RecordingControlsBar(
    recordingState: RecordingState,
    onPauseResumeClick: () -> Unit,
    onClearClick: () -> Unit,
    onStopReviewClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SecondaryButton(
            text = if (recordingState == RecordingState.RECORDING) "Pause" else "Resume",
            icon = if (recordingState == RecordingState.RECORDING) Icons.Default.Pause else Icons.Default.PlayArrow,
            onClick = onPauseResumeClick,
            modifier = Modifier.weight(1f)
        )
        SecondaryButton(
            text = "Clear",
            icon = Icons.Default.Refresh,
            onClick = onClearClick,
            modifier = Modifier.weight(0.8f)
        )
        PrimaryButton(
            text = "Stop & Review",
            icon = Icons.Default.Stop,
            onClick = onStopReviewClick,
            modifier = Modifier.weight(1.2f)
        )
    }
}

@Composable
private fun WorkspaceDemoCard(
    documents: List<WorkspaceDocument>,
    folders: List<String>,
    selectedDoc: WorkspaceDocument?,
    renameText: String,
    selectedFolder: String,
    newFolderText: String,
    onSelectDoc: (WorkspaceDocument) -> Unit,
    onRenameTextChange: (String) -> Unit,
    onPerformRename: () -> Unit,
    onSelectFolder: (String) -> Unit,
    onPerformMove: () -> Unit,
    onNewFolderTextChange: (String) -> Unit,
    onPerformCreateFolder: () -> Unit,
    onPerformExtractText: () -> Unit
) {
    RoutineForgeCard(
        borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    ) {
        Text(
            text = "Select Sample Document to Demonstrate:",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        // DOCUMENT LIST CHIPS/CARDS
        documents.forEach { doc ->
            val isSelected = selectedDoc?.name == doc.name
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(
                        width = 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .clickable { onSelectDoc(doc) },
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = doc.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${doc.folderPath}  •  ${doc.sizeText}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Selected",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // ACTION CONTROLS FOR SELECTED DOCUMENT
        AnimatedVisibility(visible = selectedDoc != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp)
            ) {
                Text(
                    text = "Demonstrate Action on '${selectedDoc?.name}':",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // ACTION 1: RENAME
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = renameText,
                        onValueChange = onRenameTextChange,
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("New file name") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedContainerColor = MaterialTheme.colorScheme.surface)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    PrimaryButton(
                        text = "Rename",
                        icon = Icons.Default.Edit,
                        onClick = onPerformRename,
                        modifier = Modifier.width(120.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // ACTION 2: MOVE TO FOLDER
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Destination: $selectedFolder",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    PrimaryButton(
                        text = "Move",
                        icon = Icons.AutoMirrored.Filled.DriveFileMove,
                        onClick = onPerformMove,
                        modifier = Modifier.width(120.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // ACTION 3: EXTRACT TEXT & CREATE DIRECTORY
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SecondaryButton(
                        text = "Extract Text",
                        icon = Icons.AutoMirrored.Filled.TextSnippet,
                        onClick = onPerformExtractText,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // ACTION 4: CREATE FOLDER
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newFolderText,
                        onValueChange = onNewFolderTextChange,
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("New folder name") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedContainerColor = MaterialTheme.colorScheme.surface)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    SecondaryButton(
                        text = "Create Dir",
                        icon = Icons.Default.CreateNewFolder,
                        onClick = onPerformCreateFolder,
                        modifier = Modifier.width(130.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CapturedActionItemCard(action: RecordedAction) {
    RoutineForgeCard(
        borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(28.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "${action.sequenceNumber}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = action.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = action.actionType.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = action.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
