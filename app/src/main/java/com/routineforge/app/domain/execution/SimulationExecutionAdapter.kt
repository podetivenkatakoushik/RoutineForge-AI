package com.routineforge.app.domain.execution

import com.routineforge.app.domain.model.ActionType
import com.routineforge.app.domain.model.ExecutionStepResult
import com.routineforge.app.domain.model.RoutineStep
import com.routineforge.app.domain.workspace.InMemorySimulationWorkspace
import com.routineforge.app.domain.workspace.SimulationWorkspace
import kotlinx.coroutines.delay

class SimulationExecutionAdapter(
    private val workspace: SimulationWorkspace = InMemorySimulationWorkspace.instance
) : ExecutionAdapter {

    override val name: String = "Controlled Workspace Simulation Adapter"

    override suspend fun executeStep(step: RoutineStep): ExecutionStepResult {
        val startTime = System.currentTimeMillis()
        delay(400) // Realistic simulation execution delay

        fun param(key: String, default: String): String {
            return step.parameters.find { it.key.equals(key, ignoreCase = true) }?.value ?: default
        }

        return try {
            when (step.actionType) {
                ActionType.SELECT_FILE -> {
                    val fileName = param("fileName", "Assignment.docx")
                    val ok = workspace.selectDocument(fileName)
                    val elapsed = System.currentTimeMillis() - startTime
                    if (ok) {
                        ExecutionStepResult(
                            stepId = step.id,
                            stepTitle = step.title,
                            success = true,
                            durationMs = elapsed,
                            outputMessage = "Selected document '$fileName' in workspace."
                        )
                    } else {
                        ExecutionStepResult(
                            stepId = step.id,
                            stepTitle = step.title,
                            success = false,
                            durationMs = elapsed,
                            outputMessage = "Document '$fileName' could not be found.",
                            errorMessage = "Document '$fileName' not found in workspace."
                        )
                    }
                }

                ActionType.RENAME_FILE -> {
                    val oldName = param("oldName", "Assignment.docx")
                    val newName = param("newName", "Renamed_Assignment.docx")
                    val ok = workspace.renameDocument(oldName, newName)
                    val elapsed = System.currentTimeMillis() - startTime
                    if (ok) {
                        ExecutionStepResult(
                            stepId = step.id,
                            stepTitle = step.title,
                            success = true,
                            durationMs = elapsed,
                            outputMessage = "Renamed file to '$newName' in workspace."
                        )
                    } else {
                        ExecutionStepResult(
                            stepId = step.id,
                            stepTitle = step.title,
                            success = false,
                            durationMs = elapsed,
                            outputMessage = "Failed to rename file.",
                            errorMessage = "No selected file available for rename."
                        )
                    }
                }

                ActionType.MOVE_FILE -> {
                    val fileName = param("fileName", "Renamed_Assignment.docx")
                    val destFolder = param("destinationFolder", param("destination", "/Documents/Organized"))
                    val ok = workspace.moveDocument(fileName, destFolder)
                    val elapsed = System.currentTimeMillis() - startTime
                    if (ok) {
                        ExecutionStepResult(
                            stepId = step.id,
                            stepTitle = step.title,
                            success = true,
                            durationMs = elapsed,
                            outputMessage = "Moved file to '$destFolder'."
                        )
                    } else {
                        ExecutionStepResult(
                            stepId = step.id,
                            stepTitle = step.title,
                            success = false,
                            durationMs = elapsed,
                            outputMessage = "Failed to move file.",
                            errorMessage = "Destination folder '$destFolder' is invalid or document missing."
                        )
                    }
                }

                ActionType.COPY_FILE -> {
                    val fileName = param("fileName", "Assignment.docx")
                    val ok = workspace.copyDocument(fileName)
                    val elapsed = System.currentTimeMillis() - startTime
                    ExecutionStepResult(
                        stepId = step.id,
                        stepTitle = step.title,
                        success = ok,
                        durationMs = elapsed,
                        outputMessage = if (ok) "Copied document successfully." else "Copy operation failed."
                    )
                }

                ActionType.CREATE_FILE -> {
                    val folderName = param("folderName", "NewFolder")
                    val ok = workspace.createFolder(folderName)
                    val elapsed = System.currentTimeMillis() - startTime
                    ExecutionStepResult(
                        stepId = step.id,
                        stepTitle = step.title,
                        success = ok,
                        durationMs = elapsed,
                        outputMessage = "Created directory '/Documents/$folderName'."
                    )
                }

                ActionType.EXTRACT_TEXT -> {
                    val fileName = param("fileName", "Project_Report.pdf")
                    val summary = workspace.extractText(fileName)
                    val elapsed = System.currentTimeMillis() - startTime
                    ExecutionStepResult(
                        stepId = step.id,
                        stepTitle = step.title,
                        success = true,
                        durationMs = elapsed,
                        outputMessage = summary
                    )
                }

                ActionType.READ_FILE, ActionType.EXTRACT_DATA, ActionType.TRANSFORM_DATA, ActionType.SHOW_RESULT, ActionType.USER_INPUT -> {
                    val elapsed = System.currentTimeMillis() - startTime
                    ExecutionStepResult(
                        stepId = step.id,
                        stepTitle = step.title,
                        success = true,
                        durationMs = elapsed,
                        outputMessage = "Simulated action '${step.actionType.displayTitle}' completed successfully."
                    )
                }

                ActionType.WAIT -> {
                    delay(300)
                    val elapsed = System.currentTimeMillis() - startTime
                    ExecutionStepResult(
                        stepId = step.id,
                        stepTitle = step.title,
                        success = true,
                        durationMs = elapsed,
                        outputMessage = "Wait delay completed (400ms)."
                    )
                }

                else -> {
                    val elapsed = System.currentTimeMillis() - startTime
                    ExecutionStepResult(
                        stepId = step.id,
                        stepTitle = step.title,
                        success = false,
                        durationMs = elapsed,
                        outputMessage = "Action type is not supported by the simulation engine.",
                        errorMessage = "Action '${step.actionType.name}' is unsupported in simulation mode."
                    )
                }
            }
        } catch (e: Exception) {
            val elapsed = System.currentTimeMillis() - startTime
            ExecutionStepResult(
                stepId = step.id,
                stepTitle = step.title,
                success = false,
                durationMs = elapsed,
                outputMessage = "Step execution threw exception: ${e.localizedMessage}",
                errorMessage = e.localizedMessage
            )
        }
    }
}
