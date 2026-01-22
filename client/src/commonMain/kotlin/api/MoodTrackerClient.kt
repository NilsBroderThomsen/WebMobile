import dto.EntryDto
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
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
        // TODO: Implementieren Sie HTTP GET Request
        // URL: "$baseUrl/api/entries?userId=$userId"
        throw NotImplementedError()
    }

    fun close() {
        client.close()
    }
}