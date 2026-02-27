package nondh.shared.db

import nondh.shared.Note

interface NotesDb {
    fun upsert(note: Note)
    fun get(id: String): Note?
    fun since(ts: Long): List<Note>
    fun delete(id: String)
}

class InMemoryNotesDb : NotesDb {
    private val data = mutableMapOf<String, Note>()

    override fun upsert(note: Note) {
        data[note.id] = note
    }

    override fun get(id: String): Note? = data[id]

    override fun since(ts: Long): List<Note> =
        data.values.filter { it.updatedAt > ts }.sortedBy { it.updatedAt }

    override fun delete(id: String) {
        data.remove(id)
    }
}
