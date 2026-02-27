package handlers_test

import (
    "bytes"
    "encoding/json"
    "net/http"
    "net/http/httptest"
    "testing"
    "time"

    apphttp "nondh/internal/http"
    "nondh/internal/http/handlers"
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

    h := handlers.NewNotesHandler(db)
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
