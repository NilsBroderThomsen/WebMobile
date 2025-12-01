import database.DatabaseFactory
import database.MoodTrackerDatabaseRepository
import extension.entryCard
import extension.isValidMoodRating
import extension.toEmoji
import extension.toDto
import dto.CreateEntryRequest
import dto.CreateUserRequest
import dto.ErrorResponse
import dto.SuccessResponse
import dto.UpdateEntryRequest
import io.ktor.http.ContentDisposition
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.html.*
import io.ktor.server.http.content.staticResources
import io.ktor.server.netty.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.ContentTransformationException
import io.ktor.server.request.receive
import io.ktor.server.request.receiveParameters
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.response.respondText
import io.ktor.server.routing.*
import kotlinx.html.*
import kotlinx.serialization.json.Json
import org.slf4j.event.Level
import model.Entry
import model.EntryId
import model.User
import model.UserId
import service.ExportService
import service.ImportService
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.time.ExperimentalTime

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        install(CallLogging) {
            level = Level.INFO
        }
        configureDatabases()
        configureRouting()
    }.start(wait = true)
}

fun Application.configureDatabases() {
    DatabaseFactory.init()
}

@OptIn(ExperimentalTime::class)
fun Application.configureRouting() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
    val repository = MoodTrackerDatabaseRepository()
    repository.findUserById(UserId(1)) ?: repository.createUser(
        User(
            id = UserId(0),
            username = "Nils",
            email = "nils@sample.com",
            passwordHash = "dbhasjl",
            registrationDate = LocalDate.now()
        )
    )

    routing {
        getHome(repository)
        getEntryDetails(repository)
        postCreateEntry(repository)
        postDeleteEntry(repository)
        staticResources("/static", "static")

        route("/api/users") {
            post {
                val request = try {
                    call.receive<CreateUserRequest>()
                } catch (ex: ContentTransformationException) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse(
                            error = "Bad Request",
                            message = "Request body is not valid JSON"
                        )
                    )
                    return@post
                }

                val registrationDate = request.registrationDate?.let {
                    runCatching { LocalDate.parse(it) }.getOrNull()
                } ?: LocalDate.now()

                val createdUser = repository.createUser(
                    User(
                        id = UserId(0),
                        username = request.username.trim(),
                        email = request.email.trim(),
                        passwordHash = request.passwordHash,
                        registrationDate = registrationDate
                    )
                )

                call.respond(HttpStatusCode.Created, createdUser.toDto())
            }

            get("/{id}") {
                val id = call.parameters["id"]?.toLongOrNull()
                    ?: return@get call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse(
                            error = "Bad Request",
                            message = "Invalid user id"
                        )
                    )

                val user = repository.findUserById(UserId(id))
                    ?: return@get call.respond(
                        HttpStatusCode.NotFound,
                        ErrorResponse(
                            error = "Not Found",
                            message = "User with id $id not found"
                        )
                    )

                call.respond(HttpStatusCode.OK, user.toDto())
            }
        }

        getUserEntriesApi(repository)
        getEntryDetailsApi(repository)
        postCreateEntryApi(repository)
        putUpdateEntryApi(repository)
        deleteEntryApi(repository)

        exportJsonApi(repository)
        exportCsvApi(repository)
        importJsonApi(repository)
        importCsvApi(repository)
    }
}

private fun Route.getHome(repository: MoodTrackerDatabaseRepository) {
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
                section {
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

private fun Route.getEntryDetails(repository: MoodTrackerDatabaseRepository) {
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

private fun Route.postCreateEntry(repository: MoodTrackerDatabaseRepository) {
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
        repository.createEntry(entry)

        call.respondRedirect("/")
    }
}

private fun Route.postDeleteEntry(repository: MoodTrackerDatabaseRepository) {
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

private fun Route.getUserEntriesApi(repository: MoodTrackerDatabaseRepository) {
    get("/api/users/{userId}/entries") {
        val userId = call.parameters["userId"]?.toLongOrNull()

        if (userId == null) {
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(
                    error = "Bad Request",
                    message = "Invalid userId"
                )
            )
            return@get
        }

        val entries = repository.findAllEntries(UserId(userId))
        val dtos = entries.map { it.toDto() }
        call.respond(HttpStatusCode.OK, dtos)
    }
}

private fun Route.getEntryDetailsApi(repository: MoodTrackerDatabaseRepository) {
    get("/api/entries/{id}") {
        val id = call.parameters["id"]?.toLongOrNull()
        if (id == null) {
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(
                    error = "Bad Request",
                    message = "Invalid entry id"
                )
            )
            return@get
        }

        val entry = repository.findEntryById(EntryId(id))
        if (entry == null) {
            call.respond(
                HttpStatusCode.NotFound,
                ErrorResponse(
                    error = "Not Found",
                    message = "Entry with id $id not found"
                )
            )
            return@get
        }

        call.respond(HttpStatusCode.OK, entry.toDto())
    }
}

private fun Route.postCreateEntryApi(repository: MoodTrackerDatabaseRepository) {
    post("/api/users/{userId}/entries") {
        val userId = call.parameters["userId"]?.toLongOrNull()
            ?: return@post call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("Bad Request", "Invalid userId")
            )
        val request = try {
            call.receive<CreateEntryRequest>()
        } catch (ex: ContentTransformationException) {
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(
                    error = "Bad Request",
                    message = "Request body is not valid JSON"
                )
            )
            return@post
        }

        val title = request.title.trim()
        val content = request.content.trim()
        val moodRating = request.moodRating
        val errors = validateEntryPayload(title, content, moodRating)

        if (errors.isNotEmpty()) {
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(
                    error = "Validation Error",
                    message = errors.joinToString(" ")
                )
            )
            return@post
        }

        val entry = Entry(
            id = EntryId(System.currentTimeMillis()),
            userId = UserId(userId),
            title = title,
            content = content,
            moodRating = moodRating,
            createdAt = LocalDateTime.now(),
            updatedAt = null,
            tags = emptySet()
        )

        val savedEntry = repository.createEntry(entry)

        call.respond(HttpStatusCode.Created, savedEntry.toDto())
    }
}

