package extension

import java.time.Instant
import java.time.ZoneId

fun Instant.toDateString(timeZone: ZoneId = ZoneId.systemDefault()): String {
    val localDateTime = this.atZone(timeZone).toLocalDateTime()
    val date = localDateTime.toLocalDate()
    return "%02d.%02d.%04d".format(
        date.dayOfMonth,
        date.monthValue,
        date.year
    )
}

fun Instant.toDateTimeString(timeZone: ZoneId = ZoneId.systemDefault()): String {
    val localDateTime = this.atZone(timeZone).toLocalDateTime()
    val date = localDateTime.toLocalDate()
    val time = localDateTime.toLocalTime()

    return "%02d.%02d.%04d %02d:%02d".format(
        date.dayOfMonth,
        date.monthValue,
        date.year,
        time.hour,
        time.minute
    )
}
