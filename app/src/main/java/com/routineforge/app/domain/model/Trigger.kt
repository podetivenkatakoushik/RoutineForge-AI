package com.routineforge.app.domain.model

enum class TriggerType {
    MANUAL,
    FILE_SELECTED,
    USER_REQUEST,
    VOICE_REQUEST,
    SCHEDULED,
    REPEATED_PATTERN
}

sealed class Trigger(val type: TriggerType, val displayTitle: String) {
    data class Manual(val name: String = "Manual Launch") :
        Trigger(TriggerType.MANUAL, "Manual Launch")

    data class FileSelected(val targetFolder: String, val extensionPattern: String = "*.*") :
        Trigger(TriggerType.FILE_SELECTED, "File Selected ($extensionPattern)")

    data class UserRequest(val prompt: String) :
        Trigger(TriggerType.USER_REQUEST, "User Request")

    data class VoiceRequest(val triggerPhrase: String) :
        Trigger(TriggerType.VOICE_REQUEST, "Voice Request")

    data class Scheduled(val timeOfDay: String, val daysOfWeek: List<String> = listOf("Mon", "Tue", "Wed", "Thu", "Fri")) :
        Trigger(TriggerType.SCHEDULED, "Scheduled ($timeOfDay)")

    data class RepeatedPattern(val patternName: String, val thresholdCount: Int = 3) :
        Trigger(TriggerType.REPEATED_PATTERN, "Repeated Pattern Detected")
}
