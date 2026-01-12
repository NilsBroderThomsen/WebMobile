import dto.CreateEntryRequest
import dto.ErrorResponse
import dto.SuccessResponse
import dto.UpdateEntryRequest
import extension.isValidMoodRating
import extension.toDto
import model.Entry
import model.EntryId
import model.UserId
import repository.MoodTrackerRepository
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
import io.ktor.server.routing.*
import io.ktor.server.response.respond
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.ContentTransformationException
import io.ktor.server.request.receive
import kotlinx.serialization.json.Json
import io.ktor.server.request.receiveText
import io.ktor.server.response.respondText
import org.slf4j.event.Level
import kotlin.time.Clock

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
        getUserEntries(repository)
        getEntryDetails(repository)
        postCreateEntry(repository)
        putUpdateEntry(repository)
        deleteEntry(repository)

        exportJson(repository)
        exportCsv(repository)
        importJson(repository)
        importCsv(repository)
    }
}

private fun Route.getUserEntries(repository: MoodTrackerRepository) {
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

private fun Route.getEntryDetails(repository: MoodTrackerRepository) {
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

private fun Route.postCreateEntry(repository: MoodTrackerRepository) {
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

        val savedEntry = repository.addEntry(entry)

        call.respond(HttpStatusCode.Created, savedEntry.toDto())
    }
}

private fun Route.deleteEntry(repository: MoodTrackerRepository) {
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

private fun Route.putUpdateEntry(repository: MoodTrackerRepository) {
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
            ?: return@put call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse(
                    error = "Internal Server Error",
                    message = "Failed to update entry"
                )
            )

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

private fun Route.exportJson(repository: MoodTrackerRepository) {
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

private fun Route.exportCsv(repository: MoodTrackerRepository) {
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

private fun Route.importJson(repository: MoodTrackerRepository) {
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

private fun Route.importCsv(repository: MoodTrackerRepository) {
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