# Editor-Only UI Polish Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Make editor truly full-screen, remove visible menu/sync controls, add edge-swipe drawer, speed-dial FAB, subtle error toast, and smaller editor font.

**Architecture:** Editor-only content is a full-screen TextField. Drawer is `ModalNavigationDrawer` with a solid background and no transparency. FAB expands into a speed-dial overlay with mini actions. Sync errors show as a transient bottom toast.

**Tech Stack:** Compose Multiplatform, Material3.

---

### Task 1: Full-screen editor + edge-swipe drawer

**Files:**
- Modify: `mobile/shared/src/commonMain/kotlin/nondh/shared/ui/NotesScreen.kt`

**Step 1: Remove top row UI**

```kotlin
// Remove Menu/Sync buttons; editor is only content
```

**Step 2: Ensure drawer has solid surface background**

```kotlin
ModalNavigationDrawer(
  drawerContent = { Box(Modifier.fillMaxSize().background(WarmSurface)) { ... } }
)
```

**Step 3: Commit**

```bash
git add mobile/shared/src/commonMain/kotlin/nondh/shared/ui/NotesScreen.kt
git commit -m "feat(ui): full-screen editor and solid drawer"
```

### Task 2: Speed-dial FAB actions

**Files:**
- Modify: `mobile/shared/src/commonMain/kotlin/nondh/shared/ui/NotesScreen.kt`

**Step 1: Replace bottom sheet with speed-dial overlay**

```kotlin
if (showActions) SpeedDial(...)
```

**Step 2: Commit**

```bash
git add mobile/shared/src/commonMain/kotlin/nondh/shared/ui/NotesScreen.kt
git commit -m "feat(ui): add speed dial fab"
```

### Task 3: Subtle error toast + smaller editor font

**Files:**
- Modify: `mobile/shared/src/commonMain/kotlin/nondh/shared/ui/NotesScreen.kt`

**Step 1: Add transient toast for sync errors**

```kotlin
LaunchedEffect(lastSyncError) { delay(3000); hide }
```

**Step 2: Reduce editor font size**

```kotlin
TextField(textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp))
```

**Step 3: Commit**

```bash
git add mobile/shared/src/commonMain/kotlin/nondh/shared/ui/NotesScreen.kt
git commit -m "feat(ui): add error toast and smaller editor font"
```
