# Obsidian-Style App Bar + Save/Type Tweaks

Date: 2026-02-28

## Goal
Improve the editor UI to feel Obsidian-like: a slim blended app bar with a hamburger icon, a minimal drawer list, and snappier local saves. Reduce text sizes for a cleaner reading density.

## Non-Goals
- No new features beyond UI/layout and save timing changes.
- No changes to sync protocol or backend.
- No new note creation flow in the drawer.

## UI Design
### App Bar
- Height: 48dp, plus status bar inset (`statusBarsPadding`).
- Same background color as the editor surface, no elevation/shadow.
- Left: hamburger icon; no title.
- App bar blends with the editor background (Obsidian-like).

### Editor Layout
- Editor content starts below the app bar, so text never collides with the menu icon.
- Editor text size: 12sp.

### Drawer
- Remove the "Notes" header and "New" button.
- Drawer shows only the list of notes.

### Typography
- Note list body: 12sp.
- Note list metadata (timestamp): 10sp.

## Save Behavior
- Save to local DB immediately on each text update.
- Debounce only sync (1–2s), so typing feels instant while still batching network sync.

## Error Handling
- No changes; existing sync error toast remains.

## Testing
- Manual:
  - Confirm hamburger sits in a blended app bar and doesn’t overlap the editor.
  - Confirm drawer list shows only notes.
  - Confirm editor text size is reduced.
  - Confirm notes list text size is reduced.
  - Confirm local saves feel instant; sync still occurs after short delay.

