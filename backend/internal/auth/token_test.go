package auth

import (
    "net/http"
    "net/http/httptest"
    "testing"
)

func TestTokenAuth(t *testing.T) {
    h := TokenMiddleware("secret", http.HandlerFunc(func(w http.ResponseWriter, _ *http.Request) {
        w.WriteHeader(http.StatusOK)
    }))

    req := httptest.NewRequest(http.MethodGet, "/notes", nil)
    rr := httptest.NewRecorder()
    h.ServeHTTP(rr, req)
    if rr.Code != http.StatusUnauthorized {
        t.Fatalf("expected 401, got %d", rr.Code)
    }

    req2 := httptest.NewRequest(http.MethodGet, "/notes", nil)
    req2.Header.Set("Authorization", "Bearer secret")
    rr2 := httptest.NewRecorder()
    h.ServeHTTP(rr2, req2)
    if rr2.Code != http.StatusOK {
        t.Fatalf("expected 200, got %d", rr2.Code)
    }
}
