package extension

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

fun LocalDateTime.toDateString(zoneId: ZoneId = ZoneId.systemDefault()): String {
    val date = atZone(ZoneOffset.UTC)
        .withZoneSameInstant(zoneId)
        .toLocalDate()

    return "%02d.%02d.%04d".format(
        date.dayOfMonth,
        date.monthValue,
        date.year
    )
}

fun LocalDateTime.toDateTimeString(zoneId: ZoneId = ZoneId.systemDefault()): String {
    val zonedDateTime = atZone(ZoneOffset.UTC).withZoneSameInstant(zoneId)
    val date = zonedDateTime.toLocalDate()
    val time = zonedDateTime.toLocalTime()

    return "%02d.%02d.%04d %02d:%02d".format(
        date.dayOfMonth,
        date.monthValue,
        date.year,
        time.hour,
        time.minute
    )
}
