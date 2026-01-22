import api.MoodTrackerClient
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) {
    val userId = 1L  // TODO: Parse aus args
    val baseUrl = "http://localhost:8080"
    println("=== MoodTracker JVM Client ===")

    // TODO: runBlocking verwenden
    runBlocking {
        // TODO: MoodTrackerClient erstellen
        val client = MoodTrackerClient(baseUrl)

        // TODO: getEntries(userId) aufrufen
        val entries = client.getEntries(userId)

        // TODO: Entries formatiert ausgeben
    }
}