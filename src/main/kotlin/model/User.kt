package model

import kotlinx.datetime.*
import kotlin.time.Clock

@JvmInline
value class UserId(val value: Long)

data class User(
    val id: UserId,
    val username: String,
    val email: String,
    val passwordHash: String,
    val registrationDate: LocalDate,
    val isActive: Boolean = true
) {
    val accountAge: Long
        get() {
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            return (today.toEpochDays() - registrationDate.toEpochDays()).toLong()
        }

    val isNewUser: Boolean
        get() = accountAge < 7

    fun deactivate(): User = copy(isActive = false)

    fun activate(): User = copy(isActive = true)
}