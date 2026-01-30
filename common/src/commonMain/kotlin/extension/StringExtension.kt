package extension

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

val String.isValidEmail: Boolean
    get() = this.contains("@") && this.contains(".")

val String.isValidUsername: Boolean
    get() = this.matches(Regex("^[A-Za-z0-9_]{3,20}$"))

fun String.formatEntryTimestamp(): String {
    val normalized = replace("T ", "T")
    return runCatching {
        val localDateTime = Instant.parse(normalized)
            .toLocalDateTime(TimeZone.currentSystemDefault())
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
            append(date.dayOfMonth.toString().padStart(2, '0'))
            append('.')
            append(date.monthNumber.toString().padStart(2, '0'))
            append('.')
            append(date.year)
            append(" • ")
            append(time.hour.toString().padStart(2, '0'))
            append(':')
            append(time.minute.toString().padStart(2, '0'))
        }
    }.getOrElse { this }
}
