package extension

import kotlinx.datetime.*
import kotlin.time.Instant

fun Instant.toDateString(timeZone: TimeZone = TimeZone.currentSystemDefault()): String {
    val localDateTime = this.toLocalDateTime(timeZone)

    val day = localDateTime.date.dayOfMonth
    val month = localDateTime.date.monthNumber
    val year = localDateTime.date.year

    return "%02d.%02d.%04d".format(day, month, year)
}


fun Instant.toDateTimeString(timeZone: TimeZone = TimeZone.currentSystemDefault()): String {
    val localDateTime = this.toLocalDateTime(timeZone)

    val date = localDateTime.date
    val time = localDateTime.time

    return "%02d.%02d.%04d %02d:%02d".format(
        date.dayOfMonth,
        date.monthNumber,
        date.year,
        time.hour,
        time.minute
    )
}