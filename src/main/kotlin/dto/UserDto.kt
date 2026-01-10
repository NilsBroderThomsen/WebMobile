package dto

import kotlinx.datetime.LocalDate
import kotlinx.serialization.*

@Serializable
data class UserDto(
    val id: Long,
    val username: String,
    val email: String,
    val registrationDate: LocalDate,
    val isActive: Boolean
)