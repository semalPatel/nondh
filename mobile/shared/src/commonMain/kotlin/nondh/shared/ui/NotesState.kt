package nondh.shared.ui

import nondh.shared.Note

data class NotesState(
    val notes: List<Note> = emptyList(),
    val selectedId: String? = null,
    val draftText: String = "",
    val showSettings: Boolean = false,
    val settingsBaseUrl: String = "",
    val settingsToken: String = "",
    val syncInProgress: Boolean = false,
    val lastSyncAt: Long? = null,
    val lastSyncError: String? = null
)
