# Floating Hamburger Overlay Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Add a floating hamburger icon overlay that fades out while typing and reappears after idle or tap, opening the drawer.

**Architecture:** Implement visibility state and debounce logic in the editor composable, and wire the icon to `drawerState.open()` from the scaffold. Use Compose animation for fade.

**Tech Stack:** Kotlin Multiplatform, Compose Multiplatform, Material3.

---

### Task 1: Add menu visibility state and debounce behavior

**Files:**
- Modify: `mobile/shared/src/commonMain/kotlin/nondh/shared/ui/NotesScreen.kt`

**Step 1: Write the failing test**
- Not applicable: no existing UI test harness in this project.
- Instead, add a manual verification checklist (below) and keep changes minimal.

**Step 2: Run test to verify it fails**
- Skipped (no UI tests).

**Step 3: Write minimal implementation**
- Add `menuVisible` state in `EditorContent`.
- On each `onValueChange`, set `menuVisible = false` and restart a debounce job (1500ms) to set it `true`.
- Add a tap handler on the editor container to set `menuVisible = true` immediately.
- Replace `onSyncNow` in the menu icon with a callback that opens the drawer.

**Step 4: Run tests to verify**
- Manual:
  - Type and confirm the icon fades out.
  - Stop typing and confirm icon returns after ~1.5s.
  - Tap editor to show icon immediately.
  - Tap icon to open drawer.

**Step 5: Commit**
```bash
git add mobile/shared/src/commonMain/kotlin/nondh/shared/ui/NotesScreen.kt
git commit -m "feat(ui): add floating menu debounce"
```

### Task 2: Add fade animation and styling

**Files:**
- Modify: `mobile/shared/src/commonMain/kotlin/nondh/shared/ui/NotesScreen.kt`

**Step 1: Write the failing test**
- Not applicable: no existing UI test harness.

**Step 2: Run test to verify it fails**
- Skipped.

**Step 3: Write minimal implementation**
- Wrap the menu icon in `AnimatedVisibility` or `animateFloatAsState` alpha.
- Place the icon inside a small `Surface` for contrast and to avoid transparency issues.
- Ensure padding does not intrude on editor content area.

**Step 4: Run tests to verify**
- Manual: confirm smooth fade and readable icon against the editor.

**Step 5: Commit**
```bash
git add mobile/shared/src/commonMain/kotlin/nondh/shared/ui/NotesScreen.kt
git commit -m "feat(ui): animate floating menu"
```

### Task 3: Wire drawer open and tidy imports

**Files:**
- Modify: `mobile/shared/src/commonMain/kotlin/nondh/shared/ui/NotesScreen.kt`

**Step 1: Write the failing test**
- Not applicable.

**Step 2: Run test to verify it fails**
- Skipped.

**Step 3: Write minimal implementation**
- Move the menu icon to the scaffold so it can access `drawerState` and call `open()`.
- Remove unused imports and stale callbacks.

**Step 4: Run tests to verify**
- Manual: confirm drawer opens on icon tap.

**Step 5: Commit**
```bash
git add mobile/shared/src/commonMain/kotlin/nondh/shared/ui/NotesScreen.kt
git commit -m "chore(ui): wire drawer menu"
```

