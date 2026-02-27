# Nondh MVP Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Build the first working slice of Nondh with a Go backend, KMM shared logic, and Compose Multiplatform UI for basic text notes sync.

**Architecture:** Go HTTP API with SQLite storage and token auth; Kotlin Multiplatform shared module for models, local DB, sync engine, and API client; Compose Multiplatform UI for shared screens with platform glue for Android/iOS.

**Tech Stack:** Go, SQLite, Docker, Kotlin Multiplatform, Compose Multiplatform, SQLDelight, Kotlinx Serialization, Ktor client.

---

### Task 1: Scaffold backend module with health endpoint

**Files:**
- Create: `backend/go.mod`
- Create: `backend/cmd/nondh-server/main.go`
- Create: `backend/internal/http/router.go`
- Create: `backend/internal/http/handlers/health.go`
- Create: `backend/internal/http/handlers/health_test.go`

**Step 1: Write the failing test**

```go
package handlers

import (
    "net/http"
    "net/http/httptest"
    "testing"
)

func TestHealthHandler(t *testing.T) {
    req := httptest.NewRequest(http.MethodGet, "/health", nil)
    rr := httptest.NewRecorder()

    HealthHandler(rr, req)

    if rr.Code != http.StatusOK {
        t.Fatalf("expected 200, got %d", rr.Code)
    }
    if body := rr.Body.String(); body != "ok" {
        t.Fatalf("expected body 'ok', got %q", body)
    }
}
```

**Step 2: Run test to verify it fails**

Run: `cd backend && go test ./...`
Expected: FAIL with "undefined: HealthHandler"

**Step 3: Write minimal implementation**

```go
package handlers

import "net/http"

func HealthHandler(w http.ResponseWriter, _ *http.Request) {
    w.WriteHeader(http.StatusOK)
    _, _ = w.Write([]byte("ok"))
}
```

**Step 4: Wire up router and main**

```go
// router.go
package http

import (
    "net/http"

    "nondh/internal/http/handlers"
)

func Router() http.Handler {
    mux := http.NewServeMux()
    mux.HandleFunc("/health", handlers.HealthHandler)
    return mux
}
```

```go
// main.go
package main

import (
    "log"
    "net/http"

    apphttp "nondh/internal/http"
)

func main() {
    srv := &http.Server{
        Addr:    ":8080",
        Handler: apphttp.Router(),
    }
    log.Fatal(srv.ListenAndServe())
}
```

**Step 5: Run tests and commit**

Run: `cd backend && go test ./...`
Expected: PASS

```bash
git add backend/go.mod backend/cmd/nondh-server/main.go backend/internal/http/router.go backend/internal/http/handlers/health.go backend/internal/http/handlers/health_test.go
git commit -m "feat(backend): add health endpoint"
```

### Task 2: Add notes model and SQLite store

**Files:**
- Create: `backend/internal/model/note.go`
- Create: `backend/internal/store/sqlite.go`
- Create: `backend/internal/store/notes.go`
- Create: `backend/internal/store/notes_test.go`

**Step 1: Write the failing store test**

```go
package store

import (
    "testing"
    "time"
)

func TestNotesCRUD(t *testing.T) {
    db, cleanup := mustTestDB(t)
    defer cleanup()

    n := Note{
        ID:        "note-1",
        Title:     "",
        Body:      "hello",
        UpdatedAt: time.Unix(1000, 0).UTC(),
    }

    if err := db.UpsertNote(n); err != nil {
        t.Fatalf("upsert failed: %v", err)
    }

    got, err := db.GetNote("note-1")
    if err != nil {
        t.Fatalf("get failed: %v", err)
    }
    if got.Body != "hello" {
        t.Fatalf("expected body 'hello', got %q", got.Body)
    }
}
```

**Step 2: Run test to verify it fails**

Run: `cd backend && go test ./...`
Expected: FAIL with "undefined: Note" or missing methods

**Step 3: Implement model and store**

```go
// model/note.go
package model

import "time"

type Note struct {
    ID        string
    Title     string
    Body      string
    UpdatedAt time.Time
}
```

