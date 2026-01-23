import api.MoodTrackerClient
import kotlinx.browser.document
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.html.*
import kotlinx.html.dom.append
import kotlinx.html.js.onClickFunction

fun main() {
    val scope = MainScope()

    document.addEventListener("DOMContentLoaded", {
        val root = document.getElementById("root") ?: return@addEventListener
        root.append {
            h1 { +"MoodTracker Web Client" }
            button {
                +"Load Entries"
                onClickFunction = {
                    scope.launch {
                        loadEntries()
                    }
                }
            }
            div {
                id = "entries"
            }
        }
    })
}

suspend fun loadEntries() {
    val userId = 1L  // TODO: Parse aus args
    val baseUrl = "http://localhost:8080"

    val client = MoodTrackerClient(baseUrl)
    val entries = client.getEntries(userId)

    val entriesRoot = document.getElementById("entries") ?: return
    entriesRoot.innerHTML = ""
    entriesRoot.append {
        if (entries.isEmpty()) {
            p { +"No entries found." }
        } else {
            ul {
                entries.forEach { entry ->
                    li {
                        h3 { +entry.title }
                        p { +entry.content }
                        p { +"Mood Rating: ${entry.moodRating ?: "N/A"}" }
                        p { +"Tags: ${if (entry.tags.isEmpty()) "None" else entry.tags.joinToString(", ")}" }
                        small { +"Created At: ${entry.createdAt}" }
                    }
                }
            }
        }
    }
}