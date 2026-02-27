package nondh.shared.sync

import nondh.shared.Note

class SyncQueue {
    private val pending = mutableListOf<Note>()

    fun enqueue(note: Note) {
        pending.add(note)
    }

    fun drain(): List<Note> {
        if (pending.isEmpty()) return emptyList()
        val out = pending.toList()
        pending.clear()
        return out
    }

    fun isEmpty(): Boolean = pending.isEmpty()
}
