package api

import dto.CreateUserRequest
import dto.EntryDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class MoodTrackerClient(private val baseUrl: String) {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }

    suspend fun getEntries(userId: Long): List<EntryDto> {
        var url = "$baseUrl/api/users/$userId/entries"
        return client.get(url).body<List<EntryDto>>()
    }

    fun close() {
        client.close()
    }
}