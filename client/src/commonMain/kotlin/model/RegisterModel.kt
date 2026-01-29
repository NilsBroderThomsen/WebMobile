package model

import api.MoodTrackerClient
import dto.CreateUserRequest
import dto.LoginResponse
import dto.UserDto

data class RegisterInput(val username: String, val email: String, val password: String)

data class RegisterValidation(
    val missingUsername: Boolean,
    val missingEmail: Boolean,
    val missingPassword: Boolean
) {
    val hasErrors: Boolean
        get() = missingUsername || missingEmail || missingPassword
}

sealed class RegisterResult {
    data class ValidationError(val validation: RegisterValidation) : RegisterResult()
    data class Success(val user: UserDto, val loginResponse: LoginResponse) : RegisterResult()
    data class Failure(val message: String?) : RegisterResult()
}

class RegisterModel(private val client: MoodTrackerClient) {
    fun validate(input: RegisterInput): RegisterValidation {
        return RegisterValidation(
            missingUsername = input.username.isBlank(),
            missingEmail = input.email.isBlank(),
            missingPassword = input.password.isBlank()
        )
    }

    suspend fun register(input: RegisterInput): RegisterResult {
        val trimmedInput = input.copy(
            username = input.username.trim(),
            email = input.email.trim(),
            password = input.password.trim()
        )
        val validation = validate(trimmedInput)
        if (validation.hasErrors) {
            return RegisterResult.ValidationError(validation)
        }

        return try {
            val user = client.register(
                CreateUserRequest(
                    username = trimmedInput.username,
                    email = trimmedInput.email,
                    password = trimmedInput.password
                )
            )
            val loginResponse = client.login(
                username = trimmedInput.username,
                password = trimmedInput.password
            )
            RegisterResult.Success(user = user, loginResponse = loginResponse)
        } catch (ex: Exception) {
            RegisterResult.Failure(ex.message)
        }
    }
}
