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
import nondh.shared.api.NotesApiClient
import nondh.shared.db.DatabaseFactory
import nondh.shared.db.SqlDelightNotesDb
import nondh.shared.sync.SyncManager
import nondh.shared.sync.SyncRunner
import nondh.shared.ui.NotesScreen
import nondh.shared.ui.NotesViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = SqlDelightNotesDb(DatabaseFactory(this).create())
        val api = NotesApiClient(
            baseUrl = "http://10.0.2.2:8080",
            token = "CHANGE_ME"
        )
        val syncManager = SyncManager(db, api)
        val syncRunner = SyncRunner(lifecycleScope, syncManager)

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
            val viewModel = NotesViewModel(db, syncManager)
            var state by remember { mutableStateOf(viewModel.state) }

            fun refresh() {
                state = viewModel.state
            }

            NotesScreen(
                state = state,
                onAdd = { body ->
                    viewModel.addNote(body, System.currentTimeMillis())
                    refresh()
                },
                onSelect = { note ->
                    viewModel.selectNote(note)
                    refresh()
                },
                onUpdateDraft = { text ->
                    viewModel.updateDraft(text)
                    refresh()
                },
                onSave = {
                    viewModel.saveDraft(System.currentTimeMillis())
                    refresh()
                },
                onDelete = {
                    viewModel.deleteSelected()
                    refresh()
                },
                onBack = {
                    viewModel.closeEditor()
                    refresh()
                }
            )
        }
    }
}
