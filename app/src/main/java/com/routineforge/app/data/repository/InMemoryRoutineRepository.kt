package com.routineforge.app.data.repository

import com.routineforge.app.data.model.ProductivityStats
import com.routineforge.app.domain.model.ActionType
import com.routineforge.app.domain.model.ParameterType
import com.routineforge.app.domain.model.RiskLevel
import com.routineforge.app.domain.model.Routine
import com.routineforge.app.domain.model.RoutineStatus
import com.routineforge.app.domain.model.RoutineStep
import com.routineforge.app.domain.model.StepParameter
import com.routineforge.app.domain.model.TimeSavedMetrics
import com.routineforge.app.domain.model.Trigger
import com.routineforge.app.domain.repository.RoutineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Thread-safe in-memory implementation of RoutineRepository initialized with sample routines.
 */
class InMemoryRoutineRepository : RoutineRepository {

    private val routinesState = MutableStateFlow<List<Routine>>(createSampleRoutines())

    override fun getAllRoutines(): Flow<List<Routine>> = routinesState

    override fun getRoutinesByStatus(status: RoutineStatus): Flow<List<Routine>> {
        return routinesState.map { list -> list.filter { it.status == status } }
    }

    override fun getRoutineById(id: String): Flow<Routine?> {
        return routinesState.map { list -> list.find { it.id == id } }
    }

    override suspend fun saveRoutine(routine: Routine): Boolean {
        val current = routinesState.value.toMutableList()
        val index = current.indexOfFirst { it.id == routine.id }
        if (index >= 0) {
            current[index] = routine.copy(updatedAt = System.currentTimeMillis())
        } else {
            current.add(routine)
        }
        routinesState.value = current
        return true
    }

    override suspend fun updateRoutine(routine: Routine): Boolean {
        return saveRoutine(routine)
    }

    override suspend fun deleteRoutine(id: String): Boolean {
        val current = routinesState.value.toMutableList()
        val removed = current.removeIf { it.id == id }
        if (removed) {
            routinesState.value = current
        }
        return removed
    }

    override fun getProductivityStats(): Flow<ProductivityStats> {
        return routinesState.map { list ->
            val totalRoutines = list.size
            val totalExecutions = list.sumOf { it.executionCount }
            val totalMinutesSaved = list.sumOf { (it.timeSavedMetrics.timeSavedMs / (1000 * 60)) * it.executionCount }
            val hours = totalMinutesSaved / 60
            val mins = totalMinutesSaved % 60
            val formattedSaved = if (hours > 0) "${hours}h ${mins}m" else "${mins}m"

            ProductivityStats(
                totalRoutines = totalRoutines,
                totalExecutions = totalExecutions,
                timeSavedFormatted = formattedSaved,
                avgRoutineTimeFormatted = "3m 42s"
            )
        }
    }

