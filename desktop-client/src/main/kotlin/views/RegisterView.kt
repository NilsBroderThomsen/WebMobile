package views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import dto.CreateUserRequest
import kotlinx.coroutines.launch

@Composable
fun RegisterView(
    onNavigateBack: () -> Unit,
    onNavigateToEntries: () -> Unit
) {
    val baseUrl = "http://localhost:8080"
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(onClick = onNavigateBack) {
            Text("Back")
        }

        TextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )

        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                val trimmedUsername = username.trim()
                val trimmedEmail = email.trim()
                val trimmedPassword = password.trim()
                if (trimmedUsername.isBlank() || trimmedEmail.isBlank() || trimmedPassword.isBlank()) {
                    errorMessage = "Bitte alle Felder ausfüllen."
                    statusMessage = null
                    return@Button
                }

                isLoading = true
                statusMessage = null
                errorMessage = null
                scope.launch {
                    val client = MoodTrackerClient(baseUrl)
                    try {
                        val user = client.registerUser(
                            CreateUserRequest(
                                username = trimmedUsername,
                                email = trimmedEmail,
                                password = trimmedPassword
                            )
                        )
                        statusMessage = "Registrierung erfolgreich. Willkommen, ${user.username}!"
                        onNavigateToEntries()
                    } catch (ex: Exception) {
                        errorMessage = ex.message ?: "Registrierung fehlgeschlagen."
                    } finally {
                        client.close()
                        isLoading = false
                    }
                }
            },
            enabled = !isLoading
        ) {
            Text("Register")
        }

        if (isLoading) {
            Spacer(modifier = Modifier.height(8.dp))
            CircularProgressIndicator()
        }

        statusMessage?.let { message ->
            Text(message)
        }

        errorMessage?.let { message ->
            Text(message)
        }
    }
}
