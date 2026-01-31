package extension

import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

fun Instant.toDateString(timeZone: TimeZone = TimeZone.currentSystemDefault()): String {
    val localDateTime = this.toLocalDateTime(timeZone)

    val date = localDateTime.date

    return "%02d.%02d.%04d".format(
        date.day,
        date.month.number,
        date.year
    )
}

fun Instant.toDateTimeString(timeZone: TimeZone = TimeZone.currentSystemDefault()): String {
    val localDateTime = this.toLocalDateTime(timeZone)

    val date = localDateTime.date
    val time = localDateTime.time

    return "%02d.%02d.%04d %02d:%02d".format(
        date.day,
        date.month.number,
        date.year,
        time.hour,
        time.minute
    )
}