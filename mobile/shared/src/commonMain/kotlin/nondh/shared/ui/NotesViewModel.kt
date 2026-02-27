package nondh.shared.ui

import nondh.shared.Note

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nondh.shared.api.NotesApiClient
import nondh.shared.db.NotesDb
import nondh.shared.sync.SyncManager

class NotesViewModel(
    private val db: NotesDb,
    baseUrl: String,
    token: String,
    private val scope: CoroutineScope,
    private val nowMillis: () -> Long
) {
    private var sync: SyncManager
    var state: NotesState
        private set
    private var debounceJob: Job? = null

    init {
        val savedBaseUrl = db.getSetting("base_url") ?: baseUrl
        val savedToken = db.getSetting("token") ?: token
        sync = SyncManager(db, NotesApiClient(savedBaseUrl, savedToken))
        state = NotesState(
            notes = db.listVisible(),
            settingsBaseUrl = savedBaseUrl,
            settingsToken = savedToken,
            syncInProgress = sync.status.inProgress,
            lastSyncAt = sync.status.lastSuccessAt,
            lastSyncError = sync.status.lastError
        )
    }

    fun currentSyncManager(): SyncManager = sync

    fun addNote(body: String, updatedAt: Long) {
        val id = "local-${updatedAt}"
        val note = Note(id = id, title = "", body = body, updatedAt = updatedAt, deletedAt = null)
        sync.enqueueLocal(note)
        state = state.copy(notes = db.listVisible(), selectedId = id, draftText = body)
    }

    fun selectNote(note: Note) {
        state = state.copy(selectedId = note.id, draftText = note.body)
    }

    fun updateDraft(text: String) {
        state = state.copy(draftText = text)
        debounceJob?.cancel()
        debounceJob = scope.launch {
            delay(800)
            autoSaveDraft()
        }
    }

    fun saveDraft(updatedAt: Long) {
        val id = state.selectedId ?: return
        val existing = db.get(id) ?: return
        val updated = existing.copy(body = state.draftText, updatedAt = updatedAt, deletedAt = null)
        sync.enqueueLocal(updated)
        state = state.copy(notes = db.listVisible(), selectedId = null, draftText = "")
    }

    fun deleteSelected(deletedAt: Long) {
        val id = state.selectedId ?: return
        sync.deleteLocal(id, deletedAt)
        state = state.copy(notes = db.listVisible(), selectedId = null, draftText = "")
    }

    fun closeEditor() {
        state = state.copy(selectedId = null, draftText = "")
    }

    fun newNote() {
        state = state.copy(selectedId = null, draftText = "")
    }

    fun openSettings() {
        state = state.copy(showSettings = true)
    }

    fun closeSettings() {
        state = state.copy(showSettings = false)
    }

    fun updateSettingsBaseUrl(value: String) {
        state = state.copy(settingsBaseUrl = value)
    }

    fun updateSettingsToken(value: String) {
        state = state.copy(settingsToken = value)
    }

    fun saveSettings() {
        sync = SyncManager(db, NotesApiClient(state.settingsBaseUrl, state.settingsToken))
        db.setSetting("base_url", state.settingsBaseUrl)
        db.setSetting("token", state.settingsToken)
        state = state.copy(showSettings = false)
    }

    suspend fun syncNow() {
        sync.sync()
        state = state.copy(
            syncInProgress = sync.status.inProgress,
            lastSyncAt = sync.status.lastSuccessAt,
            lastSyncError = sync.status.lastError,
            notes = db.listVisible()
        )
    }

    private fun autoSaveDraft() {
        val text = state.draftText.trim()
        if (text.isBlank()) return

        val now = nowMillis()
        val id = state.selectedId
        if (id == null) {
            addNote(text, now)
        } else {
            val existing = db.get(id) ?: return
            val updated = existing.copy(body = text, updatedAt = now, deletedAt = null)
            sync.enqueueLocal(updated)
            state = state.copy(notes = db.listVisible())
        }

        scope.launch { syncNow() }
    }
}
