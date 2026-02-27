package store

import (
    "testing"
    "time"
)

func TestNotesCRUD(t *testing.T) {
    db, cleanup := MustTestDB(t)
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
