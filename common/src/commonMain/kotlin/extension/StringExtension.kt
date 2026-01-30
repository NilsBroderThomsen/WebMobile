package extension

val String.isValidEmail: Boolean
    get() = this.contains("@") && this.contains(".")

val String.isValidUsername: Boolean
    get() = this.matches(Regex("^[A-Za-z0-9_]{3,20}$"))

fun String.formatEntryTimestamp(
    timeZone: kotlinx.datetime.TimeZone = kotlinx.datetime.TimeZone.currentSystemDefault()
): String {
    val normalized = replace("T ", "T")
    return runCatching {
        val localDateTime = kotlinx.datetime.Instant.parse(normalized)
            .toLocalDateTime(timeZone)
        val date = localDateTime.date
        val time = localDateTime.time
        val weekday = when (date.dayOfWeek) {
            kotlinx.datetime.DayOfWeek.MONDAY -> "Mo"
            kotlinx.datetime.DayOfWeek.TUESDAY -> "Di"
            kotlinx.datetime.DayOfWeek.WEDNESDAY -> "Mi"
            kotlinx.datetime.DayOfWeek.THURSDAY -> "Do"
            kotlinx.datetime.DayOfWeek.FRIDAY -> "Fr"
            kotlinx.datetime.DayOfWeek.SATURDAY -> "Sa"
            kotlinx.datetime.DayOfWeek.SUNDAY -> "So"
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
