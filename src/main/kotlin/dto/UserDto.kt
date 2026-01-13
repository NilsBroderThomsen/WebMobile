package dto

import kotlinx.serialization.*

@Serializable
data class UserDto(
    val id: Long,
    val username: String,
    val email: String,
    val registrationDate: String,
    val isActive: Boolean
)