private fun Route.deleteEntryApi(repository: MoodTrackerDatabaseRepository) {
    delete("/api/entries/{id}") {
        val id = call.parameters["id"]?.toLongOrNull()
        if (id == null) {
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(
                    error = "Bad Request",
                    message = "Invalid entry id"
                )
            )
            return@delete
        }

        val deleted = repository.deleteEntry(EntryId(id))
        if (!deleted) {
            call.respond(
                HttpStatusCode.NotFound,
                ErrorResponse(
                    error = "Not Found",
                    message = "Entry with id $id not found"
                )
            )
            return@delete
        }

        call.respond(
            HttpStatusCode.OK,
            SuccessResponse(message = "Entry deleted successfully")
        )
    }
}

private fun Route.putUpdateEntryApi(repository: MoodTrackerDatabaseRepository) {
    put("/api/entries/{id}") {
        val id = call.parameters["id"]?.toLongOrNull()
            ?: return@put call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(
                    error = "Bad Request",
                    message = "Invalid entry id"
                )
            )

        val existingEntry = repository.findEntryById(EntryId(id))
            ?: return@put call.respond(
                HttpStatusCode.NotFound,
                ErrorResponse(
                    error = "Not Found",
                    message = "Entry with id $id not found"
                )
            )

        val request = try {
            call.receive<UpdateEntryRequest>()
        } catch (ex: ContentTransformationException) {
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(
                    error = "Bad Request",
                    message = "Request body is not valid JSON"
                )
            )
            return@put
        }

        val title = request.title.trim()
        val content = request.content.trim()
        val moodRating = request.moodRating
        val errors = validateEntryPayload(title, content, moodRating)

        if (errors.isNotEmpty()) {
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(
                    error = "Validation Error",
                    message = errors.joinToString(" ")
                )
            )
            return@put
        }

        val updatedEntry = existingEntry.copy(
            title = title,
            content = content,
            moodRating = moodRating,
            updatedAt = LocalDateTime.now()
        )

        val savedEntry = repository.updateEntry(updatedEntry)

        call.respond(HttpStatusCode.OK, savedEntry.toDto())
    }
}

private fun validateEntryPayload(
    title: String,
    content: String,
    moodRating: Int?
): List<String> {
    val errors = mutableListOf<String>()

    if (title.isBlank()) {
        errors += "Title must not be blank."
    }

    if (content.isBlank()) {
        errors += "Content must not be blank."
    }

    if (moodRating != null && !moodRating.isValidMoodRating()) {
        errors += "Mood rating must be a number between 1 and 10 or Blank."
    }

    return errors
}

private fun Route.exportJsonApi(repository: MoodTrackerDatabaseRepository) {
    val exportService = ExportService(repository)
    get("/api/users/{userId}/export/json") {
        val userId = call.parameters["userId"]?.toLongOrNull()
        if (userId == null) {
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(
                    error = "Bad Request",
                    message = "Invalid userId"
                )
            )
            return@get
        }

        val exportJson = exportService.exportToJson(UserId(userId))
        call.respondText(
            exportJson,
            ContentType.Application.Json
        )
    }
}

private fun Route.exportCsvApi(repository: MoodTrackerDatabaseRepository) {
    val exportService = ExportService(repository)
    get("/api/users/{userId}/export/csv") {
        val userId = call.parameters["userId"]?.toLongOrNull()
        if (userId == null) {
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(
                    error = "Bad Request",
                    message = "Invalid userId"
                )
            )
            return@get
        }

        val exportCsv = exportService.exportToCsv(UserId(userId))
        val filename = "moodtracker-entries-$userId.csv"
        call.response.headers.append(
            HttpHeaders.ContentDisposition,
            ContentDisposition.Attachment.withParameter(ContentDisposition.Parameters.FileName, filename).toString()
        )
        call.respondText(
            exportCsv,
            ContentType.Text.CSV
        )
    }
}

private fun Route.importJsonApi(repository: MoodTrackerDatabaseRepository) {
    post("/api/users/{userId}/import/json") {
        val userId = call.parameters["userId"]?.toLongOrNull()
            ?: return@post call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("Bad Request", "Invalid userId")
            )

        val jsonPayload = call.receiveText()
        val importService = ImportService(repository)
        val result = importService.importFromJson(jsonPayload, UserId(userId))

        val status = if (result.failed > 0) {
            HttpStatusCode.PartialContent
        } else {
            HttpStatusCode.OK
        }

        call.respond(status, result)
    }
}

private fun Route.importCsvApi(repository: MoodTrackerDatabaseRepository) {
    post("/api/users/{userId}/import/csv") {
        val userId = call.parameters["userId"]?.toLongOrNull()
            ?: return@post call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("Bad Request", "Invalid userId")
            )

        val csvPayload = call.receiveText()
        val importService = ImportService(repository)
        val result = importService.importFromCsv(csvPayload, UserId(userId))

        val status = if (result.failed > 0) {
            HttpStatusCode.PartialContent
        } else {
            HttpStatusCode.OK
        }

        call.respond(status, result)
    }
}