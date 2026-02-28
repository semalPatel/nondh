package nondh.shared.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import nondh.shared.Note
import nondh.shared.ui.theme.NondhTheme
import nondh.shared.ui.theme.WarmOnSurfaceVariant
import nondh.shared.ui.theme.WarmSurface

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
    NondhTheme {
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
            Surface(color = WarmSurface, modifier = Modifier.fillMaxSize()) {
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
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            EditorContent(
                text = state.draftText,
                onUpdate = onUpdateDraft,
                onOpenDrawer = { scope.launch { drawerState.open() } },
                lastSyncError = state.lastSyncError
            )

            FloatingActionButton(
                onClick = { showActions = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Text("+")
            }
        }
    }

    SpeedDial(
        expanded = showActions,
        onDismiss = { showActions = false },
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
        }
    )
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
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "${note.updatedAt}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
    lastSyncError: String?
) {
    var showToast by remember { mutableStateOf(false) }
    var menuVisible by remember { mutableStateOf(true) }
    var editTick by remember { mutableStateOf(0) }
    val interactionSource = remember { MutableInteractionSource() }

    LaunchedEffect(lastSyncError) {
        if (lastSyncError != null) {
            showToast = true
            delay(3000)
            showToast = false
        }
    }

    LaunchedEffect(editTick) {
        if (editTick == 0) return@LaunchedEffect
        delay(1500)
        menuVisible = true
    }

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            if (interaction is PressInteraction.Press) {
                menuVisible = true
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        TextField(
            modifier = Modifier.fillMaxSize(),
            value = text,
            onValueChange = { updated ->
                menuVisible = false
                editTick += 1
                onUpdate(updated)
            },
            placeholder = { Text("Start writing...") },
            interactionSource = interactionSource,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = WarmSurface,
                unfocusedContainerColor = WarmSurface,
                disabledContainerColor = WarmSurface,
                focusedIndicatorColor = WarmSurface,
                unfocusedIndicatorColor = WarmSurface,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedPlaceholderColor = WarmOnSurfaceVariant,
                unfocusedPlaceholderColor = WarmOnSurfaceVariant
            ),
            textStyle = TextStyle(
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp
            )
        )

        AnimatedVisibility(
            visible = menuVisible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                shadowElevation = 4.dp,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
            ) {
                IconButton(onClick = onOpenDrawer) {
                    Text(
                        text = "≡",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (showToast) {
            Surface(
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Text(
                    text = lastSyncError ?: "Sync error",
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun SpeedDial(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onNewNote: () -> Unit,
    onSyncNow: () -> Unit,
    onOpenSettings: () -> Unit
) {
    if (!expanded) return

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onDismiss() }
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 88.dp),
            horizontalAlignment = Alignment.End
        ) {
            SpeedDialItem("New", onNewNote)
            Spacer(modifier = Modifier.height(12.dp))
            SpeedDialItem("Sync", onSyncNow)
            Spacer(modifier = Modifier.height(12.dp))
            SpeedDialItem("Settings", onOpenSettings)
        }
    }
}

@Composable
private fun SpeedDialItem(label: String, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            color = WarmSurface,
            modifier = Modifier.padding(end = 8.dp)
        ) {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
        FloatingActionButton(
            onClick = onClick,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Text(label.take(1))
        }
    }
}
