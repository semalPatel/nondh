# Editor-First UI Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Replace the current list-first UI with an editor-first layout that uses a bottom-sheet list, FAB settings, and debounced auto-save.

**Architecture:** Implement a single Compose screen with a full-height editor, a bottom-sheet list for note selection, and a floating action button to open Settings. Add a debounced auto-save in the ViewModel and sync after save.

**Tech Stack:** Kotlin Multiplatform, Compose Multiplatform, SQLDelight, Ktor.

---

### Task 1: Add debounce support to NotesViewModel

**Files:**
- Modify: `mobile/shared/src/commonMain/kotlin/nondh/shared/ui/NotesViewModel.kt`
- Test: `mobile/shared/src/commonTest/kotlin/nondh/shared/ui/NotesViewModelTest.kt`

**Step 1: Write the failing test**

```kotlin
package nondh.shared.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.advanceTimeBy
import nondh.shared.db.InMemoryNotesDb

class NotesViewModelTest {
    @Test
    fun autoSaveDebouncesDraft() = runTest {
        val vm = NotesViewModel(InMemoryNotesDb(), "http://localhost:8080", "test")
        vm.selectNote(vm.state.notes.firstOrNull() ?: return@runTest)

        vm.updateDraft("a")
        vm.updateDraft("ab")
        advanceTimeBy(1000)

        val note = vm.state.notes.first()
        assertEquals("ab", note.body)
    }
}
```

**Step 2: Run test to verify it fails**

Run: `cd mobile && ./gradlew :shared:allTests`
Expected: FAIL with missing debounce behavior

**Step 3: Implement minimal debounce**

```kotlin
// NotesViewModel.kt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private var debounceJob: Job? = null

fun updateDraft(text: String) {
    state = state.copy(draftText = text)
    debounceJob?.cancel()
    debounceJob = scope.launch {
        delay(800)
        saveDraft(nowMillis())
    }
}
```

**Step 4: Run tests to verify they pass**

Run: `cd mobile && ./gradlew :shared:allTests`
Expected: PASS

**Step 5: Commit**

```bash
git add mobile/shared/src/commonMain/kotlin/nondh/shared/ui/NotesViewModel.kt mobile/shared/src/commonTest/kotlin/nondh/shared/ui/NotesViewModelTest.kt
git commit -m "feat(ui): add debounced autosave"
```

### Task 2: Editor-first layout + bottom sheet list

**Files:**
- Modify: `mobile/shared/src/commonMain/kotlin/nondh/shared/ui/NotesScreen.kt`

**Step 1: Implement bottom sheet layout**

```kotlin
// NotesScreen.kt
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.rememberBottomSheetScaffoldState

val scaffoldState = rememberBottomSheetScaffoldState()
BottomSheetScaffold(
    scaffoldState = scaffoldState,
    sheetContent = { NotesList(...) },
    sheetPeekHeight = 72.dp,
) {
    NoteEditor(...)
}
```

**Step 2: Commit**

```bash
git add mobile/shared/src/commonMain/kotlin/nondh/shared/ui/NotesScreen.kt
git commit -m "feat(ui): editor-first bottom sheet layout"
```

### Task 3: Add floating Settings FAB

**Files:**
- Modify: `mobile/shared/src/commonMain/kotlin/nondh/shared/ui/NotesScreen.kt`

**Step 1: Add FAB overlay**

```kotlin
// NotesScreen.kt
FloatingActionButton(onClick = onOpenSettings) { Text("⚙") }
```

**Step 2: Commit**

```bash
git add mobile/shared/src/commonMain/kotlin/nondh/shared/ui/NotesScreen.kt
git commit -m "feat(ui): add settings fab"
```

### Task 4: Add Sync status bar and Sync Now button in editor

**Files:**
- Modify: `mobile/shared/src/commonMain/kotlin/nondh/shared/ui/NotesScreen.kt`

**Step 1: Move sync status into editor header**

```kotlin
// NotesScreen.kt
Text("Last sync: ...")
Button(onClick = onSyncNow) { Text("Sync now") }
```

**Step 2: Commit**

```bash
git add mobile/shared/src/commonMain/kotlin/nondh/shared/ui/NotesScreen.kt
git commit -m "feat(ui): editor sync status"
```

### Task 5: Wire autosave to sync

**Files:**
- Modify: `mobile/androidApp/src/main/java/nondh/android/MainActivity.kt`
- Modify: `mobile/shared/src/iosMain/kotlin/nondh/shared/NotesViewController.kt`

**Step 1: Ensure autosave triggers sync**

```kotlin
// call syncNow() after autosave completion
```

**Step 2: Commit**

```bash
git add mobile/androidApp/src/main/java/nondh/android/MainActivity.kt mobile/shared/src/iosMain/kotlin/nondh/shared/NotesViewController.kt
git commit -m "feat(ui): sync after autosave"
```
