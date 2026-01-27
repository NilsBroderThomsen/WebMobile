package extension

import java.time.LocalDate
import java.time.temporal.ChronoUnit

fun LocalDate.isToday(): Boolean =
    this == LocalDate.now()

fun LocalDate.isInPast(): Boolean =
    this < LocalDate.now()

fun LocalDate.daysSince(): Long =
    ChronoUnit.DAYS.between(this, LocalDate.now())