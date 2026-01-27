package api

import dto.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class MoodTrackerClient(private val baseUrl: String) {
    private val json = Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
    }
    private var authToken: String? = null
    var authenticatedUserId: Long? = null
        private set
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(json)
        }
        defaultRequest {
            authToken?.let { token ->
                header(HttpHeaders.Authorization, "Bearer $token")
            }
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

    suspend fun loginUser(username: String, password: String): LoginResponse {
        val url = "$baseUrl/api/login"
        val response = client.post(url) {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(username = username, password = password))
        }
        if (response.status.isSuccess()) {
            val loginResponse: LoginResponse = response.body()
            authToken = loginResponse.token
            authenticatedUserId = loginResponse.userId
            return loginResponse
        }

        val bodyText = response.bodyAsText()
        val errorMessage = runCatching {
            json.decodeFromString<ErrorResponse>(bodyText).message
        }.getOrNull() ?: "Login fehlgeschlagen (${response.status.value})"
        throw IllegalStateException(errorMessage)
    }

    suspend fun getEntries(userId: Long): List<EntryDto> {
        if (authToken == null) {
            throw IllegalStateException("Login erforderlich.")
        }
        val url = "$baseUrl/api/users/$userId/entries"
        try {
            val response = client.get(url) {
                header(HttpHeaders.Authorization, "Bearer $authToken")
            }
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

    suspend fun getEntryDetails(entryId: Long): EntryDto {
        val url = "$baseUrl/api/entries/$entryId"
        try {
            val response = client.get(url)
            if (response.status.isSuccess()) {
                return response.body()
            }

            val bodyText = response.bodyAsText()
            val errorMessage = runCatching {
                json.decodeFromString<ErrorResponse>(bodyText).message
            }.getOrNull() ?: "Abrufen der Eintragsdetails fehlgeschlagen (${response.status.value})"

            throw IllegalStateException(errorMessage)
        } catch (ex: Exception) {
            if (ex is IllegalStateException) {
                throw ex
            }
            throw IllegalStateException("Keine Verbindung zum Server möglich.")
        }
    }

    suspend fun createEntry(userId: Long ,request: CreateEntryRequest): EntryDto {
        val url = "$baseUrl/api/users/$userId/entries"
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

    suspend fun updateEntry(entryId: Long, request: UpdateEntryRequest): EntryDto {
        val url = "$baseUrl/api/entries/${entryId}"
        val response = client.put(url) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        if (response.status.isSuccess()) {
            return response.body()
        }

        val bodyText = response.bodyAsText()
        val errorMessage = runCatching {
            json.decodeFromString<ErrorResponse>(bodyText).message
        }.getOrNull() ?: "Aktualisierung des Eintrags fehlgeschlagen (${response.status.value})"
        throw IllegalStateException(errorMessage)
    }

    suspend fun deleteEntry(entryId: Long) {
        val url = "$baseUrl/api/entries/$entryId"
        try {
            val response = client.delete(url)
            if (response.status.isSuccess()) {
                return
            }

            val bodyText = response.bodyAsText()
            val errorMessage = runCatching {
                json.decodeFromString<ErrorResponse>(bodyText).message
            }.getOrNull() ?: "Löschen des Eintrags fehlgeschlagen (${response.status.value})"
            throw IllegalStateException(errorMessage)
        } catch (ex: Exception) {
            if (ex is IllegalStateException) {
                throw ex
            }
            throw IllegalStateException("Keine Verbindung zum Server möglich.")
        }
    }

    fun close() {
        client.close()
    }
}
