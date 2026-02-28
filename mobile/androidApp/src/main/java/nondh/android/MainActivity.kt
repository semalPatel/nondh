package nondh.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import nondh.shared.db.DatabaseFactory
import nondh.shared.db.SqlDelightNotesDb
import nondh.shared.sync.SyncRunner
import nondh.shared.ui.NotesScreen
import nondh.shared.ui.NotesViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = SqlDelightNotesDb(DatabaseFactory(this).create())
        val viewModel = NotesViewModel(
            db = db,
            baseUrl = "http://10.0.2.2:8080",
            token = "CHANGE_ME",
            scope = lifecycleScope,
            nowMillis = { System.currentTimeMillis() }
        )
        val syncRunner = SyncRunner(lifecycleScope) { viewModel.syncNow() }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                syncRunner.trigger()
                while (true) {
                    delay(30_000)
                    syncRunner.trigger()
                }
            }
        }

        setContent {
            var state by remember { mutableStateOf(viewModel.state) }

            fun refresh() {
                state = viewModel.state
            }

            fun triggerSync() {
                lifecycleScope.launch {
                    viewModel.syncNow()
                    refresh()
                }
            }

            NotesScreen(
                state = state,
                onSelect = { note ->
                    viewModel.selectNote(note)
                    refresh()
                },
                onUpdateDraft = { text ->
                    viewModel.updateDraft(text)
                    refresh()
                },
                onSyncNow = {
                    triggerSync()
                },
                onNewNote = {
                    viewModel.newNote()
                    refresh()
                },
                onOpenSettings = {
                    viewModel.openSettings()
                    refresh()
                },
                onUpdateSettingsBaseUrl = { value ->
                    viewModel.updateSettingsBaseUrl(value)
                    refresh()
                },
                onUpdateSettingsToken = { value ->
                    viewModel.updateSettingsToken(value)
                    refresh()
                },
                onSaveSettings = {
                    viewModel.saveSettings()
                    refresh()
                    triggerSync()
                },
                onCloseSettings = {
                    viewModel.closeSettings()
                    refresh()
                }
            )
        }
    }
}
