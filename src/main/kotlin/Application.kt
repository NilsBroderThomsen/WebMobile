import extension.toEmoji
import extension.toMoodLevel
import io.ktor.http.HttpStatusCode
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.html.*
import kotlinx.html.*
import org.slf4j.event.Level
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respondRedirect
import model.Entry
import model.EntryId
import model.UserId
import repository.MoodTrackerRepository

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
        // TODO: GET / - Homepage mit Entry-Liste und Formular
        get("/") {
            val userId = UserId(1)
            val entries = repository.findAllEntries(userId)
            call.respondHtml {
                head {
                    title { +"MoodTracker - Home" }
                    link( rel = "stylesheet", href = "/static/styles.css", type = "text/css" )
                }
                body {
                    h1 { +"MoodTracker - Meine Einträge" }
                    ul {
                        entries.forEach {
                            li {
                                a(href = "/entries/${it.id.value}") { +"Entry${it.id.value}: ${it.title}" }
                                +" — ${it.content.take(100)} — "
                                +"${it.moodRating?.toMoodLevel()?.emoji ?: ""}${it.moodRating ?: "Keine Bewertung!"} — "
                                +"${it.createdAt.toLocalDate()} — "
                                a(href = "/entries/${it.id.value}/delete") { +"[löschen]" }
                            }
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

        // TODO: POST /entries - Neuen Entry erstellen
        post("/entries") {
            val parameters = call.receiveParameters()
            val title = parameters["title"] ?: ""
            val content = parameters["content"] ?: ""
            val moodRating = parameters["moodRating"]?.toIntOrNull()
            // Validierung
            if (title.isBlank() || content.isBlank()) {
                call.respondHtml(HttpStatusCode.BadRequest) {
                    body {
                        h1 {
                            +"Fehler"
                        }
                        p {
                        }
                    }
                    return@post
                }
                // Entry erstellen...
            }

            // TODO: Parameter auslesen (title, content, moodRating)
            // TODO: Validierung
            // TODO: Entry erstellen und zum Repository hinzufügen
            // TODO: Redirect zu "/"
            call.respondRedirect("/")
        }

        // TODO: GET /entries/{id} - Entry Detail-Ansicht
        get("/entries/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
            if (id == null) {
                call.respondHtml(HttpStatusCode.BadRequest) {
                    body {
                        h1 {
                            +"Ungültige ID" }
                        a(href
                        = "/") { +"Zurück" }
                    }
                }
                return@get
            }
            val entry = repository.findEntryById(EntryId(id))
            if (entry == null) {
                call.respondHtml(HttpStatusCode.NotFound) {
                    body {
                        h1 {
                            +"Eintrag nicht gefunden" }
                        a(href = "/") { +"Zurück" }
                    }
                }
                return@get
            }
            // Entry anzeigen...
        }
        // TODO: POST /entries/{id}/delete - Entry löschen
        // TODO: staticResources("/static", "static")
    }
}