package nondh.shared.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nondh.shared.Note

@Composable
fun NotesScreen(
    state: NotesState,
    onSelect: (Note) -> Unit,
    onUpdateDraft: (String) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onUpdateSettingsBaseUrl: (String) -> Unit,
    onUpdateSettingsToken: (String) -> Unit,
    onSaveSettings: () -> Unit,
    onCloseSettings: () -> Unit,
    onSyncNow: () -> Unit,
    onNewNote: () -> Unit
) {
    if (state.showSettings) {
        SettingsScreen(
            baseUrl = state.settingsBaseUrl,
            token = state.settingsToken,
            onUpdateBaseUrl = onUpdateSettingsBaseUrl,
            onUpdateToken = onUpdateSettingsToken,
            onSave = onSaveSettings,
            onClose = onCloseSettings
        )
    } else if (state.selectedId != null) {
        EditorScaffold(
            state = state,
            onUpdateDraft = onUpdateDraft,
            onSave = onSave,
            onDelete = onDelete,
            onBack = onBack,
            onSelect = onSelect,
            onSyncNow = onSyncNow,
            onOpenSettings = onOpenSettings,
            onNewNote = onNewNote
        )
    } else {
        EditorScaffold(
            state = state,
            onUpdateDraft = onUpdateDraft,
            onSave = onSave,
            onDelete = onDelete,
            onBack = onBack,
            onSelect = onSelect,
            onSyncNow = onSyncNow,
            onOpenSettings = onOpenSettings,
            onNewNote = onNewNote
        )
    }
}

@Composable
private fun SettingsScreen(
    baseUrl: String,
    token: String,
    onUpdateBaseUrl: (String) -> Unit,
    onUpdateToken: (String) -> Unit,
    onSave: () -> Unit,
    onClose: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = baseUrl,
            onValueChange = onUpdateBaseUrl,
            placeholder = { Text("Base URL (e.g. http://192.168.1.10:8080)") }
        )
        Spacer(modifier = Modifier.height(12.dp))
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = token,
            onValueChange = onUpdateToken,
            placeholder = { Text("Token") }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row {
            Button(onClick = onSave) {
                Text("Save")
            }
            Spacer(modifier = Modifier.width(8.dp))
            TextButton(onClick = onClose) {
                Text("Cancel")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditorScaffold(
    state: NotesState,
    onUpdateDraft: (String) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit,
    onSelect: (Note) -> Unit,
    onSyncNow: () -> Unit,
    onOpenSettings: () -> Unit,
    onNewNote: () -> Unit
) {
    val sheetState = rememberBottomSheetScaffoldState()

    Box(modifier = Modifier.fillMaxSize()) {
        BottomSheetScaffold(
            scaffoldState = sheetState,
            sheetPeekHeight = 72.dp,
            sheetContent = {
                NotesSheet(
                    notes = state.notes,
                    onSelect = onSelect,
                    onNewNote = onNewNote
                )
            }
        ) { padding ->
            NoteEditor(
                modifier = Modifier.padding(padding),
                text = state.draftText,
                onUpdate = onUpdateDraft,
                onSave = onSave,
                onDelete = onDelete,
                onBack = onBack,
                onSyncNow = onSyncNow,
                syncInProgress = state.syncInProgress,
                lastSyncAt = state.lastSyncAt,
                lastSyncError = state.lastSyncError
            )
        }

        FloatingActionButton(
            onClick = onOpenSettings,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Text("Set")
        }
    }
}
@Composable
private fun NotesSheet(
    notes: List<Note>,
    onSelect: (Note) -> Unit,
    onNewNote: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Notes", style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = onNewNote) {
                Text("New")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn {
            items(notes) { note ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(note) }
                        .padding(vertical = 8.dp)
                ) {
                    Text(note.body.take(120), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun NoteEditor(
    modifier: Modifier = Modifier,
    text: String,
    onUpdate: (String) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit,
    onSyncNow: () -> Unit,
    syncInProgress: Boolean,
    lastSyncAt: Long?,
    lastSyncError: String?
) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row {
                Button(onClick = onSave) { Text("Save") }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = onDelete) { Text("Delete") }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = onBack) { Text("Back") }
            }
            TextButton(onClick = onSyncNow, enabled = !syncInProgress) {
                Text(if (syncInProgress) "Syncing..." else "Sync now")
            }
        }
        if (lastSyncError != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Sync error: $lastSyncError", color = MaterialTheme.colorScheme.error)
        } else if (lastSyncAt != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Last sync: $lastSyncAt")
        }
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            modifier = Modifier.fillMaxSize(),
            value = text,
            onValueChange = onUpdate,
            placeholder = { Text("Start writing...") }
        )
    }
}
