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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import nondh.shared.Note

@Composable
fun NotesScreen(
    state: NotesState,
    onSelect: (Note) -> Unit,
    onUpdateDraft: (String) -> Unit,
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
    } else {
        EditorOnlyScaffold(
            state = state,
            onUpdateDraft = onUpdateDraft,
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

@Composable
private fun EditorOnlyScaffold(
    state: NotesState,
    onUpdateDraft: (String) -> Unit,
    onSelect: (Note) -> Unit,
    onSyncNow: () -> Unit,
    onOpenSettings: () -> Unit,
    onNewNote: () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = androidx.compose.material3.DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showActions by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            NotesDrawer(
                notes = state.notes,
                onSelect = { note ->
                    onSelect(note)
                    scope.launch { drawerState.close() }
                },
                onNewNote = {
                    onNewNote()
                    scope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            EditorContent(
                text = state.draftText,
                onUpdate = onUpdateDraft,
                onOpenDrawer = { scope.launch { drawerState.open() } },
                onSyncNow = onSyncNow,
                syncInProgress = state.syncInProgress,
                lastSyncAt = state.lastSyncAt,
                lastSyncError = state.lastSyncError
            )

            FloatingActionButton(
                onClick = { showActions = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Text("+")
            }
        }
    }

    if (showActions) {
        ActionsSheet(
            onNewNote = {
                onNewNote()
                showActions = false
            },
            onSyncNow = {
                onSyncNow()
                showActions = false
            },
            onOpenSettings = {
                onOpenSettings()
                showActions = false
            },
            onDismiss = { showActions = false }
        )
    }
}

@Composable
private fun NotesDrawer(
    notes: List<Note>,
    onSelect: (Note) -> Unit,
    onNewNote: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
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
private fun EditorContent(
    text: String,
    onUpdate: (String) -> Unit,
    onOpenDrawer: () -> Unit,
    onSyncNow: () -> Unit,
    syncInProgress: Boolean,
    lastSyncAt: Long?,
    lastSyncError: String?
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onOpenDrawer) { Text("Menu") }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActionsSheet(
    onNewNote: () -> Unit,
    onSyncNow: () -> Unit,
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Button(onClick = onNewNote, modifier = Modifier.fillMaxWidth()) { Text("New note") }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onSyncNow, modifier = Modifier.fillMaxWidth()) { Text("Sync now") }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onOpenSettings, modifier = Modifier.fillMaxWidth()) { Text("Settings") }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Close") }
        }
    }
}
