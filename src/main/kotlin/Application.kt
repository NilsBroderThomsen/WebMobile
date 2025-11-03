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

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        install(CallLogging) {
            level
            = Level.INFO
        }
        configureRouting()
    }.start(wait = true)
}
fun Application.configureRouting() {
    val repository = MoodTrackerRepository()
    repository.initializeWithTestData()
    routing {
        // TODO: GET / - Homepage mit Entry-Liste und Formular
        // TODO: POST /entries - Neuen Entry erstellen
        // TODO: GET /entries/{id} - Entry Detail-Ansicht
        // TODO: POST /entries/{id}/delete - Entry löschen
        // TODO: staticResources("/static", "static")
    }
}