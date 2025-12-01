import database.DatabaseFactory
import database.MoodTrackerDatabaseRepository
import dto.toDTO
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import model.Entry
import model.EntryId
import model.User
import model.UserId
import java.time.LocalDate
import java.time.LocalDateTime

@Serializable
data class CreateUserRequest(
    val username: String,
    val email: String,
    val passwordHash: String,
    val registrationDate: String
)

@Serializable
data class CreateEntryRequest(
    val userId: Long,
    val title: String,
    val content: String,
    val moodRating: Int? = null,
    val createdAt: String? = null
)

@Serializable
data class UpdateEntryRequest(
    val title: String,
    val content: String,
    val moodRating: Int? = null
)

fun Application.configureDatabases() {
    DatabaseFactory.init()
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
                val request = call.receive<CreateUserRequest>()
                val user = User(
                    id = UserId(0),
                    username = request.username,
                    email = request.email,
                    passwordHash = request.passwordHash,
                    registrationDate = LocalDate.parse(request.registrationDate)
                )
                val created = repository.createUser(user)
                call.respond(created.toDTO())
            }
            get("/{id}") {
                val idParam = call.parameters["id"]?.toLongOrNull()
                    ?: return@get call.respondText("Invalid user id", status = io.ktor.http.HttpStatusCode.BadRequest)

                val user = repository.findUserById(UserId(idParam))
                    ?: return@get call.respondText("User not found", status = io.ktor.http.HttpStatusCode.NotFound)

                call.respond(user.toDTO())
            }
        }

        route("/api/entries") {
            post {
                val request = call.receive<CreateEntryRequest>()
                val createdAt = request.createdAt?.let(LocalDateTime::parse) ?: LocalDateTime.now()
                val entry = Entry(
                    id = EntryId(0),
                    userId = UserId(request.userId),
                    title = request.title,
                    content = request.content,
                    moodRating = request.moodRating,
                    createdAt = createdAt
                )
                val created = repository.createEntry(entry)
                call.respond(created.toDTO())
            }

            get {
                val userIdParam = call.request.queryParameters["userId"]?.toLongOrNull()
                    ?: return@get call.respondText("Missing userId", status = io.ktor.http.HttpStatusCode.BadRequest)

                val entries = repository.findAllEntries(UserId(userIdParam))
                call.respond(entries.map { it.toDTO() })
            }

            get("/{id}") {
                val idParam = call.parameters["id"]?.toLongOrNull()
                    ?: return@get call.respondText("Invalid entry id", status = io.ktor.http.HttpStatusCode.BadRequest)

                val entry = repository.findEntryById(EntryId(idParam))
                    ?: return@get call.respondText("Entry not found", status = io.ktor.http.HttpStatusCode.NotFound)

                call.respond(entry.toDTO())
            }

            put("/{id}") {
                val idParam = call.parameters["id"]?.toLongOrNull()
                    ?: return@put call.respondText("Invalid entry id", status = io.ktor.http.HttpStatusCode.BadRequest)
                val request = call.receive<UpdateEntryRequest>()

                val existing = repository.findEntryById(EntryId(idParam))
                    ?: return@put call.respondText("Entry not found", status = io.ktor.http.HttpStatusCode.NotFound)

                val updated = repository.updateEntry(
                    existing.copy(
                        title = request.title,
                        content = request.content,
                        moodRating = request.moodRating
                    )
                )
                call.respond(updated.toDTO())
            }

            delete("/{id}") {
                val idParam = call.parameters["id"]?.toLongOrNull()
                    ?: return@delete call.respondText("Invalid entry id", status = io.ktor.http.HttpStatusCode.BadRequest)

                val deleted = repository.deleteEntry(EntryId(idParam))
                if (deleted) {
                    call.respond(io.ktor.http.HttpStatusCode.NoContent)
                } else {
                    call.respond(io.ktor.http.HttpStatusCode.NotFound)
                }
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
