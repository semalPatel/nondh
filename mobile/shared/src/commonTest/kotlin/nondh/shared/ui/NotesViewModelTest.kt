package nondh.shared.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.advanceTimeBy
import nondh.shared.db.InMemoryNotesDb

class NotesViewModelTest {
    @Test
    fun addNoteAddsToList() = runTest {
        val vm = NotesViewModel(
            db = InMemoryNotesDb(),
            baseUrl = "http://localhost:8080",
            token = "test",
            scope = this,
            nowMillis = { 1000L }
        )
        vm.addNote("hello", 1000)
        assertEquals(1, vm.state.notes.size)
    }

    @Test
    fun saveSettingsPersistsValues() = runTest {
        val db = InMemoryNotesDb()
        val vm = NotesViewModel(
            db = db,
            baseUrl = "http://a",
            token = "t1",
            scope = this,
            nowMillis = { 1000L }
        )
        vm.updateSettingsBaseUrl("http://b")
        vm.updateSettingsToken("t2")
        vm.saveSettings()

        assertEquals("http://b", db.getSetting("base_url"))
        assertEquals("t2", db.getSetting("token"))
    }

    @Test
    fun autoSaveDebouncesDraft() = runTest {
        val db = InMemoryNotesDb()
        val vm = NotesViewModel(
            db = db,
            baseUrl = "http://a",
            token = "t1",
            scope = this,
            nowMillis = { 2000L }
        )

        vm.updateDraft("a")
        vm.updateDraft("ab")
        advanceTimeBy(900)

        val note = db.listVisible().first()
        assertEquals("ab", note.body)
    }
}
