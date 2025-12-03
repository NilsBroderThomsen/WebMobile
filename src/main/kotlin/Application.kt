import database.DatabaseFactory
import database.MoodTrackerDatabaseRepository
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.*

fun Application.configureDatabases() {
    DatabaseFactory.init()
}

fun main() {
    embeddedServer(Netty, port = 8080) {
        configureDatabases() // VOR Routing
        configureRouting()
        configureSerialization()
    }.start(wait = true)
}

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json()
    }
}

fun Application.configureRouting() {
    val repository = MoodTrackerDatabaseRepository()
    routing {
        route("/api/users") {
            post {
                // TODO: CreateUserRequest empfangen
                // TODO: User erstellen mit repository.createUser()
                // TODO: DTO zurückgeben (toEntryDTO() oder toUserDTO() Methode)
            }
            get("/{id}") {
                // TODO: ID aus Parameter lesen
                // TODO: repository.findUserById() aufrufen
                // TODO: Respond mit DTO oder 404
            }
        }
        route("/api/entries") {
            post {
                // TODO: CreateEntryRequest empfangen
                // TODO: Entry erstellen
                // TODO: DTO zurückgeben
            }
            get {
                // TODO: userId aus Query-Parameter
                // TODO: repository.findAllEntries()
                // TODO: Liste von DTOs zurückgeben
            }
            get("/{id}") {
                // TODO: repository.findEntryById()
                // TODO: DTO oder 404
            }
            put("/{id}") {
                // TODO: UpdateEntryRequest empfangen
                // TODO: repository.updateEntry()
                // TODO: DTO zurückgeben
            }
            delete("/{id}") {
                // TODO: repository.deleteEntry()
                // TODO: 204 No Content oder 404
            }
        }
    }
}
