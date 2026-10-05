package com.routineforge.app.domain.execution

import com.routineforge.app.domain.model.Routine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface ExecutionStore {
    val selectedRoutine: StateFlow<Routine?>
    fun setSelectedRoutine(routine: Routine)
    fun clear()
}

class InMemoryExecutionStore : ExecutionStore {
    private val _selectedRoutine = MutableStateFlow<Routine?>(null)
    override val selectedRoutine: StateFlow<Routine?> = _selectedRoutine.asStateFlow()

    override fun setSelectedRoutine(routine: Routine) {
        _selectedRoutine.value = routine
    }

    override fun clear() {
        _selectedRoutine.value = null
    }

    companion object {
        val instance: ExecutionStore by lazy { InMemoryExecutionStore() }
    }
}
