package dto

import kotlinx.serialization.*
import serialization.LocalDateSerializer
import java.time.LocalDate

@Serializable
data class UserDto(
    val id: Long,
    val username: String,
    val email: String,
    @Serializable(with = LocalDateSerializer::class)
    val registrationDate: LocalDate,
    val isActive: Boolean
)