import extension.entryCard
import extension.toEmoji
import io.ktor.http.HttpStatusCode
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.html.*
import io.ktor.server.http.content.staticResources
import kotlinx.html.*
import org.slf4j.event.Level
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respondRedirect
import model.Entry
import model.EntryId
import model.UserId
import repository.MoodTrackerRepository
import java.time.LocalDateTime

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        install(CallLogging) {
            level = Level.INFO
        }
        configureRouting()
    }.start(wait = true)
}

fun Application.configureRouting() {
    val repository = MoodTrackerRepository()
    repository.initializeWithTestData()

    routing {
        getHome(repository)
        getEntryDetails(repository)
        postCreateEntry(repository)
        postDeleteEntry(repository)
        staticResources("/static", "static")
    }
}

private fun Route.getHome(repository: MoodTrackerRepository) {
    get("/") {
        val userId = UserId(1)
        val entries = repository.findAllEntries(userId)

        call.respondHtml {
            head {
                title { +"MoodTracker - Home" }
                link(rel = "stylesheet", href = "/static/styles.css", type = "text/css")
            }
            body {
                h1 { +"MoodTracker - Meine Einträge" }
                section{
                    if (entries.isNotEmpty()) {
                        ul { entries.forEach { entryCard(it) } }
                    } else {
                        p { +"Noch keine Einträge vorhanden" }
                    }
                }

                section {
                    h2 { +"Neuer Eintrag" }
                    form(action = "/entries", method = FormMethod.post) {
                        label {
                            +"Titel:"
                            input(type = InputType.text, name = "title") {
                                placeholder = "Titel eingeben"
                                required = true
                            }
                        }
                        br
                        label {
                            +"Inhalt:"
                            textArea {
                                name = "content"
                                placeholder = "Deine Gedanken..."
                                required = true
                            }
                        }
                        br
                        label {
                            +"Stimmung (1-10):"
                            input(type = InputType.number, name = "moodRating") {
                                placeholder = "Optional"
                                min = "1"
                                max = "10"
                            }
                        }
                        br
                        button(type = ButtonType.submit) { +"Erstellen" }
                    }
                }
            }
        }
    }
}

private fun Route.postCreateEntry(repository: MoodTrackerRepository) {
    post("/entries") {
        val parameters = call.receiveParameters()
        val title = parameters["title"] ?: ""
        val content = parameters["content"] ?: ""
        val moodRatingRaw = parameters["moodRating"]?.trim().orEmpty()

        // Validierung
        val errors = mutableListOf<String>()

        if (title.isBlank()) {
            errors += "Titel darf nicht leer sein."
        }
        if (content.isBlank()) {
            errors += "Inhalt darf nicht leer sein."
        }

        val moodRating = if (moodRatingRaw.isBlank()) {
            null
        } else {
            val rating = moodRatingRaw.toIntOrNull()
            when (rating) {
                null -> {
                    errors += "Stimmung muss eine Zahl sein."
                    null
                }
                !in 1..10 -> {
                    errors += "Stimmung muss zwischen 1 und 10 liegen."
                    null
                }
                else -> rating
            }
        }

        if (errors.isNotEmpty()) {
            call.respondHtml(HttpStatusCode.BadRequest) {
                head {
                    title { +"Fehler beim Erstellen" }
                    link(rel = "stylesheet", href = "/static/styles.css", type = "text/css")
                }
                body {
                    h1 { +"Fehler beim Erstellen des Eintrags" }
                    ul {
                        errors.forEach { error ->
                            li { +error }
                        }
                    }
                    a(href = "/") { +"Zurück" }
                }
            }
            return@post
        }
        val entry = Entry(
            id = EntryId(System.currentTimeMillis()),
            userId = UserId(1),
            title = title,
            content = content,
            moodRating = moodRating,
            createdAt = LocalDateTime.now(),
            updatedAt = null,
            tags = emptySet()
        )
        repository.addEntry(entry)

        call.respondRedirect("/")
    }
}

private fun Route.getEntryDetails(repository: MoodTrackerRepository) {
    get("/entries/{id}") {
        val id = call.parameters["id"]?.toLongOrNull()
        if (id == null) {
            call.respondHtml(HttpStatusCode.BadRequest) {
                body {
                    h1 {
                        +"Ungültige ID"
                    }
                    a(
                        href
                        = "/"
                    ) { +"Zurück" }
                }
            }
            return@get
        }
        val entry = repository.findEntryById(EntryId(id))
        if (entry == null) {
            call.respondHtml(HttpStatusCode.NotFound) {
                body {
                    h1 {
                        +"Eintrag nicht gefunden"
                    }
                    a(href = "/") { +"Zurück" }
                }
            }
            return@get
        }
        call.respondHtml {
            head {
                title { +"MoodTracker - ${entry.title}" }
                link(rel = "stylesheet", href = "/static/styles.css", type = "text/css")
            }
            body {
                h1 { +entry.title }
                p { +"Erstellt am: ${entry.createdAt.toLocalDate()}" }
                if (entry.moodRating != null) {
                    p { +"Stimmung: ${entry.moodRating}/10 ${entry.moodRating.toEmoji()}" }
                } else {
                    p { +"Keine Stimmung angegeben" }
                }
                section {
                    h2 { +"Inhalt" }
                    p { +entry.content }
                }
                form(action = "/entries/${entry.id.value}/delete", method = FormMethod.post) {
                    button(type = ButtonType.submit) { +"Löschen" }
                }
                br
                a(href = "/") { +"Zurück zur Übersicht" }
            }
        }
    }
}

private fun Route.postDeleteEntry(repository: MoodTrackerRepository) {
    post("/entries/{id}/delete") {
        val id = call.parameters["id"]?.toLongOrNull()
        if (id == null) {
            call.respondHtml(HttpStatusCode.BadRequest) {
                body {
                    h1 { +"Ungültige ID" }
                    a(href = "/") { +"Zurück" }
                }
            }
            return@post
        }

        val deleted = repository.deleteEntry(EntryId(id))
        if (!deleted) {
            call.respondHtml(HttpStatusCode.NotFound) {
                body {
                    h1 { +"Eintrag nicht gefunden" }
                    a(href = "/") { +"Zurück" }
                }
            }
            return@post
        }

        call.respondRedirect("/")
    }
}