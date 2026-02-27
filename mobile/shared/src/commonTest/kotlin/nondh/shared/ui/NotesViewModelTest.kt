package nondh.shared.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import nondh.shared.db.InMemoryNotesDb

class NotesViewModelTest {
    @Test
    fun addNoteAddsToList() {
        val vm = NotesViewModel(InMemoryNotesDb())
        vm.addNote("hello", 1000)
        assertEquals(1, vm.state.notes.size)
    }
}
