package nondh.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import nondh.shared.Note
import nondh.shared.ui.NotesScreen
import nondh.shared.ui.NotesState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var state by remember { mutableStateOf(NotesState()) }
            NotesScreen(state = state, onAdd = { body ->
                state = state.copy(
                    notes = state.notes + Note(
                        id = "local-${state.notes.size}",
                        title = "",
                        body = body,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            })
        }
    }
}
