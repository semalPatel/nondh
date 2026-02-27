package nondh.shared.ui

import nondh.shared.Note

data class NotesState(
    val notes: List<Note> = emptyList(),
    val selectedId: String? = null,
    val draftText: String = ""
)
