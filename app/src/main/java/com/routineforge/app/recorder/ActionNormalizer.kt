package com.routineforge.app.recorder

import com.routineforge.app.domain.model.ActionType
import com.routineforge.app.domain.model.ParameterType
import com.routineforge.app.domain.model.StepParameter
import com.routineforge.app.recorder.model.RecordedAction

/**
 * Normalizes UI interactions from the workspace into structured semantic RecordedAction objects.
 */
object ActionNormalizer {

    private fun generateUniqueId(prefix: String): String {
        return "${prefix}_${System.nanoTime()}_${(100..999).random()}"
    }

    fun normalizeSelectDocument(
        fileName: String,
        folderPath: String,
        sequenceNumber: Int
    ): RecordedAction {
        return RecordedAction(
            id = generateUniqueId("act_sel"),
            sequenceNumber = sequenceNumber,
            actionType = ActionType.SELECT_FILE,
            title = "Select Document",
            description = "User selected '$fileName' from $folderPath",
            parameters = listOf(
                StepParameter("fileName", fileName, "File Name", ParameterType.TEXT),
                StepParameter("folderPath", folderPath, "Folder Path", ParameterType.FOLDER_PATH)
            ),
            source = "IN_APP_WORKSPACE"
        )
    }

    fun normalizeRenameDocument(
        oldName: String,
        newName: String,
        folderPath: String,
        sequenceNumber: Int
    ): RecordedAction {
        return RecordedAction(
            id = generateUniqueId("act_ren"),
            sequenceNumber = sequenceNumber,
            actionType = ActionType.RENAME_FILE,
            title = "Rename File",
            description = "User renamed '$oldName' to '$newName'",
            parameters = listOf(
                StepParameter("oldName", oldName, "Original Name", ParameterType.TEXT),
                StepParameter("newName", newName, "New Name", ParameterType.TEXT),
                StepParameter("folderPath", folderPath, "Folder Path", ParameterType.FOLDER_PATH)
            ),
            source = "IN_APP_WORKSPACE"
        )
    }

    fun normalizeMoveDocument(
        fileName: String,
        sourceFolder: String,
        destinationFolder: String,
        sequenceNumber: Int
    ): RecordedAction {
        return RecordedAction(
            id = generateUniqueId("act_mov"),
            sequenceNumber = sequenceNumber,
            actionType = ActionType.MOVE_FILE,
            title = "Move File",
            description = "User moved '$fileName' to '$destinationFolder'",
            parameters = listOf(
                StepParameter("fileName", fileName, "File Name", ParameterType.TEXT),
                StepParameter("sourceFolder", sourceFolder, "Source Folder", ParameterType.FOLDER_PATH),
                StepParameter("destinationFolder", destinationFolder, "Destination Folder", ParameterType.FOLDER_PATH)
            ),
            source = "IN_APP_WORKSPACE"
        )
    }

    fun normalizeCreateFolder(
        folderName: String,
        parentPath: String,
        sequenceNumber: Int
    ): RecordedAction {
        return RecordedAction(
            id = generateUniqueId("act_fld"),
            sequenceNumber = sequenceNumber,
            actionType = ActionType.CREATE_FILE,
            title = "Create Folder",
            description = "User created directory '$folderName' in $parentPath",
            parameters = listOf(
                StepParameter("folderName", folderName, "Folder Name", ParameterType.TEXT),
                StepParameter("parentPath", parentPath, "Parent Path", ParameterType.FOLDER_PATH)
            ),
            source = "IN_APP_WORKSPACE"
        )
    }

    fun normalizeExtractText(
        fileName: String,
        extractedSummary: String,
        sequenceNumber: Int
    ): RecordedAction {
        return RecordedAction(
            id = generateUniqueId("act_ext"),
            sequenceNumber = sequenceNumber,
            actionType = ActionType.EXTRACT_TEXT,
            title = "Extract Document Text",
            description = "User extracted content summary from '$fileName'",
            parameters = listOf(
                StepParameter("fileName", fileName, "File Name", ParameterType.TEXT),
                StepParameter("extractedSummary", extractedSummary, "Summary", ParameterType.TEXT)
            ),
            source = "IN_APP_WORKSPACE"
        )
    }
}
