package extension

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

fun String.toDisplayTimestamp(timeZone: TimeZone = TimeZone.currentSystemDefault()): String {
    val normalized = replace("T ", "T")
    return runCatching {
        val localDateTime = Instant.parse(normalized)
            .toLocalDateTime(timeZone)
        val date = localDateTime.date
        val time = localDateTime.time
        val weekday = when (date.dayOfWeek) {
            DayOfWeek.MONDAY -> "Mo"
            DayOfWeek.TUESDAY -> "Di"
            DayOfWeek.WEDNESDAY -> "Mi"
            DayOfWeek.THURSDAY -> "Do"
            DayOfWeek.FRIDAY -> "Fr"
            DayOfWeek.SATURDAY -> "Sa"
            DayOfWeek.SUNDAY -> "So"
        }
        buildString {
            append(weekday)
            append(", ")
            append(date.day.toString().padStart(2, '0'))
            append('.')
            append(date.month.number.toString().padStart(2, '0'))
            append('.')
            append(date.year)
            append(" • ")
            append(time.hour.toString().padStart(2, '0'))
            append(':')
            append(time.minute.toString().padStart(2, '0'))
        }
    }.getOrElse { this }
}