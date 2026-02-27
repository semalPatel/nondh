package nondh.shared.sync

import nondh.shared.Note
import nondh.shared.db.NotesDb

class SyncEngine(private val db: NotesDb) {
    fun applyRemote(notes: List<Note>) {
        notes.forEach { incoming ->
            val current = db.get(incoming.id)
            if (current == null || incoming.updatedAt >= current.updatedAt) {
                db.upsert(incoming)
            }
        }
    }
}
