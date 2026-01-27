package extension

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
private val DATETIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

fun LocalDateTime.toDateString(): String =
    this.toLocalDate().format(DATE_FORMAT)

fun LocalDateTime.toDateTimeString(): String =
    this.format(DATETIME_FORMAT)
