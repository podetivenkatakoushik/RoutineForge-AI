package com.routineforge.app.domain.workspace

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class WorkspaceDoc(
    val name: String,
    val folderPath: String = "/Documents/Reports",
    val category: String = "Documents",
    val sizeText: String = "1.1 MB"
)

interface SimulationWorkspace {
    val documents: StateFlow<List<WorkspaceDoc>>
    val folders: StateFlow<List<String>>
    val selectedDoc: StateFlow<WorkspaceDoc?>

    fun selectDocument(fileName: String): Boolean
    fun renameDocument(oldName: String, newName: String): Boolean
    fun moveDocument(fileName: String, destinationFolder: String): Boolean
    fun copyDocument(fileName: String): Boolean
    fun createFolder(folderName: String): Boolean
    fun extractText(fileName: String): String
    fun reset()
}

class InMemorySimulationWorkspace : SimulationWorkspace {
    private val initialDocs = listOf(
        WorkspaceDoc("Project_Report.pdf", "/Documents/Reports", "Reports", "2.4 MB"),
        WorkspaceDoc("Assignment.docx", "/Documents/Reports", "Documents", "1.1 MB"),
        WorkspaceDoc("Meeting_Notes.txt", "/Documents/Reports", "Notes", "45 KB"),
        WorkspaceDoc("Research_Data.csv", "/Documents/Reports", "Spreadsheets", "820 KB")
    )
    private val initialFolders = listOf("/Documents/Reports", "/Documents/Organized", "/Documents/Archive")

    private val _documents = MutableStateFlow(initialDocs)
    override val documents: StateFlow<List<WorkspaceDoc>> = _documents.asStateFlow()

    private val _folders = MutableStateFlow(initialFolders)
    override val folders: StateFlow<List<String>> = _folders.asStateFlow()

    private val _selectedDoc = MutableStateFlow<WorkspaceDoc?>(null)
    override val selectedDoc: StateFlow<WorkspaceDoc?> = _selectedDoc.asStateFlow()

    override fun selectDocument(fileName: String): Boolean {
        val found = _documents.value.find { it.name.equals(fileName, ignoreCase = true) }
        if (found != null) {
            _selectedDoc.value = found
            return true
        }
        val fallback = _documents.value.firstOrNull()
        if (fallback != null) {
            _selectedDoc.value = fallback
            return true
        }
        return false
    }

    override fun renameDocument(oldName: String, newName: String): Boolean {
        val currentDocs = _documents.value.toMutableList()
        val target = _selectedDoc.value ?: currentDocs.find { it.name.equals(oldName, ignoreCase = true) } ?: currentDocs.firstOrNull()
        if (target != null && newName.isNotBlank()) {
            val idx = currentDocs.indexOfFirst { it.name == target.name }
            if (idx >= 0) {
                val updated = target.copy(name = newName)
                currentDocs[idx] = updated
                _documents.value = currentDocs
                _selectedDoc.value = updated
                return true
            }
        }
        return false
    }

    override fun moveDocument(fileName: String, destinationFolder: String): Boolean {
        val currentDocs = _documents.value.toMutableList()
        val target = _selectedDoc.value ?: currentDocs.find { it.name.equals(fileName, ignoreCase = true) } ?: currentDocs.firstOrNull()
        if (target != null) {
            val idx = currentDocs.indexOfFirst { it.name == target.name }
            if (idx >= 0) {
                val updated = target.copy(folderPath = destinationFolder)
                currentDocs[idx] = updated
                _documents.value = currentDocs
                _selectedDoc.value = updated
                return true
            }
        }
        return false
    }

    override fun copyDocument(fileName: String): Boolean {
        val currentDocs = _documents.value.toMutableList()
        val target = _selectedDoc.value ?: currentDocs.find { it.name.equals(fileName, ignoreCase = true) } ?: currentDocs.firstOrNull()
        if (target != null) {
            val copied = target.copy(name = "Copy_of_${target.name}")
            currentDocs.add(copied)
            _documents.value = currentDocs
            return true
        }
        return false
    }

    override fun createFolder(folderName: String): Boolean {
        val path = "/Documents/$folderName"
        val currentFolders = _folders.value.toMutableList()
        if (!currentFolders.contains(path)) {
            currentFolders.add(path)
            _folders.value = currentFolders
        }
        return true
    }

    override fun extractText(fileName: String): String {
        return "Extracted document summary for '$fileName': Header, date, vendor details, and totals parsed successfully."
    }

    override fun reset() {
        _documents.value = initialDocs
        _folders.value = initialFolders
        _selectedDoc.value = null
    }

    companion object {
        val instance: SimulationWorkspace by lazy { InMemorySimulationWorkspace() }
    }
}
