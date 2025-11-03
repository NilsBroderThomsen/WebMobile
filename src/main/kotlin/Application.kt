import extension.toMoodLevel
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.http.*
import io.ktor.server.http.content.*
import kotlinx.html.*
import org.slf4j.event.Level
import io.ktor.server.plugins.calllogging.*
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
            // TODO: Entries aus Repository holen
            call.respondHtml {
                head {
                    title { +"MoodTracker - Home" }
                    link(
                        rel = "stylesheet", href = "/static/styles.css", type =
                            "text/css"
                    )
                }
                body {
                    h1 {
                        +"MoodTracker - Meine Einträge"
                    }
                    ul {
                        entries.forEach {
                            li {
                                +"Entry${it.id.value}: ${it.title} ${it.content.take(100)}"
                                +" — "
                                +"${it.moodRating?.toMoodLevel()?.emoji ?: ""}${it.moodRating ?: "Keine Bewertung!"}"
                                +" — "
                                +"${it.createdAt.toLocalDate()}"
                                +" — "
                                a(href = "/entries/${it.id}/delete") { +"[löschen]" }
                            }
                        }
                    }
                    section {
                        h2 {

                        }
                        // TODO: Section für Formular
                        section {
                            h2 {
                                +"Neuer Eintrag"
                            }
                            form(action = "/entries", method = FormMethod.post) {
                            }
                        }
                    }
                }
            }
        }

        // TODO: POST /entries - Neuen Entry erstellen
        // TODO: GET /entries/{id} - Entry Detail-Ansicht
        // TODO: POST /entries/{id}/delete - Entry löschen
        // TODO: staticResources("/static", "static")
    }
}