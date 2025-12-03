import database.DatabaseFactory
import database.MoodTrackerDatabaseRepository
import dto.toEntryDTO
import dto.toUserDTO
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import java.time.LocalDate
import java.time.LocalDateTime
import model.Entry
import model.EntryId
import model.User
import model.UserId

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
                val request = call.receive<CreateUserRequest>()
                val user = User(
                    id = UserId(0),
                    username = request.username,
                    email = request.email,
                    passwordHash = request.passwordHash,
                    registrationDate = LocalDate.now(),
                    isActive = true
                )

                val saved = repository.createUser(user)
                call.respond(HttpStatusCode.Created, saved.toUserDTO())
            }
            get("/{id}") {
                val id = call.parameters["id"]?.toLongOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid user id")

                val user = repository.findUserById(UserId(id))
                    ?: return@get call.respond(HttpStatusCode.NotFound)

                call.respond(user.toUserDTO())
            }
        }
        route("/api/entries") {
            post {
                val request = call.receive<CreateEntryRequest>()
                val entry = Entry(
                    id = EntryId(0),
                    userId = UserId(request.userId),
                    title = request.title,
                    content = request.content,
                    moodRating = request.moodRating,
                    createdAt = LocalDateTime.now(),
                    updatedAt = null,
                    tags = request.tags.map { it.trim().lowercase() }.filter { it.isNotBlank() }.toSet()
                )

                val saved = repository.createEntry(entry)
                call.respond(HttpStatusCode.Created, saved.toEntryDTO())
            }
            get {
                val userId = call.request.queryParameters["userId"]?.toLongOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest, "userId query parameter is required")

                val entries = repository.findAllEntries(UserId(userId))
                call.respond(entries.map { it.toEntryDTO() })
            }
            get("/{id}") {
                val id = call.parameters["id"]?.toLongOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid entry id")

                val entry = repository.findEntryById(EntryId(id))
                    ?: return@get call.respond(HttpStatusCode.NotFound)

                call.respond(entry.toEntryDTO())
            }
            put("/{id}") {
                val id = call.parameters["id"]?.toLongOrNull()
                    ?: return@put call.respond(HttpStatusCode.BadRequest, "Invalid entry id")

                val request = call.receive<UpdateEntryRequest>()
                val existing = repository.findEntryById(EntryId(id))
                    ?: return@put call.respond(HttpStatusCode.NotFound)

                val updated = existing.copy(
                    title = request.title ?: existing.title,
                    content = request.content ?: existing.content,
                    moodRating = request.moodRating ?: existing.moodRating,
                    tags = request.tags?.map { it.trim().lowercase() }?.filter { it.isNotBlank() }?.toSet()
                        ?: existing.tags,
                    updatedAt = LocalDateTime.now()
                )

                val saved = repository.updateEntry(updated)
                call.respond(saved.toEntryDTO())
            }
            delete("/{id}") {
                val id = call.parameters["id"]?.toLongOrNull()
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, "Invalid entry id")

                val deleted = repository.deleteEntry(EntryId(id))
                if (deleted) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }
        }
    }
}

@kotlinx.serialization.Serializable
data class CreateUserRequest(
    val username: String,
    val email: String,
    val passwordHash: String
)

@kotlinx.serialization.Serializable
data class CreateEntryRequest(
    val userId: Long,
    val title: String,
    val content: String,
    val moodRating: Int? = null,
    val tags: List<String> = emptyList()
)

@kotlinx.serialization.Serializable
data class UpdateEntryRequest(
    val title: String? = null,
    val content: String? = null,
    val moodRating: Int? = null,
    val tags: List<String>? = null
)
