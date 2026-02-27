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

func MustTestDB(t *testing.T) (*DB, func()) {
    t.Helper()
    db, err := Open(":memory:")
    if err != nil {
        t.Fatalf("open db: %v", err)
    }
    return db, func() { _ = db.db.Close() }
}
