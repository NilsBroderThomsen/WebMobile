pacakge api

class MoodTrackerClient(private val baseUrl: String) {
    private val client = HttpClient {
        // TODO: Installieren Sie ContentNegotiation
        // TODO: Konfigurieren Sie JSON
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