package nondh.shared.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import nondh.shared.db.InMemoryNotesDb

class NotesViewModelTest {
    @Test
    fun addNoteAddsToList() {
        val vm = NotesViewModel(InMemoryNotesDb(), "http://localhost:8080", "test")
        vm.addNote("hello", 1000)
        assertEquals(1, vm.state.notes.size)
    }

    @Test
    fun saveSettingsPersistsValues() {
        val db = InMemoryNotesDb()
        val vm = NotesViewModel(db, "http://a", "t1")
        vm.updateSettingsBaseUrl("http://b")
        vm.updateSettingsToken("t2")
        vm.saveSettings()

        assertEquals("http://b", db.getSetting("base_url"))
        assertEquals("t2", db.getSetting("token"))
    }
}
