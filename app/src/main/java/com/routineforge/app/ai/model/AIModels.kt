package com.routineforge.app.ai.model

data class AIAnalysisRequest(
    val rawWorkflowData: String,
    val context: Map<String, String> = emptyMap()
)

data class AIAnalysisResponse(
    val detectedIntent: String,
    val confidenceScore: Float,
    val suggestedActionNames: List<String> = emptyList()
)
