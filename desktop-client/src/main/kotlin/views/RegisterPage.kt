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
import model.RegisterInput
import model.RegisterModel
import model.RegisterResult
import model.RegisterValidation

@Composable
fun RegisterPage(
    client: MoodTrackerClient,
    onNavigateToEntries: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var usernameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val registerModel = remember(client) { RegisterModel(client) }

    fun clearErrors() {
        usernameError = null
        emailError = null
        passwordError = null
    }

    fun applyValidation(validation: RegisterValidation) {
        usernameError = when {
            validation.missingUsername -> "Username erforderlich"
            validation.invalidUsername -> "Username zu kurz oder ungültig"
            else -> null
        }

        emailError = when {
            validation.missingEmail -> "E-Mail erforderlich"
            validation.invalidEmail -> "Ungültige E-Mail-Adresse"
            else -> null
        }

        passwordError = when {
            validation.missingPassword -> "Passwort erforderlich"
            validation.invalidPassword -> "Passwort muss mindestens 8 Zeichen haben"
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
            value = email,
            onValueChange = {
                email = it
                emailError = null
            },
            label = { Text("Email") },
            isError = emailError != null,
            modifier = Modifier.fillMaxWidth()
        )
        emailError?.let {
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
                            val result = registerModel.register(
                                RegisterInput(
                                    username = username,
                                    email = email,
                                    password = password
                                )
                            )
                        ) {
                            is RegisterResult.ValidationError -> {
                                applyValidation(result.validation)
                            }

                            is RegisterResult.Success -> {
                                statusMessage =
                                    "Registrierung erfolgreich. Willkommen, ${result.user.username}!"
                                onNavigateToEntries(result.loginResponse.userId)
                            }

                            is RegisterResult.Failure -> {
                                statusMessage =
                                    result.message ?: "Registrierung fehlgeschlagen."
                            }
                        }
                    } finally {
                        isLoading = false
                    }
                }
            }
        ) {
            Text("Register")
        }

        if (isLoading) {
            Spacer(modifier = Modifier.height(8.dp))
            CircularProgressIndicator()
        }

        statusMessage?.let {
            Text(it)
        }
    }
}
