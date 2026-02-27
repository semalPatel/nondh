package store

import (
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
