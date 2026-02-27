package nondh.shared.sync

import kotlin.math.max
import nondh.shared.Note
import nondh.shared.api.NotesApi
import nondh.shared.db.NotesDb

class SyncManager(
    private val db: NotesDb,
    private val api: NotesApi,
    private val queue: SyncQueue = SyncQueue(),
    private val engine: SyncEngine = SyncEngine(db)
) {
    private var lastSyncMillis: Long = 0
    var status: SyncStatus = SyncStatus()
        private set

    fun enqueueLocal(note: Note) {
        db.upsert(note)
        queue.enqueue(note)
    }

    fun deleteLocal(id: String, deletedAt: Long) {
        val existing = db.get(id) ?: return
        val tombstone = existing.copy(deletedAt = deletedAt, updatedAt = deletedAt)
        db.upsert(tombstone)
        queue.enqueue(tombstone)
    }

    suspend fun sync() {
        status = status.copy(inProgress = true, lastError = null)
        try {
            val pending = queue.drain()
            pending.forEach { api.upsert(it) }

            val remote = api.list(lastSyncMillis)
            engine.applyRemote(remote)

            val pendingMax = pending.maxOfOrNull { it.updatedAt } ?: lastSyncMillis
            val remoteMax = remote.maxOfOrNull { it.updatedAt } ?: lastSyncMillis
            lastSyncMillis = max(lastSyncMillis, max(pendingMax, remoteMax))
            status = status.copy(inProgress = false, lastSuccessAt = lastSyncMillis, lastError = null)
        } catch (e: Throwable) {
            status = status.copy(inProgress = false, lastError = e.message ?: "sync failed")
        }
    }
}
