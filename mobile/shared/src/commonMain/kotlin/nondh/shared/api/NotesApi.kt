package nondh.shared.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.ContentType
import nondh.shared.Note

interface NotesApi {
    suspend fun upsert(note: Note)
    suspend fun list(since: Long): List<Note>
}

class NotesApiClient(
    private val baseUrl: String,
    private val token: String,
    private val client: HttpClient = createHttpClient()
) : NotesApi {
    private fun authHeader() = "Bearer $token"

    override suspend fun upsert(note: Note) {
        client.post {
            url("$baseUrl/notes")
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, authHeader())
            setBody(note)
        }
    }

    override suspend fun list(since: Long): List<Note> {
        return client.get {
            url("$baseUrl/notes?since=$since")
            header(HttpHeaders.Authorization, authHeader())
        }.body()
    }
}
