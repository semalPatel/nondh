package nondh.shared.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nondh.shared.Note

@Composable
fun NotesScreen(
    state: NotesState,
    onAdd: (String) -> Unit,
    onSelect: (Note) -> Unit,
    onUpdateDraft: (String) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit
) {
    if (state.selectedId != null) {
        NoteEditor(
            text = state.draftText,
            onUpdate = onUpdateDraft,
            onSave = onSave,
            onDelete = onDelete,
            onBack = onBack
        )
    } else {
        NotesList(
            notes = state.notes,
            onAdd = onAdd,
            onSelect = onSelect
        )
    }
}

@Composable
private fun NotesList(
    notes: List<Note>,
    onAdd: (String) -> Unit,
    onSelect: (Note) -> Unit
) {
    var text by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            TextField(
                modifier = Modifier.weight(1f),
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("New note") }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                if (text.isNotBlank()) {
                    onAdd(text)
                    text = ""
                }
            }) {
                Text("Add")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn {
            items(notes) { note ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(note) }
                        .padding(vertical = 8.dp)
                ) {
                    Text(note.body, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
private fun NoteEditor(
    text: String,
    onUpdate: (String) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = text,
            onValueChange = onUpdate,
            placeholder = { Text("Edit note") }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row {
            Button(onClick = onSave) {
                Text("Save")
            }
            Spacer(modifier = Modifier.width(8.dp))
            TextButton(onClick = onDelete) {
                Text("Delete")
            }
            Spacer(modifier = Modifier.width(8.dp))
            TextButton(onClick = onBack) {
                Text("Back")
            }
        }
    }
}
