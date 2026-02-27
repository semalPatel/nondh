package http

import (
    "net/http"

    "nondh/internal/auth"
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
