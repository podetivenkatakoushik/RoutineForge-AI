package com.routineforge.app.ui.screens.home

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.routineforge.app.ui.components.PrimaryButton
import com.routineforge.app.ui.components.RoutineCard
import com.routineforge.app.ui.components.RoutineForgeCard
import com.routineforge.app.ui.components.SecondaryButton
import com.routineforge.app.ui.components.SectionHeader

@Composable
fun HomeScreen(
    onNavigateToDiscover: () -> Unit,
    onNavigateToSavedRoutines: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onShowSnackbar: (String) -> Unit,
    viewModel: HomeViewModel = viewModel()
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

        // TOP GREETING AREA
        TopGreetingSection(
            greeting = uiState.greetingMessage,
            subtitle = uiState.subtitleMessage,
            isEngineReady = uiState.isEngineReady
        )

        Spacer(modifier = Modifier.height(20.dp))

        // HERO CARD: RoutineForge AI
        HeroActionCard(
            appName = uiState.appName,
            tagline = uiState.tagline,
            onDiscoverClick = onNavigateToDiscover,
            onViewRoutinesClick = onNavigateToSavedRoutines
        )

        Spacer(modifier = Modifier.height(24.dp))

        // PRODUCTIVITY SUMMARY SECTION
        SectionHeader(
            title = "Productivity Summary",
            subtitle = "Overview of time savings and routine activity"
        )
        ProductivitySummaryCards(
            stats = uiState.stats,
            onAnalyticsClick = onNavigateToAnalytics
        )

        Spacer(modifier = Modifier.height(24.dp))

        // RECENT ROUTINES SECTION
        SectionHeader(
            title = "Recent routines",
            subtitle = "Active automated workflows",
            actionText = "View All",
            onActionClick = onNavigateToSavedRoutines
        )

        uiState.recentRoutines.forEach { routine ->
            RoutineCard(
                routine = routine,
                onRunClick = { routineName ->
                    onShowSnackbar("Routine execution for '$routineName' will be connected in the automation stage.")
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun TopGreetingSection(
    greeting: String,
    subtitle: String,
    isEngineReady: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = greeting,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.clip(RoundedCornerShape(20.dp))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            if (isEngineReady) MaterialTheme.colorScheme.secondary
                            else MaterialTheme.colorScheme.outline
                        )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isEngineReady) "AI Ready" else "Offline",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun HeroActionCard(
    appName: String,
    tagline: String,
    onDiscoverClick: () -> Unit,
    onViewRoutinesClick: () -> Unit
) {
    RoutineForgeCard(
        borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = appName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "\"$tagline\"",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PrimaryButton(
                text = "Discover a Routine",
                onClick = onDiscoverClick,
                icon = Icons.Default.PlayArrow,
                modifier = Modifier.weight(1f)
            )
            SecondaryButton(
                text = "View My Routines",
                onClick = onViewRoutinesClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ProductivitySummaryCards(
    stats: com.routineforge.app.data.model.ProductivityStats,
    onAnalyticsClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Card 1: Routines
        RoutineForgeCard(
            modifier = Modifier.weight(1f),
            onClick = onAnalyticsClick
        ) {
            Icon(
                imageVector = Icons.Default.Widgets,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${stats.totalRoutines}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Routines",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Card 2: Time Saved
        RoutineForgeCard(
            modifier = Modifier.weight(1f),
            onClick = onAnalyticsClick
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stats.timeSavedFormatted,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Time Saved",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Card 3: Executions
        RoutineForgeCard(
            modifier = Modifier.weight(1f),
            onClick = onAnalyticsClick
        ) {
            Icon(
                imageVector = Icons.Default.Bolt,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${stats.totalExecutions}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Executions",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
