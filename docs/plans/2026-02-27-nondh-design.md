# Nondh Design

Date: 2026-02-27

## Goal
Build a self-hosted, minimal text-only note app for close family. It should sync across Android and iOS, work offline, and run on small hardware via Docker. Initial scope is private network only, with a path to public hosting later.

## Requirements
- Minimal, clean UI.
- Cross-platform: Android and iOS (web later).
- Self-hosted with small footprint.
- Text notes only for MVP.
- Offline-first with sync when back on network.
- Simple conflict handling: last-write-wins.
- Encryption can be added later.

## Recommended Approach
Kotlin Multiplatform Mobile (KMM) with Compose Multiplatform for shared UI where stable, backed by a Go server with SQLite. If Compose Multiplatform on iOS proves unstable, fall back to SwiftUI for specific screens while keeping shared logic in KMM.

## Architecture
- Mobile shared module (KMM):
  - Data models, sync engine, API client.
  - Local database access (SQLDelight recommended).
  - Shared UI via Compose Multiplatform when feasible.
- Android app:
  - Jetpack Compose UI and platform glue (permissions, background sync).
- iOS app:
  - Compose Multiplatform UI where stable; fallback to SwiftUI as needed.
  - Platform glue for background sync.
- Backend (Go):
  - HTTP API, SQLite storage.
  - Simple token auth even for LAN.
  - Docker deployment.

## Data Flow
1. User creates/edits a note locally.
2. Change is stored in local DB and appended to a sync queue.
3. Background sync runs when network is available.
4. Client posts changes to server; server returns changes since last sync.
5. Client applies remote changes using last-write-wins on updated_at.

## Error Handling
- Offline edits always succeed locally.
- Sync retries with exponential backoff.
- Conflicts resolved with last-write-wins (no UI merge in MVP).
- Server unavailable: queue retained until reconnect.

## Testing
- Shared module: unit tests for sync and conflict logic.
- Backend: API integration tests, storage tests.
- Mobile: smoke tests for create/edit/sync in emulator/simulator.

## Future Scope
- Public hosting configuration and TLS.
- End-to-end encryption.
- Rich content (images/files).
- Web client.
- Smarter conflict resolution.
