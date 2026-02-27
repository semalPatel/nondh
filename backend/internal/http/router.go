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