```go
// store/sqlite.go
package store

import (
    "database/sql"
    "testing"

    _ "modernc.org/sqlite"
)

func Open(path string) (*DB, error) {
    db, err := sql.Open("sqlite", path)
    if err != nil {
        return nil, err
    }
    if _, err := db.Exec(`
        CREATE TABLE IF NOT EXISTS notes (
            id TEXT PRIMARY KEY,
            title TEXT NOT NULL,
            body TEXT NOT NULL,
            updated_at INTEGER NOT NULL
        )
    `); err != nil {
        _ = db.Close()
        return nil, err
    }
    return &DB{db: db}, nil
}

type DB struct {
    db *sql.DB
}

func mustTestDB(t *testing.T) (*DB, func()) {
    t.Helper()
    db, err := Open(":memory:")
    if err != nil {
        t.Fatalf("open db: %v", err)
    }
    return db, func() { _ = db.db.Close() }
}
```

```go
// store/notes.go
package store

import (
    "database/sql"
    "time"

    "nondh/internal/model"
)

type Note = model.Note

func (d *DB) UpsertNote(n Note) error {
    _, err := d.db.Exec(`
        INSERT INTO notes (id, title, body, updated_at)
        VALUES (?, ?, ?, ?)
        ON CONFLICT(id) DO UPDATE SET
            title=excluded.title,
            body=excluded.body,
            updated_at=excluded.updated_at
    `, n.ID, n.Title, n.Body, n.UpdatedAt.Unix())
    return err
}

func (d *DB) GetNote(id string) (Note, error) {
    var n Note
    var ts int64
    err := d.db.QueryRow(`SELECT id, title, body, updated_at FROM notes WHERE id=?`, id).
        Scan(&n.ID, &n.Title, &n.Body, &ts)
    if err != nil {
        return Note{}, err
    }
    n.UpdatedAt = time.Unix(ts, 0).UTC()
    return n, nil
}

func (d *DB) NotesSince(ts time.Time) ([]Note, error) {
    rows, err := d.db.Query(`SELECT id, title, body, updated_at FROM notes WHERE updated_at > ? ORDER BY updated_at ASC`, ts.Unix())
    if err != nil {
        return nil, err
    }
    defer rows.Close()

    var out []Note
    for rows.Next() {
        var n Note
        var tsv int64
        if err := rows.Scan(&n.ID, &n.Title, &n.Body, &tsv); err != nil {
            return nil, err
        }
        n.UpdatedAt = time.Unix(tsv, 0).UTC()
        out = append(out, n)
    }
    return out, rows.Err()
}
```

**Step 4: Run tests and commit**

Run: `cd backend && go test ./...`
Expected: PASS

```bash
git add backend/internal/model/note.go backend/internal/store/sqlite.go backend/internal/store/notes.go backend/internal/store/notes_test.go
git commit -m "feat(backend): add note store"
```

### Task 3: Add notes API endpoints

**Files:**
- Create: `backend/internal/http/handlers/notes.go`
- Create: `backend/internal/http/handlers/notes_test.go`
- Modify: `backend/internal/http/router.go`

**Step 1: Write failing handler test**

```go
package handlers

import (
    "bytes"
    "encoding/json"
    "net/http"
    "net/http/httptest"
    "testing"
    "time"

    apphttp "nondh/internal/http"
    "nondh/internal/store"
)

type notePayload struct {
    ID        string `json:"id"`
    Title     string `json:"title"`
    Body      string `json:"body"`
    UpdatedAt int64  `json:"updated_at"`
}

func TestUpsertAndListNotes(t *testing.T) {
    db, cleanup := store.MustTestDB(t)
    defer cleanup()

    h := NewNotesHandler(db)
    router := apphttp.RouterWithNotes(h)

    payload := notePayload{ID: "n1", Title: "", Body: "hello", UpdatedAt: time.Now().Unix()}
    body, _ := json.Marshal(payload)
    req := httptest.NewRequest(http.MethodPost, "/notes", bytes.NewReader(body))
    rr := httptest.NewRecorder()
    router.ServeHTTP(rr, req)
    if rr.Code != http.StatusCreated {
        t.Fatalf("expected 201, got %d", rr.Code)
    }

    listReq := httptest.NewRequest(http.MethodGet, "/notes?since=0", nil)
    listRR := httptest.NewRecorder()
    router.ServeHTTP(listRR, listReq)
    if listRR.Code != http.StatusOK {
        t.Fatalf("expected 200, got %d", listRR.Code)
    }
}
```

