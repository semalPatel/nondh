# Warm Minimal UI Design

Date: 2026-02-28

## Goal
Apply a warm, minimal visual style with serif typography to the editor‑only UI, keeping the editor dominant and secondary UI subtle.

## Requirements
- Editor remains full-screen.
- Drawer and FAB remain secondary.
- Use serif-leaning typography.
- Warm, soft palette.

## Typography
- Headings: Merriweather (semi-bold)
- Body/editor: Lora (regular)

## Palette
- Background: #F7F2EC
- Surface: #FFF9F3
- Primary text: #2B2621
- Secondary text: #6D6258
- Accent: #C07A4F
- Error: #B5463A

## Components
- Editor: borderless, padded, soft surface.
- Drawer: full-screen list with muted separators.
- FAB: accent color, subtle elevation.
- Actions sheet: warm surface, full-width buttons.
- Sync status strip: small, secondary text; error in accent error color.

## Layout
- Keep the editor as the main content.
- Drawer list and action sheet use the same warm surface.
- Minimal top controls (hamburger + sync).

## Motion
- Use default Material transitions for drawer and sheets.

## Testing
- Visual smoke on Android emulator.
- Ensure contrast is readable.
