# Nondh Editor-First UI Design

Date: 2026-02-28

## Goal
Make the mobile UI modern and editor-first: the editor occupies most of the screen, note list is a bottom sheet, and Settings is accessed via a floating action button. Auto-save edits with a short debounce.

## Requirements
- Editor dominates the screen (single-pane).
- Notes list in a collapsible bottom sheet.
- Settings access via FAB overlay.
- Auto-save on pause (debounced), and sync after save.
- Minimal, clean aesthetic.

## Recommended Approach
Use a single Compose Multiplatform screen with:
- Full-height editor content area.
- Material 3 bottom sheet for the notes list.
- Floating Action Button to open Settings modal.
- Debounced auto-save using coroutines in the ViewModel.

## Architecture
- **UI**: `NotesScreen` becomes editor-first with a bottom sheet list and FAB.
- **State**: add editor focus and sheet open state; maintain `draftText` and `selectedId`.
- **Auto-save**: ViewModel holds a debounced job; on draft updates, schedule save and sync.
- **Sync**: keep current sync flow; trigger sync after auto-save.

## Components
- **Editor**: large multiline `TextField` (or `OutlinedTextField`) filling screen.
- **Bottom sheet list**: shows notes; tap to switch current note.
- **FAB**: opens Settings (existing Settings screen reused).
- **Status strip**: small text for last sync / errors.

## Data Flow
1. User types → `draftText` updates.
2. Debounce timer fires → ViewModel saves draft to DB and queues sync.
3. Sync runs in background, updates status.
4. List shows current notes via DB.

## Error Handling
- Sync errors are shown as non-blocking text.
- Editor always updates local DB even if sync fails.

## Testing
- ViewModel debounce unit test (save after pause).
- ViewModel save triggers sync status update.
- UI smoke test (bottom sheet visible, FAB opens settings).

## Future Scope
- Rich text editor.
- Realtime collaborative editing.
- Smarter conflict resolution UI.
