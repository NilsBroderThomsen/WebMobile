package extension

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.Clock
import kotlinx.datetime.todayIn

fun LocalDate.isToday(): Boolean {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    return this == today
}

fun LocalDate.isInPast(): Boolean {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    return this < today
}

fun LocalDate.daysSince(): Long {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    return (today.toEpochDays() - this.toEpochDays())
}
