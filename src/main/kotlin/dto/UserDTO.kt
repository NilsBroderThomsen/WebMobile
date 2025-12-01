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

fun User.toDTO(): UserDTO = UserDTO(
    id = this.id.value,
    username = this.username,
    email = this.email,
    registrationDate = this.registrationDate.toString(),
    isActive = this.isActive
)
