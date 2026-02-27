package nondh.shared.ui

import nondh.shared.Note

class NotesViewModel {
    var state: NotesState = NotesState()
        private set

    fun addNote(body: String, updatedAt: Long) {
        val id = "local-${state.notes.size}"
        val note = Note(id = id, title = "", body = body, updatedAt = updatedAt)
        state = state.copy(notes = state.notes + note)
    }

    fun selectNote(note: Note) {
        state = state.copy(selectedId = note.id, draftText = note.body)
    }

    fun updateDraft(text: String) {
        state = state.copy(draftText = text)
    }

    fun saveDraft(updatedAt: Long) {
        val id = state.selectedId ?: return
        val updated = state.notes.map { note ->
            if (note.id == id) note.copy(body = state.draftText, updatedAt = updatedAt) else note
        }
        state = state.copy(notes = updated, selectedId = null, draftText = "")
    }

    fun deleteSelected() {
        val id = state.selectedId ?: return
        state = state.copy(
            notes = state.notes.filterNot { it.id == id },
            selectedId = null,
            draftText = ""
        )
    }

    fun closeEditor() {
        state = state.copy(selectedId = null, draftText = "")
    }
}
