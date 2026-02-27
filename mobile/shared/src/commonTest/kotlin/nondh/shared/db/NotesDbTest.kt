package nondh.shared.db

import kotlin.test.Test
import kotlin.test.assertEquals
import nondh.shared.Note

class NotesDbTest {
    @Test
    fun insertAndReadNote() {
        val db = InMemoryNotesDb()
        val note = Note("n1", "", "hello", 1000)
        db.upsert(note)
        val got = db.get("n1")
        assertEquals("hello", got?.body)
    }
}
