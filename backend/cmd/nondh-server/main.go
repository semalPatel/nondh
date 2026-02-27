package main

import (
    "log"
    "net/http"
    "os"

    apphttp "nondh/internal/http"
    "nondh/internal/http/handlers"
    "nondh/internal/store"
)

func main() {
    token := os.Getenv("NONDH_TOKEN")
    if token == "" {
        log.Fatal("NONDH_TOKEN is required")
    }

    dbPath := os.Getenv("NONDH_DB_PATH")
    if dbPath == "" {
        dbPath = "data/nondh.db"
    }

    db, err := store.Open(dbPath)
    if err != nil {
        log.Fatalf("open db: %v", err)
    }

    notesHandler := handlers.NewNotesHandler(db)

    srv := &http.Server{
        Addr:    ":8080",
        Handler: apphttp.RouterWithNotesAndAuth(notesHandler, token),
    }
    log.Fatal(srv.ListenAndServe())
}
