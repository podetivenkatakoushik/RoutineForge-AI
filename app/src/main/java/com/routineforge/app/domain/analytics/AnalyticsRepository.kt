package com.routineforge.app.domain.analytics

import com.routineforge.app.data.repository.InMemoryRoutineRepository
import com.routineforge.app.domain.execution.ExecutionRepository
import com.routineforge.app.domain.execution.InMemoryExecutionRepository
import com.routineforge.app.domain.model.ExecutionResult
import com.routineforge.app.domain.model.ExecutionStatus
import com.routineforge.app.domain.repository.RoutineRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface AnalyticsRepository {
    val summary: StateFlow<AnalyticsSummary>
    val highestImpactRoutines: StateFlow<List<RoutineImpactItem>>
    val executionHistory: StateFlow<List<ExecutionResult>>

    suspend fun recordExecution(result: ExecutionResult)
    suspend fun refreshAnalytics()
}

class InMemoryAnalyticsRepository(
    private val executionRepo: ExecutionRepository = InMemoryExecutionRepository.instance,
    private val routineRepo: RoutineRepository = InMemoryRoutineRepository.instance
) : AnalyticsRepository {

    private val _summary = MutableStateFlow(AnalyticsSummary())
    override val summary: StateFlow<AnalyticsSummary> = _summary.asStateFlow()

    private val _highestImpactRoutines = MutableStateFlow<List<RoutineImpactItem>>(
        listOf(
            RoutineImpactItem("r1", "Process Spreadsheet", "50 min saved this week", 32),
            RoutineImpactItem("r2", "Organize Reports", "36 min saved this week", 18),
            RoutineImpactItem("r3", "Invoice Auto-Archiver", "25 min saved this week", 12)
        )
    )
    override val highestImpactRoutines: StateFlow<List<RoutineImpactItem>> = _highestImpactRoutines.asStateFlow()

    override val executionHistory: StateFlow<List<ExecutionResult>> = executionRepo.executionHistory

    override suspend fun recordExecution(result: ExecutionResult) {
        executionRepo.saveExecutionResult(result)
        refreshAnalytics()
    }

    override suspend fun refreshAnalytics() {
        val history = executionRepo.executionHistory.value
        val completed = history.filter { it.status == ExecutionStatus.COMPLETED }
        val failed = history.filter { it.status == ExecutionStatus.FAILED }

        val additionalTimeSaved = completed.sumOf { it.timeSavedMs }
        val additionalRuns = history.size
        val additionalCompleted = completed.size

        val baseSummary = AnalyticsSummary()
        val updatedTimeSaved = baseSummary.totalTimeSavedMs + additionalTimeSaved
        val updatedTotalExecutions = baseSummary.totalExecutions + additionalRuns
        val updatedCompletedExecutions = baseSummary.completedExecutions + additionalCompleted

        val totalFinished = updatedTotalExecutions
        val successRate = if (totalFinished > 0) (updatedCompletedExecutions * 100 / totalFinished).coerceIn(0, 100) else 100

        val avgDuration = if (completed.isNotEmpty()) {
            completed.map { it.durationMs }.average().toLong()
        } else baseSummary.avgExecutionDurationMs

        _summary.value = baseSummary.copy(
            totalExecutions = updatedTotalExecutions,
            completedExecutions = updatedCompletedExecutions,
            failedExecutions = baseSummary.failedExecutions + failed.size,
            totalTimeSavedMs = updatedTimeSaved,
            avgExecutionDurationMs = avgDuration,
            successRatePercentage = successRate
        )

        // Update highest impact routines dynamically
        if (completed.isNotEmpty()) {
            val groupedByRoutine = completed.groupBy { it.routineName.ifBlank { "Organize Assignment Documents" } }
            val dynamicImpacts = groupedByRoutine.map { (name, execs) ->
                val timeSaved = execs.sumOf { it.timeSavedMs }
                RoutineImpactItem(
                    routineId = execs.first().routineId,
                    routineName = name,
                    totalTimeSavedFormatted = "${TimeFormatter.formatMs(timeSaved)} saved",
                    executionCount = execs.size
                )
            }.sortedByDescending { it.executionCount }

            val currentImpacts = _highestImpactRoutines.value.toMutableList()
            dynamicImpacts.forEach { dynamicItem ->
                val idx = currentImpacts.indexOfFirst { it.routineName == dynamicItem.routineName }
                if (idx >= 0) {
                    currentImpacts[idx] = dynamicItem
                } else {
                    currentImpacts.add(0, dynamicItem)
                }
            }
            _highestImpactRoutines.value = currentImpacts
        }
    }

    companion object {
        val instance: AnalyticsRepository by lazy { InMemoryAnalyticsRepository() }
    }
}
