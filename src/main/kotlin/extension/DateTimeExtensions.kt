package extension

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

fun String.toLocalDateTimeFromIso(): LocalDateTime {
    return runCatching { LocalDateTime.parse(this) }
        .getOrElse {
            val instant = Instant.parse(this)
            LocalDateTime.ofInstant(instant, ZoneOffset.UTC)
        }
}
