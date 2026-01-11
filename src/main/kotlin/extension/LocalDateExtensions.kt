package extension

import kotlinx.datetime.*
import kotlin.time.Clock

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