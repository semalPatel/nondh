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
    DeletedAt *int64 `json:"deleted_at,omitempty"`
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
    var deleted *time.Time
    if p.DeletedAt != nil {
        v := time.UnixMilli(*p.DeletedAt).UTC()
        deleted = &v
    }
    n := model.Note{ID: p.ID, Title: p.Title, Body: p.Body, UpdatedAt: time.UnixMilli(p.UpdatedAt).UTC(), DeletedAt: deleted}
    if err := h.db.UpsertNote(n); err != nil {
        http.Error(w, "store error", http.StatusInternalServerError)
        return
    }
    w.WriteHeader(http.StatusCreated)
}

func (h *NotesHandler) List(w http.ResponseWriter, r *http.Request) {
    sinceStr := r.URL.Query().Get("since")
    since, _ := strconv.ParseInt(sinceStr, 10, 64)
    notes, err := h.db.NotesSince(time.UnixMilli(since).UTC())
    if err != nil {
        http.Error(w, "store error", http.StatusInternalServerError)
        return
    }
    var out []notePayload
    for _, n := range notes {
        var deleted *int64
        if n.DeletedAt != nil {
            v := n.DeletedAt.UnixMilli()
            deleted = &v
        }
        out = append(out, notePayload{ID: n.ID, Title: n.Title, Body: n.Body, UpdatedAt: n.UpdatedAt.UnixMilli(), DeletedAt: deleted})
    }
    _ = json.NewEncoder(w).Encode(out)
}
