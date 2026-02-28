# Editor-Only UI Design

Date: 2026-02-28

## Goal
Make the home screen a full-screen editor only. Notes list and other actions are secondary and accessible via a hamburger menu (drawer) and FAB quick actions.

## Requirements
- Editor occupies the entire screen.
- Notes list is secondary via hamburger menu (drawer).
- FAB opens quick actions (New, Sync, Settings).
- Autosave remains debounced.
- Clean, modern minimal UI.

## Recommended Approach
Use `ModalNavigationDrawer` for a full-screen list drawer and a FAB to open a small actions sheet. Keep the editor as the only main content.

## Architecture
- **Editor-only screen**: single full-screen text field.
- **Drawer**: full-screen list of notes; selecting a note loads it into editor.
- **FAB**: opens a bottom sheet with actions (New, Sync, Settings).
- **Autosave**: same debounce logic in ViewModel.

## Components
- Editor: `TextField` filling screen.
- Drawer: note list with titles/snippets.
- Quick actions sheet: New note, Sync now, Settings.

## Data Flow
1. Typing updates draft.
2. Debounce save and sync.
3. Drawer selection switches note.
4. FAB actions trigger new note, sync, or settings.

## Error Handling
- Sync errors appear as subtle text at top of editor.
- No blocking dialogs.

## Testing
- Autosave debounce behavior.
- Drawer selection loads correct note.
- FAB actions wired to ViewModel.

## Future Scope
- Search in drawer.
- Pin/favorite notes.
- Rich text.
