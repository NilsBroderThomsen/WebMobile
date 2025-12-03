package dto

import kotlinx.serialization.Serializable
import model.User

@Serializable
data class UserDTO(
    val id: Long,
    val username: String,
    val email: String,
    val registrationDate: String,
    val isActive: Boolean
)

fun User.toUserDTO(): UserDTO = UserDTO(
    id = id.value,
    username = username,
    email = email,
    registrationDate = registrationDate.toString(),
    isActive = isActive
)
