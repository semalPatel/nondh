package store

import (
    "database/sql"
    "time"

    "nondh/internal/model"
)

type Note = model.Note

func (d *DB) UpsertNote(n Note) error {
    var deleted *int64
    if n.DeletedAt != nil {
        v := n.DeletedAt.UnixMilli()
        deleted = &v
    }
    _, err := d.db.Exec(`
        INSERT INTO notes (id, title, body, updated_at, deleted_at)
        VALUES (?, ?, ?, ?, ?)
        ON CONFLICT(id) DO UPDATE SET
            title=excluded.title,
            body=excluded.body,
            updated_at=excluded.updated_at,
            deleted_at=excluded.deleted_at
    `, n.ID, n.Title, n.Body, n.UpdatedAt.UnixMilli(), deleted)
    return err
}

func (d *DB) GetNote(id string) (Note, error) {
    var n Note
    var ts int64
    var deleted sql.NullInt64
    err := d.db.QueryRow(`SELECT id, title, body, updated_at, deleted_at FROM notes WHERE id=?`, id).
        Scan(&n.ID, &n.Title, &n.Body, &ts, &deleted)
    if err != nil {
        return Note{}, err
    }
    n.UpdatedAt = time.UnixMilli(ts).UTC()
    if deleted.Valid {
        v := time.UnixMilli(deleted.Int64).UTC()
        n.DeletedAt = &v
    }
    return n, nil
}

func (d *DB) NotesSince(ts time.Time) ([]Note, error) {
    rows, err := d.db.Query(`SELECT id, title, body, updated_at, deleted_at FROM notes WHERE updated_at > ? ORDER BY updated_at ASC`, ts.UnixMilli())
    if err != nil {
        return nil, err
    }
    defer rows.Close()

    var out []Note
    for rows.Next() {
        var n Note
        var tsv int64
        var deleted sql.NullInt64
        if err := rows.Scan(&n.ID, &n.Title, &n.Body, &tsv, &deleted); err != nil {
            return nil, err
        }
        n.UpdatedAt = time.UnixMilli(tsv).UTC()
        if deleted.Valid {
            v := time.UnixMilli(deleted.Int64).UTC()
            n.DeletedAt = &v
        }
        out = append(out, n)
    }
    return out, rows.Err()
}
