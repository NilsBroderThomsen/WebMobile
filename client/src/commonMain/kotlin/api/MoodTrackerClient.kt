package api

import dto.EntryDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import dto.CreateEntryRequest

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

    suspend fun createEntry(userId: Long, request: CreateEntryRequest): EntryDto {
        val url = "$baseUrl/api/users/$userId/entries"
        return client.post(url) {
            setBody(request)
        }.body()
    }

    fun close() {
        client.close()
    }
}
