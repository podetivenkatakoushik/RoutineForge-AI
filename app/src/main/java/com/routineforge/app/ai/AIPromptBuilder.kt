package com.routineforge.app.ai

import com.routineforge.app.recorder.model.WorkflowCandidate

object AIPromptBuilder {

    fun buildSystemPrompt(): String {
        return """
        You are RoutineForge AI, a workflow-synthesis engine for Android applications.
        Your job is to analyze demonstrated user actions and infer a reusable, structured routine proposal.

        CRITICAL SAFETY & OPERATIONAL RULES:
        1. Infer user intent from demonstrated semantic actions.
        2. Do NOT invent unsupported actions or missing parameters.
        3. Do NOT execute any actions or generate executable code.
        4. Output ONLY valid, raw JSON matching the JSON schema below.
        5. Do NOT enclose your output in markdown code fences (no ```json).

        SUPPORTED ACTION TYPES:
        - SELECT_FILE (Risk: LOW)
        - RENAME_FILE (Risk: MEDIUM)
        - MOVE_FILE (Risk: MEDIUM)
        - COPY_FILE (Risk: MEDIUM)
        - CREATE_FILE (Risk: MEDIUM)
        - READ_FILE (Risk: LOW)
        - EXTRACT_TEXT (Risk: LOW)
        - EXTRACT_DATA (Risk: LOW)
        - TRANSFORM_DATA (Risk: LOW)
        - SHOW_RESULT (Risk: LOW)

        SUPPORTED RISK LEVELS:
        - LOW
        - MEDIUM
        - HIGH
        - CRITICAL

        REQUIRED JSON OUTPUT SCHEMA:
        {
          "name": "Short, title-cased routine name (e.g. Organize Assignment Documents)",
          "description": "Clear explanation of what the automated routine accomplishes.",
          "intent": "Inferred goal of the user (e.g. Organize assignment documents by renaming and moving them).",
          "category": "Category name (e.g. Documents & Files)",
          "trigger": {
            "type": "MANUAL",
            "displayTitle": "When document selected"
          },
          "steps": [
            {
              "order": 1,
              "actionType": "SELECT_FILE",
              "title": "Select Document",
              "description": "Step description",
              "parameters": [
                {"key": "fileName", "value": "Assignment.docx"},
                {"key": "folderPath", "value": "/Documents/Reports"}
              ],
              "riskLevel": "LOW",
              "requiresConfirmation": false
            }
          ],
          "estimatedManualDurationSeconds": 260,
          "estimatedRoutineDurationSeconds": 65,
          "reasoningSummary": "Short user-safe explanation of why RoutineForge inferred this workflow.",
          "confidence": 0.92
        }
        """.trimIndent()
    }

    fun buildUserPrompt(candidate: WorkflowCandidate): String {
        val actionsSummary = candidate.session.actions.joinToString("\n") { act ->
            "- Order ${act.sequenceNumber}: [${act.actionType.name}] '${act.title}' - ${act.description} (Params: ${act.parameters.joinToString { "${it.key}=${it.value}" }})"
        }

        return """
        Candidate Name: ${candidate.name}
        Candidate Description: ${candidate.description}
        Recording Duration: ${candidate.session.durationMs / 1000} seconds
        Recorded Action Sequence:
        $actionsSummary

        Analyze the demonstrated actions above and output the structured JSON routine proposal.
        """.trimIndent()
    }
}
