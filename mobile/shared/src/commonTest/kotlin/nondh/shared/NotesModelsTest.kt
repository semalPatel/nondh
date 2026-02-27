package nondh.shared

import kotlin.test.Test
import kotlin.test.assertEquals

class NotesModelsTest {
    @Test
    fun noteDefaultsToEmptyTitle() {
        val note = Note(id = "n1", title = "", body = "hi", updatedAt = 1000)
        assertEquals("", note.title)
    }
}
