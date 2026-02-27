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