**Step 2: Run tests to verify failure**

Run: `cd backend && go test ./...`
Expected: FAIL with missing handler/router

**Step 3: Implement handlers and router**

```go
// handlers/notes.go
package handlers

import (
    "encoding/json"
    "net/http"
    "strconv"
    "time"

    "nondh/internal/model"
    "nondh/internal/store"
)

type NotesHandler struct {
    db *store.DB
}

type notePayload struct {
    ID        string `json:"id"`
    Title     string `json:"title"`
    Body      string `json:"body"`
    UpdatedAt int64  `json:"updated_at"`
}

func NewNotesHandler(db *store.DB) *NotesHandler {
    return &NotesHandler{db: db}
}

func (h *NotesHandler) Upsert(w http.ResponseWriter, r *http.Request) {
    var p notePayload
    if err := json.NewDecoder(r.Body).Decode(&p); err != nil {
        http.Error(w, "bad json", http.StatusBadRequest)
        return
    }
    n := model.Note{ID: p.ID, Title: p.Title, Body: p.Body, UpdatedAt: time.Unix(p.UpdatedAt, 0).UTC()}
    if err := h.db.UpsertNote(n); err != nil {
        http.Error(w, "store error", http.StatusInternalServerError)
        return
    }
    w.WriteHeader(http.StatusCreated)
}

func (h *NotesHandler) List(w http.ResponseWriter, r *http.Request) {
    sinceStr := r.URL.Query().Get("since")
    since, _ := strconv.ParseInt(sinceStr, 10, 64)
    notes, err := h.db.NotesSince(time.Unix(since, 0).UTC())
    if err != nil {
        http.Error(w, "store error", http.StatusInternalServerError)
        return
    }
    var out []notePayload
    for _, n := range notes {
        out = append(out, notePayload{ID: n.ID, Title: n.Title, Body: n.Body, UpdatedAt: n.UpdatedAt.Unix()})
    }
    _ = json.NewEncoder(w).Encode(out)
}
```

```go
// router.go
package http

import (
    "net/http"

    "nondh/internal/http/handlers"
)

func Router() http.Handler {
    mux := http.NewServeMux()
    mux.HandleFunc("/health", handlers.HealthHandler)
    return mux
}

func RouterWithNotes(h *handlers.NotesHandler) http.Handler {
    mux := http.NewServeMux()
    mux.HandleFunc("/health", handlers.HealthHandler)
    mux.HandleFunc("/notes", func(w http.ResponseWriter, r *http.Request) {
        if r.Method == http.MethodPost {
            h.Upsert(w, r)
            return
        }
        if r.Method == http.MethodGet {
            h.List(w, r)
            return
        }
        w.WriteHeader(http.StatusMethodNotAllowed)
    })
    return mux
}
```

**Step 4: Run tests and commit**

Run: `cd backend && go test ./...`
Expected: PASS

```bash
git add backend/internal/http/handlers/notes.go backend/internal/http/handlers/notes_test.go backend/internal/http/router.go
git commit -m "feat(backend): add notes API"
```

### Task 4: Add simple token auth middleware

**Files:**
- Create: `backend/internal/auth/token.go`
- Create: `backend/internal/auth/token_test.go`
- Modify: `backend/internal/http/router.go`

**Step 1: Write failing middleware test**

```go
package auth

import (
    "net/http"
    "net/http/httptest"
    "testing"
)

func TestTokenAuth(t *testing.T) {
    h := TokenMiddleware("secret", http.HandlerFunc(func(w http.ResponseWriter, _ *http.Request) {
        w.WriteHeader(http.StatusOK)
    }))

    req := httptest.NewRequest(http.MethodGet, "/notes", nil)
    rr := httptest.NewRecorder()
    h.ServeHTTP(rr, req)
    if rr.Code != http.StatusUnauthorized {
        t.Fatalf("expected 401, got %d", rr.Code)
    }

    req2 := httptest.NewRequest(http.MethodGet, "/notes", nil)
    req2.Header.Set("Authorization", "Bearer secret")
    rr2 := httptest.NewRecorder()
    h.ServeHTTP(rr2, req2)
    if rr2.Code != http.StatusOK {
        t.Fatalf("expected 200, got %d", rr2.Code)
    }
}
```

