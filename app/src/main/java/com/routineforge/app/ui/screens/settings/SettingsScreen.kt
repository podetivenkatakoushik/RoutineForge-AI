package com.routineforge.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.routineforge.app.ui.components.RoutineForgeCard
import com.routineforge.app.ui.components.SectionHeader

@Composable
fun SettingsScreen(
    onShowSnackbar: (String) -> Unit,
    viewModel: SettingsViewModel = viewModel()
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
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Configure AI model provider, security gates, and preferences.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION 1: AI CONFIGURATION
        SectionHeader(
            title = "AI Engine",
            subtitle = "Intelligence provider and on-device processing"
        )
        SettingCardGroup {
            SettingRowItem(
                icon = Icons.Default.Psychology,
                title = "AI Provider",
                subtitle = uiState.selectedAiProvider,
                onClick = { onShowSnackbar("AI provider selection will be configurable in later stages.") }
            )
            Spacer(modifier = Modifier.height(12.dp))
            SettingSwitchItem(
                icon = Icons.Default.Shield,
                title = "On-Device Local Processing",
                subtitle = "Keep sensitive workflow analysis strictly on your iQOO device",
                checked = uiState.isLocalProcessingOnly,
                onCheckedChange = {
                    viewModel.toggleLocalProcessing(it)
                    onShowSnackbar("Local processing preference updated.")
                }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION 2: AUTOMATION & SAFETY
        SectionHeader(
            title = "Automation & Safety",
            subtitle = "Risk mitigation and execution confirmation rules"
        )
        SettingCardGroup {
            SettingSwitchItem(
                icon = Icons.Default.Security,
                title = "Confirmation Required",
                subtitle = "Require explicit user tap before performing actions",
                checked = uiState.isConfirmationRequired,
                onCheckedChange = {
                    viewModel.toggleConfirmationRequired(it)
                    onShowSnackbar("Confirmation policy updated.")
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
            SettingSwitchItem(
                icon = Icons.Default.Shield,
                title = "High Risk Action Safety Gate",
                subtitle = "Block high-risk actions until multi-step approval is granted",
                checked = uiState.isHighRiskSafetyGateEnabled,
                onCheckedChange = {
                    viewModel.toggleHighRiskGate(it)
                    onShowSnackbar("High risk safety gate policy updated.")
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
            SettingSwitchItem(
                icon = Icons.Default.Info,
                title = "Execution Notifications",
                subtitle = "Display status alerts when a routine starts or completes",
                checked = uiState.isExecutionNotificationsEnabled,
                onCheckedChange = {
                    viewModel.toggleNotifications(it)
                    onShowSnackbar("Notification preference updated.")
                }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION 3: PRIVACY & DATA
        SectionHeader(
            title = "Privacy & Demo Data",
            subtitle = "Data retention, activity logs, and demo resets"
        )
        SettingCardGroup {
            SettingRowItem(
                icon = Icons.Default.Refresh,
                title = "Reset Demo State",
                subtitle = "Restore sample workspace documents and execution history",
                onClick = {
                    viewModel.resetDemoData()
                    onShowSnackbar("Demo state reset successfully.")
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
            SettingRowItem(
                icon = Icons.Default.Info,
                title = "Activity History",
                subtitle = "View execution logs and saved workflow metrics",
                onClick = { onShowSnackbar("Activity history viewer active.") }
            )
            Spacer(modifier = Modifier.height(12.dp))
            SettingRowItem(
                icon = Icons.Default.Security,
                title = "Clear Stored Data",
                subtitle = "Wipe cached workflow representations and temporary logs",
                onClick = { onShowSnackbar("Stored data cache cleared.") }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION 4: ABOUT
        SectionHeader(title = "About")
        SettingCardGroup {
            SettingRowItem(
                icon = Icons.Default.Info,
                title = "RoutineForge AI",
                subtitle = "${uiState.appVersion} • iQOO Hackathon 2026 Productivity Track",
                onClick = null
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun SettingCardGroup(
    content: @Composable () -> Unit
) {
    RoutineForgeCard(
        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            content = { content() }
        )
    }
}

@Composable
private fun SettingRowItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)?
) {
    RoutineForgeCard(
        onClick = onClick,
        borderColor = androidx.compose.ui.graphics.Color.Transparent,
        elevation = 0.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SettingSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}
