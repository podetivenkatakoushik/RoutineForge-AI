package com.routineforge.app.domain.repository

import com.routineforge.app.data.model.ProductivityStats
import com.routineforge.app.domain.model.Routine
import com.routineforge.app.domain.model.RoutineStatus
import kotlinx.coroutines.flow.Flow

interface RoutineRepository {
    fun getAllRoutines(): Flow<List<Routine>>
    fun getRoutinesByStatus(status: RoutineStatus): Flow<List<Routine>>
    fun getRoutineById(id: String): Flow<Routine?>
    suspend fun saveRoutine(routine: Routine): Boolean
    suspend fun updateRoutine(routine: Routine): Boolean
    suspend fun deleteRoutine(id: String): Boolean
    fun getProductivityStats(): Flow<ProductivityStats>
}
