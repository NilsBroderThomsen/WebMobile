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
import config.AppConfig
import dto.CreateUserRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LoginViewPage(
    onNavigateBack: () -> Unit,
    onNavigateToEntries: () -> Unit     //TODO: implement UserID navigation to entries after login
) {
    var username by remember { mutableStateOf("") }
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
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Button (
            onClick = {
                if (username.isBlank() || password.isBlank()) {
                    errorMessage = "Bitte alle Felder ausfüllen."
                    statusMessage = null
                    return@Button
                }

                isLoading = true
                statusMessage = null
                errorMessage = null

                scope.launch {
                    val client = MoodTrackerClient(AppConfig.BASE_URL)
                    try {
                        delay(5000) // Simuliere Netzwerkverzögerung
    //                    val user = client.login(
    //                        LoginRequest(
    //                            username = username,
    //                            password = password
    //                        )
    //                    )
    //                    statusMessage = "Registrierung erfolgreich. Willkommen, ${user.username}!"
    //                    onNavigateToEntries()
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
            Text("Login")
        }

        if (isLoading) {
            Spacer(modifier = Modifier.height(8.dp))
            CircularProgressIndicator()
        }

        statusMessage?.let { message ->
            Text(message)
        }

        errorMessage?.let { message ->
            Text(message, color = androidx.compose.material3.MaterialTheme.colorScheme.error)
        }
    }
}
