package nondh.shared.db

import nondh.shared.Note

class SqlDelightNotesDb(private val database: NondhDatabase) : NotesDb {
    override fun upsert(note: Note) {
        database.notesQueries.upsert(
            id = note.id,
            title = note.title,
            body = note.body,
            updated_at = note.updatedAt,
            deleted_at = note.deletedAt
        )
    }

    override fun get(id: String): Note? {
        val row = database.notesQueries.selectById(id).executeAsOneOrNull() ?: return null
        return Note(
            id = row.id,
            title = row.title,
            body = row.body,
            updatedAt = row.updated_at,
            deletedAt = row.deleted_at
        )
    }

    override fun since(ts: Long): List<Note> {
        return database.notesQueries.selectSince(ts).executeAsList().map { row ->
            Note(
                id = row.id,
                title = row.title,
                body = row.body,
                updatedAt = row.updated_at,
                deletedAt = row.deleted_at
            )
        }
    }

    override fun delete(id: String) {
        database.notesQueries.deleteById(id)
    }

    override fun listVisible(): List<Note> {
        return database.notesQueries.selectVisible().executeAsList().map { row ->
            Note(
                id = row.id,
                title = row.title,
                body = row.body,
                updatedAt = row.updated_at,
                deletedAt = row.deleted_at
            )
        }
    }
}
