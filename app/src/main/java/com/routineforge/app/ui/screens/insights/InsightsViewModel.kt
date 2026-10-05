package com.routineforge.app.ui.screens.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.routineforge.app.data.model.ProductivityStats
import com.routineforge.app.data.model.WeeklyTimeSavedPoint
import com.routineforge.app.data.repository.InMemoryRoutineRepository
import com.routineforge.app.domain.analytics.AnalyticsRepository
import com.routineforge.app.domain.analytics.InMemoryAnalyticsRepository
import com.routineforge.app.domain.analytics.RoutineImpactItem
import com.routineforge.app.domain.repository.RoutineRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

data class InsightsUiState(
    val stats: ProductivityStats = ProductivityStats(),
    val weeklyChartPoints: List<WeeklyTimeSavedPoint> = emptyList(),
    val highestImpactRoutines: List<RoutineImpactItem> = emptyList()
)

class InsightsViewModel(
    private val analyticsRepo: AnalyticsRepository = InMemoryAnalyticsRepository.instance,
    private val routineRepo: RoutineRepository = InMemoryRoutineRepository.instance
) : ViewModel() {

    private val _uiState = MutableStateFlow(InsightsUiState())
    val uiState: StateFlow<InsightsUiState> = _uiState.asStateFlow()

    init {
        routineRepo.getProductivityStats()
            .onEach { stats ->
                _uiState.value = _uiState.value.copy(stats = stats)
            }
            .launchIn(viewModelScope)

        analyticsRepo.summary
            .onEach { summary ->
                _uiState.value = _uiState.value.copy(weeklyChartPoints = summary.weeklyChartPoints)
            }
            .launchIn(viewModelScope)

        analyticsRepo.highestImpactRoutines
            .onEach { impacts ->
                _uiState.value = _uiState.value.copy(highestImpactRoutines = impacts)
            }
            .launchIn(viewModelScope)
    }
}