    companion object {
        private fun createSampleRoutines(): List<Routine> {
            val now = System.currentTimeMillis()
            val hourMs = 3600 * 1000L

            // 1. Organize Reports
            val organizeReports = Routine(
                id = "r1",
                name = "Organize Reports",
                description = "Extract, rename, and organize incoming PDF and document reports.",
                category = "Recent",
                status = RoutineStatus.ACTIVE,
                trigger = Trigger.Manual("Manual Trigger"),
                executionCount = 18,
                createdAt = now - 7 * 24 * hourMs,
                updatedAt = now - hourMs,
                lastExecutedAt = now - 2 * hourMs,
                timeSavedMetrics = TimeSavedMetrics(
                    manualEstimatedDurationMs = 15 * 60 * 1000L,
                    automatedDurationMs = 3 * 60 * 1000L
                ),
                steps = listOf(
                    RoutineStep(
                        id = "s1_1",
                        order = 1,
                        actionType = ActionType.SELECT_FILE,
                        title = "Select Incoming Report File",
                        description = "Prompt user to select unorganized PDF report file",
                        parameters = listOf(
                            StepParameter("folderPath", "/Downloads/Reports", "Source Folder", ParameterType.FOLDER_PATH),
                            StepParameter("extension", "pdf", "File Extension", ParameterType.TEXT)
                        )
                    ),
                    RoutineStep(
                        id = "s1_2",
                        order = 2,
                        actionType = ActionType.EXTRACT_TEXT,
                        title = "Extract Report Header & Date",
                        description = "Parse header fields and date stamp from document text",
                        parameters = listOf(
                            StepParameter("targetField", "HeaderDate", "Target Field", ParameterType.TEXT)
                        )
                    ),
                    RoutineStep(
                        id = "s1_3",
                        order = 3,
                        actionType = ActionType.RENAME_FILE,
                        title = "Rename to Standardized Format",
                        description = "Apply naming pattern: [YYYY-MM-DD]_[ReportName].pdf",
                        parameters = listOf(
                            StepParameter("namingPattern", "{Date}_{Title}.pdf", "Pattern", ParameterType.TEXT)
                        )
                    ),
                    RoutineStep(
                        id = "s1_4",
                        order = 4,
                        actionType = ActionType.MOVE_FILE,
                        title = "Move to Organized Folder",
                        description = "Relocate renamed report to /Documents/Reports/Organized",
                        parameters = listOf(
                            StepParameter("destination", "/Documents/Reports/Organized", "Destination", ParameterType.FOLDER_PATH)
                        )
                    )
                )
            )

            // 2. Process Spreadsheet
            val processSpreadsheet = Routine(
                id = "r2",
                name = "Process Spreadsheet",
                description = "Transform repetitive spreadsheet entries into a reusable structured format.",
                category = "Most Used",
                status = RoutineStatus.ACTIVE,
                trigger = Trigger.FileSelected("/Downloads", "*.xlsx"),
                executionCount = 32,
                createdAt = now - 14 * 24 * hourMs,
                updatedAt = now - 24 * hourMs,
                lastExecutedAt = now - 18 * hourMs,
                timeSavedMetrics = TimeSavedMetrics(
                    manualEstimatedDurationMs = 30 * 60 * 1000L,
                    automatedDurationMs = 5 * 60 * 1000L
                ),
                steps = listOf(
                    RoutineStep(
                        id = "s2_1",
                        order = 1,
                        actionType = ActionType.SELECT_FILE,
                        title = "Select Target Spreadsheet",
                        description = "Locate incoming excel file in downloads directory",
                        parameters = listOf(
                            StepParameter("filePath", "/Downloads/data.xlsx", "File Path", ParameterType.FILE_PATH)
                        )
                    ),
                    RoutineStep(
                        id = "s2_2",
                        order = 2,
                        actionType = ActionType.EXTRACT_DATA,
                        title = "Extract Columns & Entry Values",
                        description = "Parse table data into structured json memory array",
                        parameters = listOf(
                            StepParameter("columns", "Date, Amount, Category", "Columns", ParameterType.TEXT)
                        )
                    ),
                    RoutineStep(
                        id = "s2_3",
                        order = 3,
                        actionType = ActionType.TRANSFORM_DATA,
                        title = "Calculate Summary Totals",
                        description = "Aggregate column totals and strip duplicate row entries",
                        parameters = listOf(
                            StepParameter("operation", "SUM(Amount) GROUP BY Category", "Transformation", ParameterType.TEXT)
                        )
                    ),
                    RoutineStep(
                        id = "s2_4",
                        order = 4,
                        actionType = ActionType.CREATE_FILE,
                        title = "Save Output Summary CSV",
                        description = "Export transformed results to /Documents/Summaries/Summary.csv",
                        parameters = listOf(
                            StepParameter("destination", "/Documents/Summaries/Summary.csv", "Destination File", ParameterType.FILE_PATH)
                        )
                    )
                )
            )

            // 3. URL Investigation
            val urlInvestigation = Routine(
                id = "r3",
                name = "URL Investigation",
                description = "Run approved cybersecurity analysis steps and prepare a structured investigation result.",
                category = "Recent",
                status = RoutineStatus.ACTIVE,
                trigger = Trigger.Manual("Manual Launch"),
                executionCount = 11,
                createdAt = now - 3 * 24 * hourMs,
                updatedAt = now - 12 * hourMs,
                lastExecutedAt = now - 48 * hourMs,
                timeSavedMetrics = TimeSavedMetrics(
                    manualEstimatedDurationMs = 10 * 60 * 1000L,
                    automatedDurationMs = 2 * 60 * 1000L
                ),
                steps = listOf(
                    RoutineStep(
                        id = "s3_1",
                        order = 1,
                        actionType = ActionType.USER_INPUT,
                        title = "Provide Target URL",
                        description = "Prompt user for target URL string to investigate",
                        parameters = listOf(
                            StepParameter("url", "https://example.com", "Target URL", ParameterType.URL)
                        )
                    ),
                    RoutineStep(
                        id = "s3_2",
                        order = 2,
                        actionType = ActionType.EXTRACT_DATA,
                        title = "Extract Domain, Protocol, & Headers",
                        description = "Parse URL domain name and query parameters safely",
                        parameters = listOf(
                            StepParameter("parseMode", "DOMAIN_PLUS_HEADERS", "Parse Mode", ParameterType.TEXT)
                        )
                    ),
                    RoutineStep(
                        id = "s3_3",
                        order = 3,
                        actionType = ActionType.TRANSFORM_DATA,
                        title = "Analyze Reputation Score",
                        description = "Evaluate domain age, SSL status, and threat indicators",
                        parameters = listOf(
                            StepParameter("analysisProfile", "STRICT_CHECK", "Profile", ParameterType.TEXT)
                        )
                    ),
                    RoutineStep(
                        id = "s3_4",
                        order = 4,
                        actionType = ActionType.SHOW_RESULT,
                        title = "Present Structured Safety Report",
                        description = "Render structured URL analysis result on screen",
                        parameters = listOf(
                            StepParameter("displayType", "SUMMARY_CARD", "Display Format", ParameterType.TEXT)
                        )
                    )
                )
            )

            // 4. Daily Briefing Summarizer
            val dailyBriefing = Routine(
                id = "r4",
                name = "Daily Briefing Summarizer",
                description = "Summarize morning notifications, key emails, and calendar events into one quick briefing.",
                category = "Most Used",
                status = RoutineStatus.ACTIVE,
                trigger = Trigger.Scheduled("08:00 AM"),
                executionCount = 22,
                createdAt = now - 20 * 24 * hourMs,
                updatedAt = now - 2 * hourMs,
                lastExecutedAt = now - 4 * hourMs,
                timeSavedMetrics = TimeSavedMetrics(
                    manualEstimatedDurationMs = 20 * 60 * 1000L,
                    automatedDurationMs = 5 * 60 * 1000L
                ),
                steps = listOf(
                    RoutineStep(
                        id = "s4_1",
                        order = 1,
                        actionType = ActionType.EXTRACT_DATA,
                        title = "Collect Pending Briefing Notes",
                        description = "Gather morning calendar titles and notification subjects",
                        parameters = listOf(
                            StepParameter("source", "CALENDAR_AND_NOTIFICATIONS", "Source", ParameterType.TEXT)
                        )
                    ),
                    RoutineStep(
                        id = "s4_2",
                        order = 2,
                        actionType = ActionType.TRANSFORM_DATA,
                        title = "Synthesize Priority Summaries",
                        description = "Sort events chronologically and highlight top 3 priorities",
                        parameters = listOf(
                            StepParameter("priorityCount", "3", "Priority Count", ParameterType.NUMBER)
                        )
                    ),
                    RoutineStep(
                        id = "s4_3",
                        order = 3,
                        actionType = ActionType.SHOW_RESULT,
                        title = "Display Morning Digest",
                        description = "Display formatted morning briefing card",
                        parameters = listOf(
                            StepParameter("view", "MORNING_DIGEST", "View Type", ParameterType.TEXT)
                        )
                    )
                )
            )

            // 5. Invoice Auto-Archiver
            val invoiceArchiver = Routine(
                id = "r5",
                name = "Invoice Auto-Archiver",
                description = "Detect incoming receipts, extract totals, and file them into the designated expense folder.",
                category = "General",
                status = RoutineStatus.ACTIVE,
                trigger = Trigger.FileSelected("/Downloads", "*.pdf"),
                executionCount = 4,
                createdAt = now - 5 * 24 * hourMs,
                updatedAt = now - 12 * hourMs,
                lastExecutedAt = now - 72 * hourMs,
                timeSavedMetrics = TimeSavedMetrics(
                    manualEstimatedDurationMs = 25 * 60 * 1000L,
                    automatedDurationMs = 5 * 60 * 1000L
                ),
                steps = listOf(
                    RoutineStep(
                        id = "s5_1",
                        order = 1,
                        actionType = ActionType.SELECT_FILE,
                        title = "Select Receipt / Invoice File",
                        description = "Detect newly downloaded invoice PDF",
                        parameters = listOf(
                            StepParameter("folderPath", "/Downloads", "Folder", ParameterType.FOLDER_PATH)
                        )
                    ),
                    RoutineStep(
                        id = "s5_2",
                        order = 2,
                        actionType = ActionType.EXTRACT_TEXT,
                        title = "Parse Vendor & Total Amount",
                        description = "Extract total amount and vendor name from document text",
                        parameters = listOf(
                            StepParameter("patterns", "Total, Vendor", "Extraction Keys", ParameterType.TEXT)
                        )
                    ),
                    RoutineStep(
                        id = "s5_3",
                        order = 3,
                        actionType = ActionType.MOVE_FILE,
                        title = "Archive to Expenses Directory",
                        description = "Move parsed invoice file to /Documents/Expenses/Invoices",
                        parameters = listOf(
                            StepParameter("destination", "/Documents/Expenses/Invoices", "Destination", ParameterType.FOLDER_PATH)
                        ),
                        riskLevel = RiskLevel.HIGH
                    )
                )
            )

            return listOf(organizeReports, processSpreadsheet, urlInvestigation, dailyBriefing, invoiceArchiver)
        }

        val instance: InMemoryRoutineRepository by lazy { InMemoryRoutineRepository() }
    }
}
