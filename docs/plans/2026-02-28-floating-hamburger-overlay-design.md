# Floating Hamburger Overlay Design

Date: 2026-02-28

## Goal
Provide a floating hamburger menu control that overlays the full-screen editor, fades out while typing, and reappears after a brief idle period or on tap. This gives quick drawer access without reducing editor space.

## Non-Goals
- No top app bar.
- No scroll-aware logic or complex gesture detection beyond basic tap.
- No changes to sync behavior or FAB speed-dial in this change.

## Interaction Design
- The hamburger icon is visible by default.
- When the user types in the editor, the icon hides (fades out).
- After 1.5 seconds of no typing, the icon fades back in.
- A tap anywhere on the editor brings the icon back immediately.

## UI Placement and Styling
- Position: top-left overlay with 12-16dp padding.
- Visual: small circular surface with subtle elevation and a menu icon.
- Theme: uses `MaterialTheme` surface and on-surface colors.
- Animation: alpha-based fade using Compose animation APIs.

## Implementation Outline
- Track `menuVisible` local state inside the editor content.
- On `onValueChange`:
  - set `menuVisible = false`
  - restart a debounce job; after 1500ms of inactivity set `menuVisible = true`
- Add a tap handler on the editor container to set `menuVisible = true`.
- Change menu icon action to open the `ModalNavigationDrawer` instead of sync.

## Testing
- Manual: type in editor and confirm icon fades out.
- Pause typing; confirm icon returns after 1.5s.
- Tap editor; confirm icon appears immediately.
- Tap icon; confirm drawer opens.

