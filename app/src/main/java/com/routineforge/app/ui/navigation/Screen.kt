package com.routineforge.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Discover : Screen("discover", "Discover", Icons.Default.AutoAwesome)
    object Routines : Screen("routines", "Routines", Icons.AutoMirrored.Filled.ListAlt)
    object Insights : Screen("insights", "Insights", Icons.Default.Analytics)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    object RecordingWorkspace : Screen("recording_workspace", "Record Workflow", Icons.Default.TouchApp)
    object WorkflowReview : Screen("workflow_review", "Review Workflow", Icons.Default.Check)
    object GeneratedRoutine : Screen("generated_routine", "Routine Forged", Icons.Default.AutoAwesome)
    object RoutineEditor : Screen("routine_editor", "Edit Routine", Icons.Default.Edit)
    object RoutineApproval : Screen("routine_approval", "Approve Routine", Icons.Default.Check)
    object ExecutionPreview : Screen("execution_preview", "Execution Preview", Icons.Default.PlayArrow)
    object RoutineExecution : Screen("routine_execution", "Simulating Routine", Icons.AutoMirrored.Filled.DirectionsRun)
    object ExecutionResult : Screen("execution_result", "Execution Report", Icons.Default.Check)
}

val bottomNavScreens = listOf(
    Screen.Home,
    Screen.Discover,
    Screen.Routines,
    Screen.Insights,
    Screen.Settings
)
