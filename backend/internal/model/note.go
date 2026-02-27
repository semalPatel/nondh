package model

import "time"

type Note struct {
    ID        string
    Title     string
    Body      string
    UpdatedAt time.Time
}