**Step 2: Run tests to verify failure**

Run: `cd backend && go test ./...`
Expected: FAIL with missing middleware

**Step 3: Implement middleware and wire it**

```go
package auth

import (
    "net/http"
    "strings"
)

func TokenMiddleware(token string, next http.Handler) http.Handler {
    return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        auth := r.Header.Get("Authorization")
        if !strings.HasPrefix(auth, "Bearer ") {
            w.WriteHeader(http.StatusUnauthorized)
            return
        }
        if strings.TrimPrefix(auth, "Bearer ") != token {
            w.WriteHeader(http.StatusUnauthorized)
            return
        }
        next.ServeHTTP(w, r)
    })
}
```

```go
// router.go
package http

import (
    "net/http"

    "nondh/internal/auth"
    "nondh/internal/http/handlers"
)

func RouterWithNotesAndAuth(h *handlers.NotesHandler, token string) http.Handler {
    mux := http.NewServeMux()
    mux.HandleFunc("/health", handlers.HealthHandler)
    protected := auth.TokenMiddleware(token, http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        if r.Method == http.MethodPost {
            h.Upsert(w, r)
            return
        }
        if r.Method == http.MethodGet {
            h.List(w, r)
            return
        }
        w.WriteHeader(http.StatusMethodNotAllowed)
    }))
    mux.Handle("/notes", protected)
    return mux
}
```

**Step 4: Run tests and commit**

Run: `cd backend && go test ./...`
Expected: PASS

```bash
git add backend/internal/auth/token.go backend/internal/auth/token_test.go backend/internal/http/router.go
git commit -m "feat(backend): add token auth"
```

### Task 5: Add Docker support

**Files:**
- Create: `backend/Dockerfile`
- Create: `backend/.dockerignore`

**Step 1: Write Dockerfile**

```Dockerfile
FROM golang:1.22-alpine AS build
WORKDIR /src
COPY go.mod go.sum ./
RUN go mod download
COPY . .
RUN CGO_ENABLED=0 go build -o /bin/nondh ./cmd/nondh-server

FROM alpine:3.19
WORKDIR /app
COPY --from=build /bin/nondh /app/nondh
EXPOSE 8080
ENTRYPOINT ["/app/nondh"]
```

**Step 2: Add .dockerignore**

```
.git
.worktrees
**/tmp
```

**Step 3: Commit**

```bash
git add backend/Dockerfile backend/.dockerignore
git commit -m "chore(backend): add dockerfile"
```

### Task 6: Scaffold Kotlin Multiplatform project

**Files:**
- Create: `mobile/settings.gradle.kts`
- Create: `mobile/build.gradle.kts`
- Create: `mobile/shared/build.gradle.kts`
- Create: `mobile/shared/src/commonMain/kotlin/nondh/shared/NotesModels.kt`
- Create: `mobile/shared/src/commonTest/kotlin/nondh/shared/NotesModelsTest.kt`

**Step 1: Write failing test**

```kotlin
package nondh.shared

import kotlin.test.Test
import kotlin.test.assertEquals

class NotesModelsTest {
    @Test
    fun noteDefaultsToEmptyTitle() {
        val note = Note(id = "n1", title = "", body = "hi", updatedAt = 1000)
        assertEquals("", note.title)
    }
}
```

**Step 2: Run test to verify it fails**

Run: `cd mobile && ./gradlew :shared:test`
Expected: FAIL with "Unresolved reference: Note"

**Step 3: Implement model**

```kotlin
package nondh.shared

import kotlinx.serialization.Serializable

@Serializable
data class Note(
    val id: String,
    val title: String,
    val body: String,
    val updatedAt: Long
)
```

**Step 4: Run tests and commit**

Run: `cd mobile && ./gradlew :shared:test`
Expected: PASS

```bash
git add mobile/settings.gradle.kts mobile/build.gradle.kts mobile/shared/build.gradle.kts mobile/shared/src/commonMain/kotlin/nondh/shared/NotesModels.kt mobile/shared/src/commonTest/kotlin/nondh/shared/NotesModelsTest.kt
git commit -m "feat(shared): add note model"
```

