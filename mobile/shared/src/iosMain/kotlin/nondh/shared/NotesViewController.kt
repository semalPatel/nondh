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
import nondh.shared.db.DatabaseFactory
import nondh.shared.db.SqlDelightNotesDb
import nondh.shared.sync.SyncRunner
import nondh.shared.ui.NotesScreen
import nondh.shared.ui.NotesViewModel
import platform.UIKit.UIViewController
import platform.posix.time

fun NotesViewController(): UIViewController = ComposeUIViewController {
    val db = SqlDelightNotesDb(DatabaseFactory().create())
    val scope = remember { MainScope() }
    val viewModel = NotesViewModel(
        db = db,
        baseUrl = "http://127.0.0.1:8080",
        token = "CHANGE_ME",
        scope = scope,
        nowMillis = { time(null) * 1000L }
    )
    val syncRunner = remember { SyncRunner(scope) { viewModel.syncNow() } }

    DisposableEffect(Unit) {
        onDispose { scope.cancel() }
    }

    LaunchedEffect(Unit) {
        while (true) {
            syncRunner.trigger()
            delay(30_000)
        }
    }

    var state by remember { mutableStateOf(viewModel.state) }

    fun refresh() {
        state = viewModel.state
    }

    fun triggerSync() {
        syncRunner.trigger()
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
        onSyncNow = { triggerSync() },
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
