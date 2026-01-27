import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import database.MoodTrackerDatabaseRepository
import di.appModule
import dto.CreateEntryRequest
import dto.CreateUserRequest
import dto.LoginResponse
import dto.LoginRequest
import dto.ErrorResponse
import dto.SuccessResponse
import dto.UpdateEntryRequest
import extension.isValidEmail
import extension.isValidMoodRating
import extension.isValidUsername
import extension.toDto
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
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.http.content.staticResources
import io.ktor.server.routing.*
import io.ktor.server.response.respond
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.origin
import io.ktor.server.request.ContentTransformationException
import io.ktor.server.request.receive
import kotlinx.serialization.json.Json
import io.ktor.server.request.receiveText
import io.ktor.server.response.respondText
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI
import org.kodein.di.ktor.di
import org.slf4j.event.Level
import security.PasswordHasher
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date
import kotlin.text.isBlank
import kotlin.text.trim
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

private const val JWT_SECRET = "my-very-secret-key"
private const val JWT_ISSUER = "MoodTrackerServer"
private const val JWT_TOKEN_TTL_HOURS = 1L
private const val LOGIN_RATE_LIMIT_MAX_ATTEMPTS = 5
private val LOGIN_RATE_LIMIT_WINDOW: Duration = 1.minutes

private data class RateLimitState(var windowStart: Instant, var attempts: Int)

private val loginRateLimitState = mutableMapOf<String, RateLimitState>()
private val loginRateLimitLock = Any()

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        configureDI()
        install(CallLogging) {
            level = Level.INFO
        }
        install(CORS) {
            allowHost("localhost:8080")
            allowHost("localhost:8081")
            allowHost("127.0.0.1:8080")
            allowHeader(HttpHeaders.ContentType)
        }
        configureJWT()
        configureRouting()
    }.start(wait = true)
}

fun Application.configureDI() {
    di {
        import(appModule)
    }
}

fun Application.configureJWT() {
    install(Authentication) {
        jwt("jwt-auth") {
            verifier(
                JWT.require(Algorithm.HMAC256(JWT_SECRET))
                    .withIssuer(JWT_ISSUER)
                    .build()
            )
            validate { credential ->
                if (credential.payload.getClaim("username").asString() !=
                    null) {
                    UserIdPrincipal(credential.payload.getClaim("username").asString())
                }
                else {
                    null
                }
            }
        }
    }
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

        getUserEntries(repository)
        getEntryDetails(repository)
        postCreateUser(repository)
        postLogin(repository)
        postCreateEntry(repository)
        putUpdateEntry(repository)
        deleteEntry(repository)

        exportJson(repository)
        exportCsv(repository)
        importJson(repository)
        importCsv(repository)
    }
}

private fun Route.postLogin(repository: MoodTrackerDatabaseRepository) {
    post("/api/login") {
        val clientIp = call.request.origin.remoteHost
        if (shouldRateLimitLogin(clientIp, Instant.now())) {
            call.respond(
                HttpStatusCode.TooManyRequests,
                ErrorResponse(
                    error = "Too Many Requests",
                    message = "Too many login attempts. Please try again later."
                )
            )
            return@post
        }

        val request = try {
            call.receive<LoginRequest>()
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
        val password = request.password.trim()

        if (username.isBlank() || password.isBlank()) {
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(
                    error = "Validation Error",
                    message = "Username and password are required"
                )
            )
            return@post
        }

        val user = repository.findUserByUsername(username)
        if (user == null || !PasswordHasher.verify(password, user.passwordHash)) {
            call.respond(
                HttpStatusCode.Unauthorized,
                ErrorResponse(
                    error = "Unauthorized",
                    message = "Wrong username or password"
                )
            )
            return@post
        }

        val now = Instant.now()
        val token = JWT.create()
            .withIssuer(JWT_ISSUER)
            .withIssuedAt(Date.from(now))
            .withExpiresAt(Date.from(now.plus(JWT_TOKEN_TTL_HOURS, ChronoUnit.HOURS)))
            .withClaim("username", user.username)
            .withClaim("userId", user.id.value)
            .sign(Algorithm.HMAC256(JWT_SECRET))

        call.respond(
            HttpStatusCode.OK,
            LoginResponse(token = token)
        )
    }
}

private fun shouldRateLimitLogin(clientIp: String, now: Instant): Boolean {
    val state = synchronized(loginRateLimitLock) {
        val existing = loginRateLimitState[clientIp]
        if (existing == null || now.isAfter(existing.windowStart.plusMillis(LOGIN_RATE_LIMIT_WINDOW.inWholeMilliseconds))) {
            RateLimitState(windowStart = now, attempts = 1).also { loginRateLimitState[clientIp] = it }
        } else {
            existing.attempts += 1
            existing
        }
    }

    return state.attempts > LOGIN_RATE_LIMIT_MAX_ATTEMPTS
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
            passwordHash = PasswordHasher.hash(password),
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