### Task 7: Add local database with SQLDelight

**Files:**
- Create: `mobile/shared/src/commonMain/sqldelight/nondh/notes.sq`
- Create: `mobile/shared/src/commonMain/kotlin/nondh/shared/db/NotesDb.kt`
- Create: `mobile/shared/src/commonTest/kotlin/nondh/shared/db/NotesDbTest.kt`

**Step 1: Write failing test**

```kotlin
package nondh.shared.db

import kotlin.test.Test
import kotlin.test.assertEquals
import nondh.shared.Note

class NotesDbTest {
    @Test
    fun insertAndReadNote() {
        val db = InMemoryNotesDb()
        val note = Note("n1", "", "hello", 1000)
        db.upsert(note)
        val got = db.get("n1")
        assertEquals("hello", got?.body)
    }
}
```

**Step 2: Run test to verify it fails**

Run: `cd mobile && ./gradlew :shared:test`
Expected: FAIL with "Unresolved reference: InMemoryNotesDb"

**Step 3: Implement DB wrapper (in-memory for test) and SQLDelight schema**

```sql
-- notes.sq
CREATE TABLE notes (
  id TEXT NOT NULL PRIMARY KEY,
  title TEXT NOT NULL,
  body TEXT NOT NULL,
  updated_at INTEGER NOT NULL
);

selectById:
SELECT * FROM notes WHERE id = ?;

selectSince:
SELECT * FROM notes WHERE updated_at > ? ORDER BY updated_at ASC;

upsert:
INSERT INTO notes(id, title, body, updated_at)
VALUES (?, ?, ?, ?)
ON CONFLICT(id) DO UPDATE SET
  title=excluded.title,
  body=excluded.body,
  updated_at=excluded.updated_at;
```

```kotlin
package nondh.shared.db

import nondh.shared.Note

interface NotesDb {
    fun upsert(note: Note)
    fun get(id: String): Note?
    fun since(ts: Long): List<Note>
}

class InMemoryNotesDb : NotesDb {
    private val data = mutableMapOf<String, Note>()

    override fun upsert(note: Note) { data[note.id] = note }
    override fun get(id: String): Note? = data[id]
    override fun since(ts: Long): List<Note> = data.values.filter { it.updatedAt > ts }.sortedBy { it.updatedAt }
}
```

**Step 4: Run tests and commit**

Run: `cd mobile && ./gradlew :shared:test`
Expected: PASS

```bash
git add mobile/shared/src/commonMain/sqldelight/nondh/notes.sq mobile/shared/src/commonMain/kotlin/nondh/shared/db/NotesDb.kt mobile/shared/src/commonTest/kotlin/nondh/shared/db/NotesDbTest.kt
git commit -m "feat(shared): add notes db abstraction"
```

### Task 8: Implement sync engine (last-write-wins)

**Files:**
- Create: `mobile/shared/src/commonMain/kotlin/nondh/shared/sync/SyncEngine.kt`
- Create: `mobile/shared/src/commonTest/kotlin/nondh/shared/sync/SyncEngineTest.kt`

**Step 1: Write failing test**

```kotlin
package nondh.shared.sync

import kotlin.test.Test
import kotlin.test.assertEquals
import nondh.shared.Note
import nondh.shared.db.InMemoryNotesDb

class SyncEngineTest {
    @Test
    fun newerNoteWins() {
        val db = InMemoryNotesDb()
        val engine = SyncEngine(db)

        db.upsert(Note("n1", "", "old", 1000))
        engine.applyRemote(listOf(Note("n1", "", "new", 2000)))

        assertEquals("new", db.get("n1")?.body)
    }
}
```

**Step 2: Run test to verify it fails**

Run: `cd mobile && ./gradlew :shared:test`
Expected: FAIL with "Unresolved reference: SyncEngine"

**Step 3: Implement minimal sync engine**

```kotlin
package nondh.shared.sync

import nondh.shared.Note
import nondh.shared.db.NotesDb

class SyncEngine(private val db: NotesDb) {
    fun applyRemote(notes: List<Note>) {
        notes.forEach { incoming ->
            val current = db.get(incoming.id)
            if (current == null || incoming.updatedAt >= current.updatedAt) {
                db.upsert(incoming)
            }
        }
    }
}
```

