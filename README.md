# nondh

Self-hosted, family-friendly, minimal note-taking app with offline-first sync. Text-only for MVP.

## What It Is
- **Mobile-first**: Kotlin Multiplatform shared logic, Compose Multiplatform UI, Android + iOS.
- **Self-hosted backend**: Go + SQLite, tiny footprint, Docker-friendly.
- **Offline-first**: Notes saved locally and synced when the server is reachable.
- **Simple auth**: One shared token for the family (MVP).

## Architecture (MVP)
- **Backend**: Go HTTP API, SQLite storage, token auth.
- **Mobile shared**: KMM models, SQLDelight DB, sync engine, Ktor client.
- **UI**: Compose Multiplatform for shared screens. Platform glue for lifecycle + sync.

## Quickstart (Backend)

1. Create a token (any random string).
2. Create `.env` from example and set the token.

```bash
cp .env.example .env
# edit .env and set NONDH_TOKEN
```

3. Start the server:

```bash
docker compose up --build
```

Server runs on port `8080` by default.

## Token (ELI5)
A token is just a **shared password string**:
- You create it.
- The server requires it on every request.
- The app sends it with each API call.

If the app and server tokens don’t match, sync fails.

## Mobile Setup

### Android (Emulator)
- Base URL: `http://10.0.2.2:8080`
- Token: same as in `.env`

### iOS Simulator
- Base URL: `http://127.0.0.1:8080`
- Token: same as in `.env`

### Real Devices (Android / iOS)
- Base URL: `http://<your-lan-ip>:8080`
- Token: same as in `.env`

### App Settings Screen
Use the in-app **Settings** screen to set Base URL and Token. These values are persisted locally.

## Build / Test

Backend:
```bash
cd backend
go test ./...
```

Mobile shared module:
```bash
cd mobile
./gradlew :shared:allTests
```

If Android build fails with SDK errors, set SDK path:
```bash
cd mobile
cat <<EOF > local.properties
sdk.dir=/Users/corrupt/Library/Android/sdk
EOF
```

## Sync Behavior (MVP)
- Local edits are queued and pushed to server.
- Server returns changes since last sync.
- **Last-write-wins** based on `updated_at` (epoch millis).
- Deletes use **tombstones** (`deletedAt`), and the client hides deleted notes.

## Docker Notes
- Data is stored in `./data` on the host (bind-mounted into the container).
- Environment variables:
  - `NONDH_TOKEN`: shared secret for API access
  - `NONDH_PORT`: exposed port (default `8080`)

## Known Limitations
- No user accounts yet (shared token only).
- No end-to-end encryption yet.
- iOS tests may be slow/hang in some environments.

## Roadmap Ideas
- User accounts + sharing
- End-to-end encryption
- Rich content (images/files)
- Web client
- Smarter conflict resolution
