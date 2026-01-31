package api

import dto.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.delay
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

    suspend fun register(request: CreateUserRequest): UserDto {
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
        }.getOrNull() ?: "Registration failed (${response.status.value})"
        throw IllegalStateException(errorMessage)
    }

    suspend fun login(username: String, password: String): LoginResponse {
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
        }.getOrNull() ?: "Login failed (${response.status.value})"
        throw IllegalStateException(errorMessage)
    }

    fun logout() {
        authToken = null
        authenticatedUserId = null
    }

    suspend fun getEntries(userId: Long): List<EntryDto> {
        delay(1250)     // Simulate network latency

        if (authToken == null) {
            throw IllegalStateException("Login required.")
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
            }.getOrNull() ?: "Failed to fetch entries (${response.status.value})"

            throw IllegalStateException(errorMessage)
        } catch (ex: Exception) {
            if (ex is IllegalStateException) {
                throw ex
            }
            throw IllegalStateException("Unable to connect to the server.")
        }
    }

    suspend fun getEntryDetails(entryId: Long): EntryDto {
        delay(1250)     // Simulate network latency

        val url = "$baseUrl/api/entries/$entryId"
        try {
            val response = client.get(url)
            if (response.status.isSuccess()) {
                return response.body()
            }

            val bodyText = response.bodyAsText()
            val errorMessage = runCatching {
                json.decodeFromString<ErrorResponse>(bodyText).message
            }.getOrNull() ?: "Failed to fetch entry details (${response.status.value})"

            throw IllegalStateException(errorMessage)
        } catch (ex: Exception) {
            if (ex is IllegalStateException) {
                throw ex
            }
            throw IllegalStateException("Unable to connect to the server.")
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
        }.getOrNull() ?: "Failed to create entry (${response.status.value})"
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
        }.getOrNull() ?: "Failed to update entry (${response.status.value})"
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
            }.getOrNull() ?: "Failed to delete entry (${response.status.value})"
            throw IllegalStateException(errorMessage)
        } catch (ex: Exception) {
            if (ex is IllegalStateException) {
                throw ex
            }
            throw IllegalStateException("Unable to connect to the server.")
        }
    }

    fun close() {
        client.close()
    }
}
