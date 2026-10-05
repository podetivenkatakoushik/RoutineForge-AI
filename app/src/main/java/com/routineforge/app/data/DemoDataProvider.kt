package com.routineforge.app.data

import com.routineforge.app.data.model.ProductivityStats
import com.routineforge.app.data.model.RoutineItem
import com.routineforge.app.data.model.WeeklyTimeSavedPoint
import com.routineforge.app.data.model.toRoutineItem
import com.routineforge.app.data.repository.InMemoryRoutineRepository
import com.routineforge.app.domain.repository.RoutineRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

object DemoDataProvider {

    val repository: RoutineRepository = InMemoryRoutineRepository()

    fun getDemoStats(): ProductivityStats {
        return runBlocking { repository.getProductivityStats().first() }
    }

    fun getSampleRoutines(): List<RoutineItem> {
        return runBlocking { repository.getAllRoutines().first() }.map { it.toRoutineItem() }
    }

    fun getWeeklyTimeSavedPoints(): List<WeeklyTimeSavedPoint> {
        return listOf(
            WeeklyTimeSavedPoint(dayLabel = "Mon", minutesSaved = 18),
            WeeklyTimeSavedPoint(dayLabel = "Tue", minutesSaved = 25),
            WeeklyTimeSavedPoint(dayLabel = "Wed", minutesSaved = 45, isPeakDay = true),
            WeeklyTimeSavedPoint(dayLabel = "Thu", minutesSaved = 30),
            WeeklyTimeSavedPoint(dayLabel = "Fri", minutesSaved = 22),
            WeeklyTimeSavedPoint(dayLabel = "Sat", minutesSaved = 10),
            WeeklyTimeSavedPoint(dayLabel = "Sun", minutesSaved = 5)
        )
    }
}
