package dto

import kotlinx.serialization.*
import kotlin.time.Clock

// DTO for error responses
@Serializable
data class ErrorResponse(
    val error: String,
    val message: String,
    val timestamp: String =  Clock.System.now().toString()
)
