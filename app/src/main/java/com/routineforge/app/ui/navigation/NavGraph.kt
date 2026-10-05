package com.routineforge.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.routineforge.app.domain.execution.InMemoryExecutionStore
import com.routineforge.app.ui.screens.approval.RoutineApprovalScreen
import com.routineforge.app.ui.screens.discover.DiscoverScreen
import com.routineforge.app.ui.screens.editor.RoutineEditorScreen
import com.routineforge.app.ui.screens.execution.ExecutionPreviewScreen
import com.routineforge.app.ui.screens.execution.ExecutionResultScreen
import com.routineforge.app.ui.screens.execution.RoutineExecutionScreen
import com.routineforge.app.ui.screens.generated.GeneratedRoutineScreen
import com.routineforge.app.ui.screens.home.HomeScreen
import com.routineforge.app.ui.screens.insights.InsightsScreen
import com.routineforge.app.ui.screens.recording.RecordingWorkspaceScreen
import com.routineforge.app.ui.screens.review.WorkflowReviewScreen
import com.routineforge.app.ui.screens.routines.RoutinesScreen
import com.routineforge.app.ui.screens.settings.SettingsScreen
import kotlinx.coroutines.launch

@Composable
fun RoutineForgeNavGraph(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val showSnackbar: (String) -> Unit = { message ->
        coroutineScope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(message)
        }
    }

    val isFullscreenRoute = currentRoute == Screen.RecordingWorkspace.route ||
            currentRoute == Screen.WorkflowReview.route ||
            currentRoute == Screen.GeneratedRoutine.route ||
            currentRoute == Screen.RoutineEditor.route ||
            currentRoute == Screen.RoutineApproval.route ||
            currentRoute == Screen.ExecutionPreview.route ||
            currentRoute == Screen.RoutineExecution.route ||
            currentRoute == Screen.ExecutionResult.route

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            if (!isFullscreenRoute) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    bottomNavScreens.forEach { screen ->
                        val isSelected = currentRoute == screen.route
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = screen.title
                                )
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            selected = isSelected,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToDiscover = { navController.navigate(Screen.Discover.route) },
                    onNavigateToSavedRoutines = { navController.navigate(Screen.Routines.route) },
                    onNavigateToAnalytics = { navController.navigate(Screen.Insights.route) },
                    onShowSnackbar = showSnackbar
                )
            }
            composable(Screen.Discover.route) {
                DiscoverScreen(
                    onNavigateToRecording = { navController.navigate(Screen.RecordingWorkspace.route) },
                    onShowSnackbar = showSnackbar
                )
            }
            composable(Screen.RecordingWorkspace.route) {
                RecordingWorkspaceScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onRecordingCompleted = { navController.navigate(Screen.WorkflowReview.route) },
                    onShowSnackbar = showSnackbar
                )
            }
            composable(Screen.WorkflowReview.route) {
                WorkflowReviewScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToGeneratedRoutine = { navController.navigate(Screen.GeneratedRoutine.route) },
                    onSaveSuccess = {
                        navController.popBackStack(Screen.Discover.route, inclusive = false)
                    },
                    onShowSnackbar = showSnackbar
                )
            }
            composable(Screen.GeneratedRoutine.route) {
                GeneratedRoutineScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEdit = { navController.navigate(Screen.RoutineEditor.route) },
                    onNavigateToApproval = { navController.navigate(Screen.RoutineApproval.route) },
                    onSaveSuccess = {
                        navController.navigate(Screen.Routines.route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                        }
                    },
                    onShowSnackbar = showSnackbar
                )
            }
            composable(Screen.RoutineEditor.route) {
                RoutineEditorScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onSaveSuccess = { navController.navigate(Screen.RoutineApproval.route) },
                    onShowSnackbar = showSnackbar
                )
            }
            composable(Screen.RoutineApproval.route) {
                RoutineApprovalScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEdit = { navController.navigate(Screen.RoutineEditor.route) },
                    onSaveSuccess = {
                        navController.navigate(Screen.Routines.route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                        }
                    },
                    onShowSnackbar = showSnackbar
                )
            }
            composable(Screen.ExecutionPreview.route) {
                val selectedRoutine by InMemoryExecutionStore.instance.selectedRoutine.collectAsState()
                ExecutionPreviewScreen(
                    routine = selectedRoutine,
                    onNavigateBack = { navController.popBackStack() },
                    onStartSimulation = { navController.navigate(Screen.RoutineExecution.route) },
                    onShowSnackbar = showSnackbar
                )
            }
            composable(Screen.RoutineExecution.route) {
                RoutineExecutionScreen(
                    onExecutionFinished = {
                        navController.navigate(Screen.ExecutionResult.route) {
                            popUpTo(Screen.ExecutionPreview.route) { inclusive = true }
                        }
                    },
                    onShowSnackbar = showSnackbar
                )
            }
            composable(Screen.ExecutionResult.route) {
                ExecutionResultScreen(
                    onDone = {
                        navController.navigate(Screen.Routines.route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(Screen.Routines.route) {
                RoutinesScreen(
                    onNavigateToDiscover = { navController.navigate(Screen.Discover.route) },
                    onNavigateToExecutionPreview = { navController.navigate(Screen.ExecutionPreview.route) },
                    onShowSnackbar = showSnackbar
                )
            }
            composable(Screen.Insights.route) {
                InsightsScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onShowSnackbar = showSnackbar
                )
            }
        }
    }
}
