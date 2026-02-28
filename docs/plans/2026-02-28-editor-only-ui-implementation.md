# Editor-Only UI Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Replace the current editor-first layout with an editor-only home screen, a hamburger drawer for notes, and a FAB quick-actions sheet.

**Architecture:** Main screen is a full-screen editor. Use `ModalNavigationDrawer` for a full-screen notes list. Use a FAB to open a bottom-sheet with actions (New, Sync, Settings). Autosave remains debounced in the ViewModel.

**Tech Stack:** Kotlin Multiplatform, Compose Multiplatform, Material3.

---

### Task 1: Add drawer and FAB actions UI

**Files:**
- Modify: `mobile/shared/src/commonMain/kotlin/nondh/shared/ui/NotesScreen.kt`

**Step 1: Implement ModalNavigationDrawer + editor-only content**

```kotlin
ModalNavigationDrawer(
    drawerContent = { NotesDrawer(...) },
) {
    EditorContent(...)
}
```

**Step 2: Add FAB with quick actions sheet**

```kotlin
FloatingActionButton(onClick = { showActions = true }) { Text("+") }
ModalBottomSheet(...) { ActionRow("New"), ActionRow("Sync"), ActionRow("Settings") }
```

**Step 3: Commit**

```bash
git add mobile/shared/src/commonMain/kotlin/nondh/shared/ui/NotesScreen.kt
git commit -m "feat(ui): editor-only layout with drawer and fab"
```

### Task 2: Wire drawer and FAB actions to ViewModel

**Files:**
- Modify: `mobile/shared/src/commonMain/kotlin/nondh/shared/ui/NotesScreen.kt`
- Modify: `mobile/androidApp/src/main/java/nondh/android/MainActivity.kt`
- Modify: `mobile/shared/src/iosMain/kotlin/nondh/shared/NotesViewController.kt`

**Step 1: Connect drawer selection**

```kotlin
onSelect = { note -> viewModel.selectNote(note) }
```

**Step 2: Connect actions**

```kotlin
onNewNote = { viewModel.newNote() }
onSyncNow = { viewModel.syncNow() }
onOpenSettings = { viewModel.openSettings() }
```

**Step 3: Commit**

```bash
git add mobile/shared/src/commonMain/kotlin/nondh/shared/ui/NotesScreen.kt mobile/androidApp/src/main/java/nondh/android/MainActivity.kt mobile/shared/src/iosMain/kotlin/nondh/shared/NotesViewController.kt
git commit -m "feat(ui): wire drawer and fab actions"
```

### Task 3: Minimal status strip on editor

**Files:**
- Modify: `mobile/shared/src/commonMain/kotlin/nondh/shared/ui/NotesScreen.kt`

**Step 1: Add small status text at top**

```kotlin
if (lastSyncError != null) Text("Sync error: ...") else Text("Last sync: ...")
```

**Step 2: Commit**

```bash
git add mobile/shared/src/commonMain/kotlin/nondh/shared/ui/NotesScreen.kt
git commit -m "feat(ui): add editor sync status strip"
```
