package nondh.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import nondh.shared.ui.NotesScreen
import nondh.shared.ui.NotesViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel = remember { NotesViewModel() }
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
