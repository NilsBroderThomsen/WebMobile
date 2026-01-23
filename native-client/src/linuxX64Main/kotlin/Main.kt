import api.MoodTrackerClient
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) {
    val userId = 1L  // TODO: Parse aus args
    val baseUrl = "http://localhost:8080"
    println("=== MoodTracker Native Client ===")

    runBlocking {
        val client = MoodTrackerClient(baseUrl)
        val entries = client.getEntries(userId)

        entries.forEach { entry ->
            println("Entry ID   : ${entry.id}")
            println("Title      : ${entry.title}")
            println("Content    : ${entry.content}")
            println("Mood Rating: ${entry.moodRating ?: "N/A"}")
            println("Tags       : ${if (entry.tags.isEmpty()) "None" else entry.tags.joinToString(", ")}")
            println("Created At : ${entry.createdAt}")
            println("Updated At : ${entry.updatedAt ?: "N/A"}")
            println("--------------------------------------------------")
        }
    }
}