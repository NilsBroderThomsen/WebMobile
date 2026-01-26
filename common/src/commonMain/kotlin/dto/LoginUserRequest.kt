package dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginUserRequest(
    val username: String,
    val password: String
)