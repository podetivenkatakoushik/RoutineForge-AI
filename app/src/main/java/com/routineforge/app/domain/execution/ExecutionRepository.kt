package com.routineforge.app.domain.execution

import com.routineforge.app.domain.model.ExecutionResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface ExecutionRepository {
    val executionHistory: StateFlow<List<ExecutionResult>>
    suspend fun saveExecutionResult(result: ExecutionResult)
    fun getExecutionById(executionId: String): ExecutionResult?
    suspend fun clear()
}

class InMemoryExecutionRepository : ExecutionRepository {
    private val _executionHistory = MutableStateFlow<List<ExecutionResult>>(emptyList())
    override val executionHistory: StateFlow<List<ExecutionResult>> = _executionHistory.asStateFlow()

    override suspend fun saveExecutionResult(result: ExecutionResult) {
        val current = _executionHistory.value.toMutableList()
        current.add(0, result) // Newest first
        _executionHistory.value = current
    }

    override fun getExecutionById(executionId: String): ExecutionResult? {
        return _executionHistory.value.find { it.executionId == executionId }
    }

    override suspend fun clear() {
        _executionHistory.value = emptyList()
    }

    companion object {
        val instance: ExecutionRepository by lazy { InMemoryExecutionRepository() }
    }
}
