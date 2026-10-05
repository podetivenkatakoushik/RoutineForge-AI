package com.routineforge.app.ui.screens.routines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.routineforge.app.data.model.RoutineItem
import com.routineforge.app.data.model.toRoutineItem
import com.routineforge.app.data.repository.InMemoryRoutineRepository
import com.routineforge.app.domain.execution.ExecutionStore
import com.routineforge.app.domain.execution.InMemoryExecutionStore
import com.routineforge.app.domain.repository.RoutineRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class RoutinesUiState(
    val searchQuery: String = "",
    val selectedFilter: String = "All",
    val availableFilters: List<String> = listOf("All", "Recent", "Most Used"),
    val allRoutines: List<RoutineItem> = emptyList()
) {
    val filteredRoutines: List<RoutineItem>
        get() {
            var result = allRoutines
            if (selectedFilter == "Recent") {
                result = result.filter { it.category == "Recent" }
            } else if (selectedFilter == "Most Used") {
                result = result.filter { it.category == "Most Used" }
            }
            if (searchQuery.isNotBlank()) {
                result = result.filter {
                    it.name.contains(searchQuery, ignoreCase = true) ||
                            it.description.contains(searchQuery, ignoreCase = true)
                }
            }
            return result
        }
}

class RoutinesViewModel(
    private val repository: RoutineRepository = InMemoryRoutineRepository.instance,
    private val executionStore: ExecutionStore = InMemoryExecutionStore.instance
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoutinesUiState())
    val uiState: StateFlow<RoutinesUiState> = _uiState.asStateFlow()

    init {
        repository.getAllRoutines()
            .onEach { routines ->
                _uiState.value = _uiState.value.copy(
                    allRoutines = routines.map { it.toRoutineItem() }
                )
            }
            .launchIn(viewModelScope)
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun selectFilter(filter: String) {
        _uiState.value = _uiState.value.copy(selectedFilter = filter)
    }

    fun selectRoutineForExecution(routineId: String, onSelected: () -> Unit) {
        viewModelScope.launch {
            repository.getRoutineById(routineId).first()?.let { routine ->
                executionStore.setSelectedRoutine(routine)
                onSelected()
            }
        }
    }
}
