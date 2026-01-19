package extension

import java.time.LocalDate
import java.time.ZoneId

fun LocalDate.isToday(): Boolean {
    val today = LocalDate.now(ZoneId.systemDefault())
    return this == today
}

fun LocalDate.isInPast(): Boolean {
    val today = LocalDate.now(ZoneId.systemDefault())
    return this.isBefore(today)
}

fun LocalDate.daysSince(): Long {
    val today = LocalDate.now(ZoneId.systemDefault())
    return (today.toEpochDay() - this.toEpochDay())
}
