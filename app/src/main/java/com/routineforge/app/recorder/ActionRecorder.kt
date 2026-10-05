package com.routineforge.app.recorder

import com.routineforge.app.recorder.model.RecordedAction
import com.routineforge.app.recorder.model.RecordingSession
import com.routineforge.app.recorder.model.RecordingState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

interface ActionRecorder {
    val recordingState: StateFlow<RecordingState>
    val elapsedTimeMs: StateFlow<Long>
    val currentActions: StateFlow<List<RecordedAction>>

    fun startRecording()
    fun pauseRecording()
    fun resumeRecording()
    fun stopRecording(): RecordingSession
    fun recordAction(action: RecordedAction)
    fun updateAction(updatedAction: RecordedAction)
    fun deleteAction(actionId: String)
    fun reorderActions(fromIndex: Int, toIndex: Int)
    fun clear()
}

class DefaultActionRecorder(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) : ActionRecorder {

    private val _recordingState = MutableStateFlow(RecordingState.IDLE)
    override val recordingState: StateFlow<RecordingState> = _recordingState.asStateFlow()

    private val _elapsedTimeMs = MutableStateFlow(0L)
    override val elapsedTimeMs: StateFlow<Long> = _elapsedTimeMs.asStateFlow()

    private val _currentActions = MutableStateFlow<List<RecordedAction>>(emptyList())
    override val currentActions: StateFlow<List<RecordedAction>> = _currentActions.asStateFlow()

    private var startedAtTime: Long = 0L
    private var timerJob: Job? = null

    override fun startRecording() {
        if (_recordingState.value == RecordingState.RECORDING) return
        _recordingState.value = RecordingState.RECORDING
        _elapsedTimeMs.value = 0L
        _currentActions.value = emptyList()
        startedAtTime = System.currentTimeMillis()
        startTimerJob()
    }

    override fun pauseRecording() {
        if (_recordingState.value == RecordingState.RECORDING) {
            _recordingState.value = RecordingState.PAUSED
            timerJob?.cancel()
            timerJob = null
        }
    }

    override fun resumeRecording() {
        if (_recordingState.value == RecordingState.PAUSED) {
            _recordingState.value = RecordingState.RECORDING
            startTimerJob()
        }
    }

    override fun stopRecording(): RecordingSession {
        timerJob?.cancel()
        timerJob = null

        val endedAt = System.currentTimeMillis()
        val session = RecordingSession(
            sessionId = "rec_${startedAtTime}_${(100..999).random()}",
            startedAt = startedAtTime,
            endedAt = endedAt,
            status = RecordingState.COMPLETED,
            actions = _currentActions.value,
            durationMs = _elapsedTimeMs.value
        )
        _recordingState.value = RecordingState.COMPLETED
        return session
    }

    override fun recordAction(action: RecordedAction) {
        if (_recordingState.value != RecordingState.RECORDING) return
        val list = _currentActions.value.toMutableList()
        val newSequenceNumber = list.size + 1
        val sequencedAction = action.copy(sequenceNumber = newSequenceNumber)
        list.add(sequencedAction)
        _currentActions.value = list
    }

    override fun updateAction(updatedAction: RecordedAction) {
        val list = _currentActions.value.toMutableList()
        val index = list.indexOfFirst { it.id == updatedAction.id }
        if (index >= 0) {
            list[index] = updatedAction
            _currentActions.value = list
        }
    }

    override fun deleteAction(actionId: String) {
        val list = _currentActions.value.toMutableList()
        val removed = list.removeIf { it.id == actionId }
        if (removed) {
            val renumbered = list.mapIndexed { idx, act ->
                act.copy(sequenceNumber = idx + 1)
            }
            _currentActions.value = renumbered
        }
    }

    override fun reorderActions(fromIndex: Int, toIndex: Int) {
        val list = _currentActions.value.toMutableList()
        if (fromIndex in list.indices && toIndex in list.indices && fromIndex != toIndex) {
            val item = list.removeAt(fromIndex)
            list.add(toIndex, item)
            val renumbered = list.mapIndexed { idx, act ->
                act.copy(sequenceNumber = idx + 1)
            }
            _currentActions.value = renumbered
        }
    }

    override fun clear() {
        timerJob?.cancel()
        timerJob = null
        _recordingState.value = RecordingState.IDLE
        _elapsedTimeMs.value = 0L
        _currentActions.value = emptyList()
    }

    private fun startTimerJob() {
        timerJob?.cancel()
        timerJob = scope.launch {
            while (_recordingState.value == RecordingState.RECORDING) {
                delay(200)
                _elapsedTimeMs.value += 200
            }
        }
    }
}
