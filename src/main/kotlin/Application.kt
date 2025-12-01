import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.application.*

fun main() {
    fun Application.configureDatabases() {
        // TODO: DatabaseFactory.init() aufrufen
    }

    fun Application.configureRouting() {
        val repository = MoodTrackerDatabaseRepository()
        routing {
            route("/api/users") {
                post {
                    // TODO: CreateUserRequest empfangen
                    // TODO: User erstellen mit repository.createUser()
                    // TODO: DTO zurückgeben (toDTO() Methode)
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


    fun main() {
        embeddedServer(Netty, port = 8080) {
            configureDatabases()
            configureRouting()
            configureSerialization()
        }.start(wait = true)
    }
}