@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package nondh.shared

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.ComposeUIViewController
import nondh.shared.ui.NotesScreen
import nondh.shared.ui.NotesViewModel
import platform.UIKit.UIViewController
import platform.posix.time

fun NotesViewController(): UIViewController = ComposeUIViewController {
    val viewModel = remember { NotesViewModel() }
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
