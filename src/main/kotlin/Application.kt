import dto.CreateEntryRequest
import dto.ErrorResponse
import dto.SuccessResponse
import extension.entryCard
import extension.isValidMoodRating
import extension.toDto
import extension.toEmoji
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.html.*
import io.ktor.server.http.content.staticResources
import kotlinx.html.*
import org.slf4j.event.Level
import io.ktor.server.response.respond
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receive
import kotlinx.serialization.json.Json
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
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
    val repository = MoodTrackerRepository()
    repository.initializeWithTestData()

    routing {
        getHome(repository)
        getEntryDetails(repository)
        postCreateEntry(repository)
        postDeleteEntry(repository)
        staticResources("/static", "static")

        getUserEntriesApi(repository)
        getEntryDetailsApi(repository)
        postCreateEntryApi(repository)
        deleteEntryApi(repository)
        exportJsonApi(repository)
        exportCsvApi(repository)
        importJsonApi(repository)
        importCsvApi(repository)
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

private fun Route.getUserEntriesApi(repository: MoodTrackerRepository) {
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

private fun Route.getEntryDetailsApi(repository: MoodTrackerRepository) {
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

private fun Route.postCreateEntryApi(repository: MoodTrackerRepository) {
    post("/api/users/{userId}/entries") {
        val userId = call.parameters["userId"]?.toLongOrNull()
            ?: return@post call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("Bad Request", "Invalid userId")
            )
        val request = call.receive<CreateEntryRequest>()
        val errors = mutableListOf<String>()

        if (request.title.isBlank()) {
            errors += "Title must not be blank."
        }

        if (request.content.isBlank()) {
            errors += "Content must not be blank."
        }

        val moodRating = request.moodRating
        if (moodRating != null && !moodRating.isValidMoodRating()) {
            errors += "Mood rating must be a number between 1 and 10 or Blank."
        }

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
            title = request.title,
            content = request.content,
            moodRating = moodRating,
            createdAt = LocalDateTime.now(),
            updatedAt = null,
            tags = emptySet()
        )

        val savedEntry = repository.addEntry(entry)

        call.respond(HttpStatusCode.Created, savedEntry.toDto())
    }
}

private fun Route.deleteEntryApi(repository: MoodTrackerRepository) {
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

private fun Route.exportJsonApi(repository: MoodTrackerRepository) {
    get("/api/users/{userId}/export/json") {
        // TODO: userId Parameter validieren
        // TODO: ExportService erstellen
        // TODO: exportToJson aufrufen
        // TODO: respondText mit ContentType.Application.Json
        TODO("Export JSON endpoint implementieren")
    }
}

private fun Route.exportCsvApi(repository: MoodTrackerRepository) {
    get("/api/users/{userId}/export/csv") {
        // TODO: userId Parameter validieren
        // TODO: ExportService erstellen
        // TODO: exportToCsv aufrufen
        // TODO: respondText mit ContentType.Text.CSV
        // TODO: Content-Disposition Header setzen: "attachment; filename=\"...\""
        TODO("Export CSV endpoint implementieren")
    }
}

private fun Route.importJsonApi(repository: MoodTrackerRepository) {
    post("/api/users/{userId}/import/json") {
        // TODO: userId validieren
        // TODO: JSON Daten empfangen mit call.receiveText()
        // TODO: ImportService erstellen
        // TODO: importFromJson aufrufen
        // TODO: Response mit OK oder PartialContent (wenn failed > 0)
        TODO("Import JSON endpoint implementieren")
    }
}

private fun Route.importCsvApi(repository: MoodTrackerRepository) {
    post("/api/users/{userId}/import/csv") {
        // TODO: userId validieren
        // TODO: CSV Daten empfangen mit call.receiveText()
        // TODO: ImportService erstellen
        // TODO: importFromCsv aufrufen
        // TODO: Response mit OK oder PartialContent
        TODO("Import CSV endpoint implementieren")
    }
}