package model

import api.MoodTrackerClient
import dto.LoginResponse

data class LoginInput(val username: String, val password: String)

data class LoginValidation(
    val missingUsername: Boolean,
    val missingPassword: Boolean
) {
    val hasErrors: Boolean
        get() = missingUsername || missingPassword
}

sealed class LoginResult {
    data class ValidationError(val validation: LoginValidation) : LoginResult()
    data class Success(val loginResponse: LoginResponse) : LoginResult()
    data class Failure(val message: String?) : LoginResult()
}

class LoginModel(private val client: MoodTrackerClient) {
    fun validate(input: LoginInput): LoginValidation {
        return LoginValidation(
            missingUsername = input.username.isBlank(),
            missingPassword = input.password.isBlank()
        )
    }

    suspend fun login(input: LoginInput): LoginResult {
        val validation = validate(input)
        if (validation.hasErrors) {
            return LoginResult.ValidationError(validation)
        }

        return try {
            val loginResponse = client.login(
                username = input.username,
                password = input.password
            )
            LoginResult.Success(loginResponse = loginResponse)
        } catch (ex: Exception) {
            LoginResult.Failure(ex.message)
        }
    }
}
