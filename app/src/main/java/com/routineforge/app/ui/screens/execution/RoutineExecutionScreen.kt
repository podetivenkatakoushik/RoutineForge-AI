package com.routineforge.app.ui.screens.execution

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.routineforge.app.domain.model.ExecutionStatus
import com.routineforge.app.domain.model.RoutineStep
import com.routineforge.app.ui.components.PrimaryButton
import com.routineforge.app.ui.components.RoutineForgeCard
import com.routineforge.app.ui.components.SecondaryButton

@Composable
fun RoutineExecutionScreen(
    onExecutionFinished: () -> Unit,
    onShowSnackbar: (String) -> Unit,
    viewModel: RoutineExecutionViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val routine = uiState.activeRoutine

    // Auto-navigate to result screen when finished
    LaunchedEffect(uiState.status) {
        if (uiState.status == ExecutionStatus.COMPLETED ||
            uiState.status == ExecutionStatus.FAILED ||
            uiState.status == ExecutionStatus.CANCELLED) {
            onExecutionFinished()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // HEADER
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = routine?.name ?: "Executing Routine",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Controlled Workspace Simulation",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = when (uiState.status) {
                    ExecutionStatus.RUNNING -> MaterialTheme.colorScheme.primaryContainer
                    ExecutionStatus.PAUSED -> MaterialTheme.colorScheme.secondaryContainer
                    ExecutionStatus.FAILED -> MaterialTheme.colorScheme.errorContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
            ) {
                Text(
                    text = uiState.status.name,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = when (uiState.status) {
                        ExecutionStatus.RUNNING -> MaterialTheme.colorScheme.primary
                        ExecutionStatus.PAUSED -> MaterialTheme.colorScheme.secondary
                        ExecutionStatus.FAILED -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // PROGRESS CARD
        RoutineForgeCard(
            borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            backgroundColor = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Step ${uiState.completedStepResults.size} of ${uiState.totalSteps}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${(uiState.progressFraction * 100).toInt()}% Completed",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { uiState.progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surface
            )

            if (uiState.currentStep != null && uiState.status == ExecutionStatus.RUNNING) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Currently executing: ${uiState.currentStep?.title}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // HUMAN CONFIRMATION CARD (IF PAUSED FOR CONFIRMATION)
        val confirmStep = uiState.currentConfirmationStep
        if (confirmStep != null) {
            RoutineForgeCard(
                borderColor = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                backgroundColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Approval Required",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "RoutineForge requires your approval before executing high-risk action '${confirmStep.title}' (${confirmStep.actionType.name}).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PrimaryButton(
                        text = "Approve Action",
                        icon = Icons.Default.Check,
                        onClick = { viewModel.confirmStepApproval(true) },
                        modifier = Modifier.weight(1f)
                    )
                    SecondaryButton(
                        text = "Cancel Routine",
                        icon = Icons.Default.Close,
                        onClick = { viewModel.confirmStepApproval(false) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // STEP TIMELINE LIST
        Text(
            text = "Execution Timeline",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        val steps = routine?.steps ?: emptyList()
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            itemsIndexed(steps, key = { _, st -> st.id }) { index, step ->
                val result = uiState.completedStepResults.getOrNull(index)
                val isCurrent = index == uiState.currentStepIndex && (uiState.status == ExecutionStatus.RUNNING || uiState.status == ExecutionStatus.PAUSED)

                ExecutionStepProgressCard(
                    step = step,
                    isCompleted = result != null && result.success,
                    isFailed = result != null && !result.success,
                    isCurrent = isCurrent,
                    outputMessage = result?.outputMessage
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // CONTROLS BAR: PAUSE / RESUME / CANCEL
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SecondaryButton(
                text = if (uiState.status == ExecutionStatus.RUNNING) "Pause" else "Resume",
                icon = if (uiState.status == ExecutionStatus.RUNNING) Icons.Default.Pause else Icons.Default.PlayArrow,
                onClick = {
                    if (uiState.status == ExecutionStatus.RUNNING) {
                        viewModel.pauseSimulation()
                        onShowSnackbar("Execution paused.")
                    } else {
                        viewModel.resumeSimulation()
                        onShowSnackbar("Execution resumed.")
                    }
                },
                modifier = Modifier.weight(1f)
            )

            SecondaryButton(
                text = "Cancel Routine",
                icon = Icons.Default.Stop,
                onClick = {
                    viewModel.cancelSimulation()
                    onShowSnackbar("Execution cancelled.")
                },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ExecutionStepProgressCard(
    step: RoutineStep,
    isCompleted: Boolean,
    isFailed: Boolean,
    isCurrent: Boolean,
    outputMessage: String?
) {
    RoutineForgeCard(
        borderColor = when {
            isCompleted -> MaterialTheme.colorScheme.primary
            isFailed -> MaterialTheme.colorScheme.error
            isCurrent -> MaterialTheme.colorScheme.secondary
            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = when {
                    isCompleted -> MaterialTheme.colorScheme.primaryContainer
                    isFailed -> MaterialTheme.colorScheme.errorContainer
                    isCurrent -> MaterialTheme.colorScheme.secondaryContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant
                },
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    when {
                        isCompleted -> Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Completed", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        isFailed -> Icon(imageVector = Icons.Default.Error, contentDescription = "Failed", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                        isCurrent -> Text("●", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                        else -> Text("${step.order}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
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
                        text = step.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = step.actionType.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = outputMessage ?: step.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
