package views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import api.MoodTrackerClient
import kotlinx.coroutines.launch
import model.LoginInput
import model.LoginModel
import model.LoginResult
import model.LoginValidation

@Composable
fun LoginPage(
    client: MoodTrackerClient,
    onNavigateToEntries: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val loginModel = remember(client) { LoginModel(client) }
    val scope = rememberCoroutineScope()

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var usernameError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    fun clearErrors() {
        usernameError = null
        passwordError = null
    }

    fun applyValidation(validation: LoginValidation) {
        usernameError = when {
            validation.missingUsername -> "Username erforderlich"
            else -> null
        }
        passwordError = when {
            validation.missingPassword -> "Passwort erforderlich"
            else -> null
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxWidth()
    ) {

        TextField(
            value = username,
            onValueChange = {
                username = it
                usernameError = null
            },
            label = { Text("Username") },
            isError = usernameError != null,
            modifier = Modifier.fillMaxWidth()
        )
        usernameError?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        TextField(
            value = password,
            onValueChange = {
                password = it
                passwordError = null
            },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            isError = passwordError != null,
            modifier = Modifier.fillMaxWidth()
        )
        passwordError?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Button(
            enabled = !isLoading,
            onClick = {
                if (isLoading) return@Button

                clearErrors()
                statusMessage = null
                isLoading = true

                scope.launch {
                    try {
                        when (
                            val result = loginModel.login(
                                LoginInput(
                                    username = username,
                                    password = password
                                )
                            )
                        ) {
                            is LoginResult.ValidationError -> {
                                applyValidation(result.validation)
                            }

                            is LoginResult.Success -> {
                                statusMessage = "Login erfolgreich. Willkommen, ${username.trim()}!"
                                onNavigateToEntries(result.loginResponse.userId)
                            }

                            is LoginResult.Failure -> {
                                statusMessage = result.message ?: "Login fehlgeschlagen."
                            }
                        }
                    } finally {
                        isLoading = false
                    }
                }
            }
        ) {
            Text("Login")
        }

        if (isLoading) {
            Spacer(modifier = Modifier.height(8.dp))
            CircularProgressIndicator()
        }

        statusMessage?.let { msg ->
            Text(msg, color = MaterialTheme.colorScheme.error.takeIf { usernameError != null || passwordError != null } ?: MaterialTheme.colorScheme.onBackground)
        }
    }
}