**Step 4: Run tests and commit**

Run: `cd mobile && ./gradlew :shared:test`
Expected: PASS

```bash
git add mobile/shared/src/commonMain/kotlin/nondh/shared/sync/SyncEngine.kt mobile/shared/src/commonTest/kotlin/nondh/shared/sync/SyncEngineTest.kt
git commit -m "feat(shared): add sync engine"
```

### Task 9: Shared Compose Multiplatform UI state

**Files:**
- Create: `mobile/shared/src/commonMain/kotlin/nondh/shared/ui/NotesState.kt`
- Create: `mobile/shared/src/commonMain/kotlin/nondh/shared/ui/NotesViewModel.kt`
- Create: `mobile/shared/src/commonTest/kotlin/nondh/shared/ui/NotesViewModelTest.kt`

**Step 1: Write failing test**

```kotlin
package nondh.shared.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import nondh.shared.Note

class NotesViewModelTest {
    @Test
    fun addNoteAddsToList() {
        val vm = NotesViewModel()
        vm.addNote(Note("n1", "", "hello", 1000))
        assertEquals(1, vm.state.notes.size)
    }
}
```

**Step 2: Run test to verify it fails**

Run: `cd mobile && ./gradlew :shared:test`
Expected: FAIL with "Unresolved reference: NotesViewModel"

**Step 3: Implement minimal state and view model**

```kotlin
package nondh.shared.ui

import nondh.shared.Note

data class NotesState(
    val notes: List<Note> = emptyList()
)

class NotesViewModel {
    var state: NotesState = NotesState()
        private set

    fun addNote(note: Note) {
        state = state.copy(notes = state.notes + note)
    }
}
```

**Step 4: Run tests and commit**

Run: `cd mobile && ./gradlew :shared:test`
Expected: PASS

```bash
git add mobile/shared/src/commonMain/kotlin/nondh/shared/ui/NotesState.kt mobile/shared/src/commonMain/kotlin/nondh/shared/ui/NotesViewModel.kt mobile/shared/src/commonTest/kotlin/nondh/shared/ui/NotesViewModelTest.kt
git commit -m "feat(shared): add notes UI state"
```

### Task 10: Compose Multiplatform notes screen (shared UI)

**Files:**
- Create: `mobile/shared/src/commonMain/kotlin/nondh/shared/ui/NotesScreen.kt`

**Step 1: Implement NotesScreen**

```kotlin
package nondh.shared.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun NotesScreen(state: NotesState, onAdd: (String) -> Unit) {
    var text by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            TextField(
                modifier = Modifier.weight(1f),
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("New note") }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { if (text.isNotBlank()) { onAdd(text); text = "" } }) {
                Text("Add")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn {
            items(state.notes) { note ->
                Text(note.body, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
```

**Step 2: Commit**

```bash
git add mobile/shared/src/commonMain/kotlin/nondh/shared/ui/NotesScreen.kt
git commit -m "feat(shared): add notes screen"
```

### Task 11: Platform entry points

**Files:**
- Create: `mobile/androidApp/src/main/java/nondh/android/MainActivity.kt`
- Create: `mobile/iosApp/iosApp/ContentView.swift`

**Step 1: Wire Android to NotesScreen**

```kotlin
package nondh.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import nondh.shared.Note
import nondh.shared.ui.NotesScreen
import nondh.shared.ui.NotesState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var state = NotesState()
            NotesScreen(state = state, onAdd = { body ->
                state = state.copy(notes = state.notes + Note("local-${state.notes.size}", "", body, System.currentTimeMillis()))
            })
        }
    }
}
```

**Step 2: Wire iOS to NotesScreen (Compose Multiplatform)**

```swift
import SwiftUI
import shared

struct ContentView: View {
    var body: some View {
        ComposeView()
    }
}

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainKt.NotesViewController()
    }
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
```

**Step 3: Commit**

```bash
git add mobile/androidApp/src/main/java/nondh/android/MainActivity.kt mobile/iosApp/iosApp/ContentView.swift
git commit -m "feat(mobile): wire platform entry points"
```

---

If any Compose Multiplatform iOS blockers appear, pivot the iOS UI for that screen to SwiftUI while keeping shared models and sync logic.
