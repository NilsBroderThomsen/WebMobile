import database.DatabaseFactory
import database.MoodTrackerDatabaseRepository
import di.appModule
import dto.CreateEntryRequest
import dto.CreateUserRequest
import dto.ErrorResponse
import dto.SuccessResponse
import dto.UpdateEntryRequest
import extension.entryCard
import extension.isValidEmail
import extension.isValidMoodRating
import extension.isValidUsername
import extension.toDto
import extension.toEmoji
import model.Entry
import model.EntryId
import model.UserId
import service.ExportService
import service.ImportService
import io.ktor.http.ContentDisposition
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.application.*
import io.ktor.server.html.respondHtml
import io.ktor.server.http.content.staticResources
import io.ktor.server.routing.*
import io.ktor.server.response.respond
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.request.ContentTransformationException
import io.ktor.server.request.receive
import io.ktor.server.request.receiveParameters
import kotlinx.serialization.json.Json
import io.ktor.server.request.receiveText
import io.ktor.server.response.respondRedirect
import io.ktor.server.response.respondText
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.html.*
import org.jetbrains.exposed.sql.Database
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI
import org.kodein.di.ktor.di
import org.slf4j.event.Level
import kotlin.text.isBlank
import kotlin.text.trim
import kotlin.time.Clock

fun Application.configureDI() {
    di {
        import(appModule)
    }
}

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        configureDI()
        install(CallLogging) {
            level = Level.INFO
        }
        install(CORS) {
            allowHost("localhost:8080")
            allowHost("127.0.0.1:8080")
            allowHeader(HttpHeaders.ContentType)
        }
        configureRouting()
    }.start(wait = true)
}

fun Application.configureRouting() {
    val di by closestDI()

    val repository: MoodTrackerDatabaseRepository by di.instance()

    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }

    routing {
        staticResources("/static", "static")
        getHomeHTML(repository)
        getEntryDetailsHTML(repository)
        postCreateEntryHTML(repository)
        postDeleteEntryHTML(repository)

        getUserEntries(repository)
        getEntryDetails(repository)
        postCreateUser(repository)
        postCreateEntry(repository)
        putUpdateEntry(repository)
        deleteEntry(repository)

        exportJson(repository)
        exportCsv(repository)
        importJson(repository)
        importCsv(repository)
    }
}

private fun Route.getHomeHTML(repository: MoodTrackerDatabaseRepository) {
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

private fun Route.getEntryDetailsHTML(repository: MoodTrackerDatabaseRepository) {
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
                p { +"Erstellt am: ${entry.createdDate}" }

                entry.moodRating?.let { mood ->
                    p { +"Stimmung: $mood/10 ${mood.toEmoji()}" }
                } ?: run {
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

private fun Route.postCreateEntryHTML(repository: MoodTrackerDatabaseRepository) {
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
            createdAt =  Clock.System.now(),
            updatedAt = null,
            tags = emptySet()
        )
        repository.createEntry(entry)

        call.respondRedirect("/")
    }
}

private fun Route.postDeleteEntryHTML(repository: MoodTrackerDatabaseRepository) {
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

private fun Route.getUserEntries(repository: MoodTrackerDatabaseRepository) {
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

private fun Route.getEntryDetails(repository: MoodTrackerDatabaseRepository) {
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

private fun Route.postCreateUser(repository: MoodTrackerDatabaseRepository) {
    post("/api/users") {
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
        
        val username = request.username.trim()
        val email = request.email.trim()
        val password = request.password.trim()
        val errors = validateUserPayload(username, email, password)

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

        val existingEmail = repository.findUserByEmail(email)
        if (existingEmail != null) {
            call.respond(
                HttpStatusCode.Conflict,
                ErrorResponse(
                    error = "Conflict",
                    message = "Email is already registered"
                )
            )
            return@post
        }

        val existingUsername = repository.findUserByUsername(username)
        if (existingUsername != null) {
            call.respond(
                HttpStatusCode.Conflict,
                ErrorResponse(
                    error = "Conflict",
                    message = "Username is already taken"
                )
            )
            return@post
        }

        val user = model.User(
            id = UserId(System.currentTimeMillis()),
            username = username,
            email = email,
            passwordHash = password,
            registrationDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
            isActive = true
        )

        val savedUser = repository.createUser(user)
        call.respond(HttpStatusCode.Created, savedUser.toDto())
    }
}

private fun Route.postCreateEntry(repository: MoodTrackerDatabaseRepository) {
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
            createdAt = Clock.System.now(),
            updatedAt = null,
            tags = emptySet()
        )

        val savedEntry = repository.createEntry(entry)

        call.respond(HttpStatusCode.Created, savedEntry.toDto())
    }
}

private fun Route.deleteEntry(repository: MoodTrackerDatabaseRepository) {
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

private fun Route.putUpdateEntry(repository: MoodTrackerDatabaseRepository) {
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
            updatedAt = Clock.System.now()
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

private fun validateUserPayload(
    username: String,
    email: String,
    password: String
): List<String> {
    val errors = mutableListOf<String>()

    if (username.isBlank()) {
        errors += "Username must not be blank."
    } else if (!username.isValidUsername) {
        errors += "Username must be 3-20 characters (letters, numbers, underscore)."
    }

    if (email.isBlank()) {
        errors += "Email must not be blank."
    } else if (!email.isValidEmail) {
        errors += "Email format is invalid."
    }

    if (password.isBlank()) {
        errors += "Password must not be blank."
    } else if (password.length < 8) {
        errors += "Password must be at least 8 characters."
    }

    return errors
}

private fun Route.exportJson(repository: MoodTrackerDatabaseRepository) {
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

private fun Route.exportCsv(repository: MoodTrackerDatabaseRepository) {
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

private fun Route.importJson(repository: MoodTrackerDatabaseRepository) {
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

private fun Route.importCsv(repository: MoodTrackerDatabaseRepository) {
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