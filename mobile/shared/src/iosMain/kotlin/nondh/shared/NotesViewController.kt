package nondh.shared

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController
import nondh.shared.ui.NotesScreen
import nondh.shared.ui.NotesState
import kotlinx.datetime.Clock

fun NotesViewController(): UIViewController = ComposeUIViewController {
    var state by remember { mutableStateOf(NotesState()) }
    NotesScreen(state = state, onAdd = { body ->
        state = state.copy(
            notes = state.notes + Note(
                id = "local-${state.notes.size}",
                title = "",
                body = body,
                updatedAt = Clock.System.now().toEpochMilliseconds()
            )
        )
    })
}
