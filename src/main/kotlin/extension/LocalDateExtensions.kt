package extension

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.datetime.Clock

fun LocalDate.isToday(): Boolean {
    val today = kotlin.time.Clock.System.todayIn(TimeZone.currentSystemDefault())
    return this == today
}

fun LocalDate.isInPast(): Boolean {
    val today = kotlin.time.Clock.System.todayIn(TimeZone.currentSystemDefault())
    return this < today
}

fun LocalDate.daysSince(): Long {
    val today = kotlin.time.Clock.System.todayIn(TimeZone.currentSystemDefault())
    return (today.toEpochDays() - this.toEpochDays())
}