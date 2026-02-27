@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package nondh.shared

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.window.ComposeUIViewController
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import nondh.shared.api.NotesApiClient
import nondh.shared.db.DatabaseFactory
import nondh.shared.db.SqlDelightNotesDb
import nondh.shared.sync.SyncManager
import nondh.shared.sync.SyncRunner
import nondh.shared.ui.NotesScreen
import nondh.shared.ui.NotesViewModel
import platform.UIKit.UIViewController
import platform.posix.time

fun NotesViewController(): UIViewController = ComposeUIViewController {
    val db = SqlDelightNotesDb(DatabaseFactory().create())
    val api = NotesApiClient(
        baseUrl = "http://127.0.0.1:8080",
        token = "CHANGE_ME"
    )
    val syncManager = SyncManager(db, api)

    val scope = remember { MainScope() }
    val syncRunner = remember { SyncRunner(scope, syncManager) }

    DisposableEffect(Unit) {
        onDispose { scope.cancel() }
    }

    LaunchedEffect(Unit) {
        while (true) {
            syncRunner.trigger()
            delay(30_000)
        }
    }

    val viewModel = remember { NotesViewModel(db, syncManager) }
    var state by remember { mutableStateOf(viewModel.state) }

    fun refresh() {
        state = viewModel.state
    }

    fun nowMillis(): Long = time(null) * 1000L

    NotesScreen(
        state = state,
        onAdd = { body ->
            viewModel.addNote(body, nowMillis())
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
            viewModel.saveDraft(nowMillis())
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
