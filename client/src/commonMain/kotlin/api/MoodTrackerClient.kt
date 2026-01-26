package api

import dto.CreateUserRequest
import dto.EntryDto
import dto.ErrorResponse
import dto.UserDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class MoodTrackerClient(private val baseUrl: String) {
    private val json = Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
    }
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
            json(json)
        }
    }

    suspend fun getEntries(userId: Long): List<EntryDto> {
        val url = "$baseUrl/api/users/$userId/entries"
        try {
            val response = client.get(url)
            if (response.status.isSuccess()) {
                return response.body()
            }

            val bodyText = response.bodyAsText()
            val errorMessage = runCatching {
                json.decodeFromString<ErrorResponse>(bodyText).message
            }.getOrNull() ?: "Abrufen der Einträge fehlgeschlagen (${response.status.value})"
            throw IllegalStateException(errorMessage)
        } catch (ex: Exception) {
            if (ex is IllegalStateException) {
                throw ex
            }
            throw IllegalStateException("Keine Verbindung zum Server möglich.")
        }
    }


    suspend fun registerUser(request: CreateUserRequest): UserDto {
        val url = "$baseUrl/api/users"
        val response = client.post(url) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        if (response.status.isSuccess()) {
            return response.body()
        }

        val bodyText = response.bodyAsText()
        val errorMessage = runCatching {
            json.decodeFromString<ErrorResponse>(bodyText).message
        }.getOrNull() ?: "Registrierung fehlgeschlagen (${response.status.value})"
        throw IllegalStateException(errorMessage)
    }

    fun close() {
        client.close()
    }
}
