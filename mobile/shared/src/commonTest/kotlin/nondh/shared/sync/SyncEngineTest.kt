package nondh.shared.sync

import kotlin.test.Test
import kotlin.test.assertEquals
import nondh.shared.Note
import nondh.shared.db.InMemoryNotesDb

class SyncEngineTest {
    @Test
    fun newerNoteWins() {
        val db = InMemoryNotesDb()
        val engine = SyncEngine(db)

        db.upsert(Note("n1", "", "old", 1000))
        engine.applyRemote(listOf(Note("n1", "", "new", 2000)))

        assertEquals("new", db.get("n1")?.body)
    }
}
