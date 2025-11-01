package model

import java.time.LocalDate
import java.time.temporal.ChronoUnit

@JvmInline
value class UserId (val value: Long)

data class User(
    val id: UserId,
    val username: String,
    val email: String,
    val passwordHash: String,
    val registrationDate: LocalDate,
    val isActive: Boolean = true
) {
    val accountAge: Long
        get() = ChronoUnit.DAYS.between(registrationDate, LocalDate.now())

    val isNewUser: Boolean
        get() = accountAge < 7

    fun deactivate(): User = copy(isActive = false)

    fun activate(): User = copy(isActive = true)
}