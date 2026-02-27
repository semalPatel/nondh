FROM golang:1.22-alpine AS build
WORKDIR /src
COPY go.mod go.sum ./
RUN go mod download
COPY . .
RUN CGO_ENABLED=0 go build -o /bin/nondh ./cmd/nondh-server

FROM alpine:3.19
WORKDIR /app
COPY --from=build /bin/nondh /app/nondh
EXPOSE 8080
ENTRYPOINT ["/app/nondh"]
