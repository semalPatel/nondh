package nondh.shared.ui

import nondh.shared.Note

import nondh.shared.api.NotesApiClient
import nondh.shared.db.NotesDb
import nondh.shared.sync.SyncManager

class NotesViewModel(
    private val db: NotesDb,
    baseUrl: String,
    token: String
) {
    private var sync: SyncManager = SyncManager(db, NotesApiClient(baseUrl, token))
    var state: NotesState = NotesState(
        notes = db.listVisible(),
        settingsBaseUrl = baseUrl,
        settingsToken = token
    )
        private set

    fun currentSyncManager(): SyncManager = sync

    fun addNote(body: String, updatedAt: Long) {
        val id = "local-${state.notes.size + 1}"
        val note = Note(id = id, title = "", body = body, updatedAt = updatedAt, deletedAt = null)
        sync.enqueueLocal(note)
        state = state.copy(notes = db.listVisible(), selectedId = null, draftText = "")
    }

    fun selectNote(note: Note) {
        state = state.copy(selectedId = note.id, draftText = note.body)
    }

    fun updateDraft(text: String) {
        state = state.copy(draftText = text)
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
        state = state.copy(showSettings = false)
    }
}
