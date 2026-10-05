package com.routineforge.app.recorder

import com.routineforge.app.recorder.model.RecordingSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface RecordingSessionStore {
    val activeSession: StateFlow<RecordingSession?>
    fun saveSession(session: RecordingSession)
    fun updateSession(session: RecordingSession)
    fun clear()
}

class InMemoryRecordingSessionStore : RecordingSessionStore {
    private val _activeSession = MutableStateFlow<RecordingSession?>(null)
    override val activeSession: StateFlow<RecordingSession?> = _activeSession.asStateFlow()

    override fun saveSession(session: RecordingSession) {
        _activeSession.value = session
    }

    override fun updateSession(session: RecordingSession) {
        _activeSession.value = session
    }

    override fun clear() {
        _activeSession.value = null
    }

    companion object {
        val instance: RecordingSessionStore by lazy { InMemoryRecordingSessionStore() }
    }
}
