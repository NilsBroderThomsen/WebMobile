package util

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.Base64

@Serializable
data class JwtPayload(
    @SerialName("userId")
    val userId: Long? = null,
    @SerialName("username")
    val username: String? = null
)

fun extractUserIdFromToken(token: String): Long? {
    val parts = token.split(".")
    if (parts.size < 2) {
        return null
    }

    val payloadBytes = runCatching {
        Base64.getUrlDecoder().decode(parts[1])
    }.getOrNull() ?: return null

    val payloadJson = payloadBytes.toString(Charsets.UTF_8)
    val payload = runCatching {
        Json { ignoreUnknownKeys = true }.decodeFromString<JwtPayload>(payloadJson)
    }.getOrNull()

    return payload?.userId
}
