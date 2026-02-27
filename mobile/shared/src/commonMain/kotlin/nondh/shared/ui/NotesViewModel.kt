package nondh.shared.ui

import nondh.shared.Note

class NotesViewModel {
    var state: NotesState = NotesState()
        private set

    fun addNote(note: Note) {
        state = state.copy(notes = state.notes + note)
    }
}
