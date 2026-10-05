package com.routineforge.app.ai

import com.routineforge.app.domain.model.ActionType
import com.routineforge.app.domain.model.ParameterType
import com.routineforge.app.domain.model.RiskLevel
import com.routineforge.app.domain.model.RoutineProposal
import com.routineforge.app.domain.model.RoutineStep
import com.routineforge.app.domain.model.StepParameter
import com.routineforge.app.domain.model.Trigger

sealed class RoutineProposalValidationResult {
    data class Success(val proposal: RoutineProposal) : RoutineProposalValidationResult()
    data class Error(val errorMessage: String, val rawJson: String? = null) : RoutineProposalValidationResult()
}

object RoutineProposalValidator {

    fun parseAndValidate(jsonString: String): RoutineProposalValidationResult {
        return try {
            val cleanJson = jsonString
                .replace("```json", "")
                .replace("```", "")
                .trim()

            if (!cleanJson.startsWith("{") || !cleanJson.endsWith("}")) {
                return RoutineProposalValidationResult.Error("JSON response must be a valid JSON object starting with '{' and ending with '}'", cleanJson)
            }

            fun extractString(key: String): String? {
                val pattern = Regex("\"$key\"\\s*:\\s*\"([^\"]*)\"")
                return pattern.find(cleanJson)?.groupValues?.get(1)
            }

            fun extractDouble(key: String): Double? {
                val pattern = Regex("\"$key\"\\s*:\\s*([0-9.]+)")
                return pattern.find(cleanJson)?.groupValues?.get(1)?.toDoubleOrNull()
            }

            fun extractLong(key: String): Long? {
                val pattern = Regex("\"$key\"\\s*:\\s*([0-9]+)")
                return pattern.find(cleanJson)?.groupValues?.get(1)?.toLongOrNull()
            }

            val name = extractString("name")?.trim() ?: ""
            if (name.isBlank()) {
                return RoutineProposalValidationResult.Error("Generated routine name cannot be empty.", cleanJson)
            }

            val description = extractString("description")?.trim() ?: ""
            if (description.isBlank()) {
                return RoutineProposalValidationResult.Error("Generated routine description cannot be empty.", cleanJson)
            }

            val intent = extractString("intent")?.trim() ?: "Automated workflow synthesized by RoutineForge AI"
            val category = extractString("category")?.trim() ?: "Documents & Files"
            val reasoningSummary = extractString("reasoningSummary")?.trim() ?: "RoutineForge inferred this routine from demonstrated actions."
            val confidence = (extractDouble("confidence") ?: 0.90).toFloat().coerceIn(0.1f, 1.0f)

            val manualSec = extractLong("estimatedManualDurationSeconds") ?: 240L
            val routineSec = extractLong("estimatedRoutineDurationSeconds") ?: 60L

            val triggerTitle = extractString("displayTitle") ?: "When document selected"
            val trigger = Trigger.Manual(name = triggerTitle)

            val stepsStart = cleanJson.indexOf("\"steps\"")
            val stepsBlock = if (stepsStart != -1) {
                val arrayStart = cleanJson.indexOf("[", stepsStart)
                if (arrayStart != -1) {
                    var depth = 0
                    var endIdx = -1
                    for (i in arrayStart until cleanJson.length) {
                        if (cleanJson[i] == '[') depth++
                        else if (cleanJson[i] == ']') {
                            depth--
                            if (depth == 0) {
                                endIdx = i
                                break
                            }
                        }
                    }
                    if (endIdx != -1) cleanJson.substring(arrayStart + 1, endIdx) else ""
                } else ""
            } else ""

            val stepObjects = mutableListOf<String>()
            var objDepth = 0
            var startObjIdx = -1
            for (i in stepsBlock.indices) {
                if (stepsBlock[i] == '{') {
                    if (objDepth == 0) startObjIdx = i
                    objDepth++
                } else if (stepsBlock[i] == '}') {
                    objDepth--
                    if (objDepth == 0 && startObjIdx != -1) {
                        stepObjects.add(stepsBlock.substring(startObjIdx, i + 1))
                        startObjIdx = -1
                    }
                }
            }

            val steps = mutableListOf<RoutineStep>()
            stepObjects.forEachIndexed { index, stepJson ->
                fun extractStepString(key: String): String? {
                    return Regex("\"$key\"\\s*:\\s*\"([^\"]*)\"").find(stepJson)?.groupValues?.get(1)
                }

                val actionTypeName = (extractStepString("actionType") ?: "SELECT_FILE").uppercase()
                val actionType = try {
                    ActionType.valueOf(actionTypeName)
                } catch (e: Exception) {
                    ActionType.SELECT_FILE
                }

                val stepTitle = extractStepString("title") ?: actionType.displayTitle
                val stepDesc = extractStepString("description") ?: "Perform ${actionType.displayTitle}"

                val riskLevelName = (extractStepString("riskLevel") ?: actionType.defaultRiskLevel.name).uppercase()
                val riskLevel = try {
                    RiskLevel.valueOf(riskLevelName)
                } catch (e: Exception) {
                    actionType.defaultRiskLevel
                }

                val requiresConfirm = stepJson.contains("\"requiresConfirmation\"\\s*:\\s*true".toRegex()) || riskLevel != RiskLevel.LOW

                val paramsList = mutableListOf<StepParameter>()
                val paramsStart = stepJson.indexOf("\"parameters\"")
                val paramsBlock = if (paramsStart != -1) {
                    val pArrayStart = stepJson.indexOf("[", paramsStart)
                    if (pArrayStart != -1) {
                        var pDepth = 0
                        var pEndIdx = -1
                        for (p in pArrayStart until stepJson.length) {
                            if (stepJson[p] == '[') pDepth++
                            else if (stepJson[p] == ']') {
                                pDepth--
                                if (pDepth == 0) {
                                    pEndIdx = p
                                    break
                                }
                            }
                        }
                        if (pEndIdx != -1) stepJson.substring(pArrayStart + 1, pEndIdx) else ""
                    } else ""
                } else ""

                val paramObjects = mutableListOf<String>()
                var pObjDepth = 0
                var pStartIdx = -1
                for (p in paramsBlock.indices) {
                    if (paramsBlock[p] == '{') {
                        if (pObjDepth == 0) pStartIdx = p
                        pObjDepth++
                    } else if (paramsBlock[p] == '}') {
                        pObjDepth--
                        if (pObjDepth == 0 && pStartIdx != -1) {
                            paramObjects.add(paramsBlock.substring(pStartIdx, p + 1))
                            pStartIdx = -1
                        }
                    }
                }

                paramObjects.forEach { paramJson ->
                    val key = Regex("\"key\"\\s*:\\s*\"([^\"]*)\"").find(paramJson)?.groupValues?.get(1) ?: ""
                    val value = Regex("\"value\"\\s*:\\s*\"([^\"]*)\"").find(paramJson)?.groupValues?.get(1) ?: ""
                    if (key.isNotBlank()) {
                        paramsList.add(
                            StepParameter(
                                key = key,
                                value = value,
                                label = key,
                                type = if (key.contains("Folder", ignoreCase = true) || key.contains("Path", ignoreCase = true)) ParameterType.FOLDER_PATH else ParameterType.TEXT
                            )
                        )
                    }
                }

                steps.add(
                    RoutineStep(
                        id = "step_${index + 1}_${System.currentTimeMillis()}",
                        order = index + 1,
                        actionType = actionType,
                        title = stepTitle,
                        description = stepDesc,
                        parameters = paramsList,
                        riskLevel = riskLevel,
                        requiresConfirmation = requiresConfirm
                    )
                )
            }

            if (steps.isEmpty()) {
                return RoutineProposalValidationResult.Error("Generated routine contains no valid steps.", cleanJson)
            }

            val proposal = RoutineProposal(
                name = name,
                description = description,
                intent = intent,
                category = category,
                trigger = trigger,
                steps = steps,
                estimatedManualDurationMs = manualSec * 1000L,
                estimatedRoutineDurationMs = routineSec * 1000L,
                reasoningSummary = reasoningSummary,
                confidenceScore = confidence
            )

            RoutineProposalValidationResult.Success(proposal)
        } catch (e: Exception) {
            RoutineProposalValidationResult.Error("JSON parsing failed: ${e.localizedMessage ?: "Invalid structure"}", jsonString)
        }
    }
}
