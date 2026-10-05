package com.routineforge.app.ui.screens.discover

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.routineforge.app.ui.components.AILoadingState
import com.routineforge.app.ui.components.PrimaryButton
import com.routineforge.app.ui.components.RoutineForgeCard
import com.routineforge.app.ui.components.SecondaryButton
import com.routineforge.app.ui.components.SectionHeader

@Composable
fun DiscoverScreen(
    onNavigateToRecording: () -> Unit,
    onShowSnackbar: (String) -> Unit,
    viewModel: DiscoverViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // HEADER
        Text(
            text = uiState.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = uiState.subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (uiState.isSimulatingAILoading) {
            AILoadingState()
            Spacer(modifier = Modifier.height(16.dp))
            SecondaryButton(
                text = "Cancel Simulation",
                onClick = { viewModel.toggleAILoadingSimulation() }
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // OPTION 1: Record Actions
        DiscoverOptionCard(
            icon = Icons.Default.TouchApp,
            title = "Record Actions",
            description = "Perform the workflow once and let RoutineForge understand the sequence.",
            buttonText = "Start Recording",
            onButtonClick = onNavigateToRecording
        )

        Spacer(modifier = Modifier.height(16.dp))

        // OPTION 2: Explain with Voice
        DiscoverOptionCard(
            icon = Icons.Default.Mic,
            title = "Explain with Voice",
            description = "Describe the repetitive task in your own words.",
            buttonText = "Describe Routine",
            onButtonClick = {
                onShowSnackbar("Voice input will be available in the next stage.")
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // OPTION 3: Show with Camera
        DiscoverOptionCard(
            icon = Icons.Default.CameraAlt,
            title = "Show with Camera",
            description = "Use the camera to capture documents, notes, or workflow context.",
            buttonText = "Use Camera",
            onButtonClick = {
                onShowSnackbar("Camera input will be available in the next stage.")
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // AI DEMO TOGGLE SECTION
        SectionHeader(
            title = "AI Synthesis Preview",
            subtitle = "Test the AI workflow parsing loading state"
        )
        SecondaryButton(
            text = if (uiState.isSimulatingAILoading) "Hide AI Processing Preview" else "Preview AI Processing State",
            icon = Icons.Default.PlayCircle,
            onClick = { viewModel.toggleAILoadingSimulation() }
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun DiscoverOptionCard(
    icon: ImageVector,
    title: String,
    description: String,
    buttonText: String,
    onButtonClick: () -> Unit
) {
    RoutineForgeCard(
        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(36.dp)
                    .padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        PrimaryButton(
            text = buttonText,
            onClick = onButtonClick,
            icon = icon
        )
    }
}
