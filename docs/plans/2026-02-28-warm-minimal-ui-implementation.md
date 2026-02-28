# Warm Minimal UI Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Apply warm minimal styling with Merriweather/Lora typography to the editor-only UI.

**Architecture:** Add font resources, define a Material3 theme with warm palette, and apply typography + colors to editor, drawer, FAB, and action sheet.

**Tech Stack:** Compose Multiplatform, Material3, Android resources.

---

### Task 1: Add fonts and theme

**Files:**
- Create: `mobile/androidApp/src/main/res/font/merriweather_regular.ttf`
- Create: `mobile/androidApp/src/main/res/font/merriweather_semibold.ttf`
- Create: `mobile/androidApp/src/main/res/font/lora_regular.ttf`
- Create: `mobile/shared/src/commonMain/kotlin/nondh/shared/ui/theme/Theme.kt`
- Create: `mobile/shared/src/commonMain/kotlin/nondh/shared/ui/theme/Type.kt`
- Create: `mobile/shared/src/commonMain/kotlin/nondh/shared/ui/theme/Color.kt`
- Modify: `mobile/shared/src/commonMain/kotlin/nondh/shared/ui/NotesScreen.kt`

**Step 1: Add font resources**

Place the font TTFs in Android res/font and register in theme.

**Step 2: Define color palette**

```kotlin
val WarmBackground = Color(0xFFF7F2EC)
val WarmSurface = Color(0xFFFFF9F3)
val WarmPrimary = Color(0xFFC07A4F)
val WarmOnPrimary = Color(0xFFFFFFFF)
val WarmOnSurface = Color(0xFF2B2621)
val WarmOnSurfaceVariant = Color(0xFF6D6258)
val WarmError = Color(0xFFB5463A)
```

**Step 3: Define typography**

- Title/labels: Merriweather
- Body/editor: Lora

**Step 4: Apply theme**

Wrap `NotesScreen` content in `NondhTheme` and update TextField colors.

**Step 5: Commit**

```bash
git add mobile/androidApp/src/main/res/font/*.ttf mobile/shared/src/commonMain/kotlin/nondh/shared/ui/theme/*.kt mobile/shared/src/commonMain/kotlin/nondh/shared/ui/NotesScreen.kt
git commit -m "feat(ui): add warm minimal theme"
```

### Task 2: Style editor + drawer + FAB

**Files:**
- Modify: `mobile/shared/src/commonMain/kotlin/nondh/shared/ui/NotesScreen.kt`

**Step 1: Editor styling**

- Use warm surface and no borders.
- Increase padding and font size slightly.

**Step 2: Drawer styling**

- Muted separators, secondary text color.

**Step 3: FAB styling**

- Accent color, small shadow.

**Step 4: Commit**

```bash
git add mobile/shared/src/commonMain/kotlin/nondh/shared/ui/NotesScreen.kt
git commit -m "feat(ui): style editor drawer fab"
```
