package dto

import kotlinx.serialization.Serializable

@Serializable
data class UserDTO(
    val id: Long,
    val username: String,
    val email: String,
    val registrationDate: String,
    val isActive: Boolean
)
