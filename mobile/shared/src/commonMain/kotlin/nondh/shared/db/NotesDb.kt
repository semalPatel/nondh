package nondh.shared.db

import nondh.shared.Note

interface NotesDb {
    fun upsert(note: Note)
    fun get(id: String): Note?
    fun since(ts: Long): List<Note>
}

class InMemoryNotesDb : NotesDb {
    private val data = mutableMapOf<String, Note>()

    override fun upsert(note: Note) {
        data[note.id] = note
    }

    override fun get(id: String): Note? = data[id]

    override fun since(ts: Long): List<Note> =
        data.values.filter { it.updatedAt > ts }.sortedBy { it.updatedAt }
}
