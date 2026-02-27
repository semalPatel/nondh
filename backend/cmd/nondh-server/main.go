package main

import (
    "log"
    "net/http"

    apphttp "nondh/internal/http"
)

func main() {
    srv := &http.Server{
        Addr:    ":8080",
        Handler: apphttp.Router(),
    }
    log.Fatal(srv.ListenAndServe())
}
